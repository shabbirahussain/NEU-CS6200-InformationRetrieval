package com.ir.homework.hw3.elasticclient;

import static com.ir.homework.hw3.Constants.*;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.Flushable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;

public class ElasticClient implements Flushable{
	private static Client _client = null;
	//private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss");
	
	private BulkRequestBuilder _bulkBuilder; // Stores the requests for loading data
	private Map<String, Map<String, Object>> enqueueBuffer; // Stores enqueue requests
	
	
	/**
	 * Default constructor
	 * @throws UnknownHostException 
	 */
	public ElasticClient() throws UnknownHostException{
		Builder settings = Settings.settingsBuilder()
				.put("client.transport.ignore_cluster_name", false)
		        .put("node.client", true)
		        .put("client.transport.sniff", true)
		        .put("cluster.name", CLUSTER_NAME);;
		
		_client = TransportClient.builder().settings(settings.build()).build()
		        .addTransportAddress(
		        		new InetSocketTransportAddress(InetAddress.getByName(HOST), PORT));
		_bulkBuilder  = _client.prepareBulk();
		
		enqueueBuffer = new HashMap<String, Map<String, Object>>();
	}
	
	// --------------------------- Loaders ----------------------------
	public synchronized void loadData(String id, String title, String string){
		IndexRequestBuilder irBuilder = _client.prepareIndex()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId(id)
				.setSource(FIELD_TEXT, string)
				.setSource(FIELD_TITLE, title)
				.setSource(FIELD_DT_UPDATED, (new Date()).toString());
		
		_bulkBuilder.add(irBuilder);
		return;
	}
	
	@Override
	public synchronized void flush(){
		// load all buffered documents if any
		_bulkBuilder.get();
		_bulkBuilder = _client.prepareBulk();
		
		this.enqueue(this.enqueueBuffer);
		this.enqueueBuffer = new HashMap<String, Map<String, Object>>();
	}
	
	/**
	 * Dequeues n elements from elastic search queue
	 * @param size is the number of elements to be dequeued
	 * @return A collection of items from queue
	 * @throws Exception
	 */
	public synchronized SearchHit[] dequeue(Integer size) throws Exception{
		SearchHit[] hits = this.getNextItems(size);
		this.removeItems(hits);
		
		return hits;
	}
	
	/**
	 * Enqueues the requested links to the elastic search buffer. A call to flush is required for buffer to be written back to es.
	 * @param score
	 * @param url is the url to store
	 * @throws MalformedURLException 
	 */
	public synchronized void enqueue(Float score, URL url, Integer discoveryTime) throws MalformedURLException{
		Map<String, Object> params = this.enqueueBuffer.getOrDefault(url, DEFAULT_QUEUE_FIELDS);
		
		score             = Math.max(score, (Float) params.get(FIELD_PARENT_SCORE));
		Integer inCnt     = (Integer) params.get(FIELD_IN_CNT) + 1 ;
		discoveryTime     = Math.min(discoveryTime, (Integer) params.get(FIELD_DISCOVERY_TIME) + 1 );
		String domainName = url.getHost();
				
		params.put(FIELD_PARENT_SCORE, score);
		params.put(FIELD_IN_CNT, inCnt);
		params.put(FIELD_DISCOVERY_TIME, discoveryTime);
		params.put(FIELD_DOMAIN_NAME, domainName);
		
		this.enqueueBuffer.put(url.toString(), params);
	}
	
	
	// ----------------------------------------------------------------
	
	private void enqueue(Map<String, Map<String, Object>> items){
		BulkRequestBuilder bulkBuilder = _client.prepareBulk();
		
		for(String id : items.keySet()){
			Map<String, Object> params = items.get(id);
			
			IndexRequestBuilder indexRequest = _client.prepareIndex()
					.setIndex(QUEUE_NAME)
					.setType(QUEUE_TYPE)
					.setId(id)
					.setSource(params);
			
			UpdateRequestBuilder request = _client.prepareUpdate()
					.setIndex(QUEUE_NAME)
					.setType(QUEUE_TYPE)
					.setId(id)
					.setUpsert(indexRequest.request())
					.setScript(new Script(SCRIPT_ENQUEUE, ScriptType.INDEXED, "groovy", params));
			
			bulkBuilder.add(request);
		}
		
		bulkBuilder.get();
	}
	
	
	/**
	 * Fetches list of items from queue
	 * @param size is the size of items to pull from queue
	 * @return An array of search hits
	 */
	private SearchHit[] getNextItems(Integer size){
		SearchResponse response = _client.prepareSearch()
			.setIndices(QUEUE_NAME)
			.setTypes(QUEUE_TYPE)
		 	.setSize(size)
		 	.setQuery(QueryBuilders.boolQuery()
		 		.must(QueryBuilders.functionScoreQuery()
		 			.add(ScoreFunctionBuilders
		 			.scriptFunction(new Script(SCRIPT_DEQUEUE, ScriptType.INDEXED, "groovy", null)))
		 			.boostMode("replace"))
		 		.filter(QueryBuilders.termQuery(FIELD_VISITED, false)))
		 	.addFields(FIELD_DISCOVERY_TIME)
		 	.get();
		
		return response.getHits().hits();
	}
	
	/**
	 * Removes requested search hits from the queue
	 * @param hits
	 * @throws Exception
	 */
	private void removeItems(SearchHit[] hits) throws Exception{
		if(hits.length == 0) return; 
		
		BulkRequestBuilder bulkBuilder = _client.prepareBulk();
		
		for(SearchHit hit : hits){
			String id = hit.getId();
			
			XContentBuilder builder = jsonBuilder()
			.startObject()
				.field(FIELD_VIS_DOMAIN_NAME, (new URL(id).getHost()))
				.field(FIELD_VISITED, true)
			.endObject();
			
			UpdateRequestBuilder request = _client.prepareUpdate()
				.setIndex(QUEUE_NAME)
				.setType(QUEUE_TYPE)
				.setId(id)
				.setDoc(builder);
			
			bulkBuilder.add(request);
		}
		
		bulkBuilder.get();
	}
	
}
