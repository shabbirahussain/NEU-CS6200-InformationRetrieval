package com.ir.homework.hw3.elasticclient;

import static com.ir.homework.hw3.Constants.*;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.Flushable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;

import com.google.common.net.InternetDomainName;
import com.ir.homework.hw3.tools.WebPageParser.ParsedWebPage;

public class ElasticClient implements Flushable{
	private static Client _client = null;
	private static DateFormat _dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
	private static Random random = new Random();
	
	// Local buffers
	private BulkRequestBuilder loadDataBuffer; // Stores the requests for loading data
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
		
		_client = TransportClient.builder()
				.settings(settings.build())
				.build()
		        .addTransportAddress(
		        		new InetSocketTransportAddress(InetAddress.getByName(HOST), PORT));
		loadDataBuffer  = _client.prepareBulk();
		
		enqueueBuffer = new HashMap<String, Map<String, Object>>(MAX_BUFFER_SIZE);
	}
	
	// --------------------------- Loaders ----------------------------
	/**
	 * Loads data into elasticsearch
	 * @param id is the id of the document
	 * @param ParsedWebPage is the object containing parsed webpage response
	 * @throws IOException
	 */
	public synchronized void loadData(String id, ParsedWebPage parsedWebPage) throws IOException{
		
		if(this.loadDataBuffer.numberOfActions() > MAX_BUFFER_SIZE){
			System.out.println("[X] Storing results...");
			this.flush();
		}
		
		XContentBuilder source = jsonBuilder()
			.startObject()
				.field(FIELD_HTML, parsedWebPage.html)
				.field(FIELD_HTTP_HEADERS, parsedWebPage.headers)
				.field(FIELD_TEXT, parsedWebPage.text)
				.field(FIELD_TITLE, parsedWebPage.title)
				.field(FIELD_DT_UPDATED, _dateFormat.format(new Date()))
			.endObject();
		
		IndexRequestBuilder irBuilder = _client.prepareIndex()
				.setIndex(INDEX_NAME)
				.setType(INDEX_TYPE)
				.setId(id)
				.setSource(source);
		
		loadDataBuffer.add(irBuilder);
		
		
		// Store link map
		for(URL link : parsedWebPage.outLinks){
			String dstLink = link.toString();
			String mapId = id + "#" + dstLink;
			
			source = jsonBuilder()
					.startObject()
						.field(FIELD_SRC_LINK, id)
						.field(FIELD_DST_LINK, dstLink)
					.endObject();
			
			irBuilder = _client.prepareIndex()
					.setIndex(LINK_MAP_NAME)
					.setType(LINK_MAP_TYPE)
					.setId(mapId)
					.setSource(source);
			
			loadDataBuffer.add(irBuilder);
		}
		return;
	}
	
	@Override
	public synchronized void flush(){
		// load all buffered documents if any
		
		if(this.loadDataBuffer.numberOfActions() > 0){
			BulkResponse response = loadDataBuffer.get();
			if(response.hasFailures())
				System.err.println(response.buildFailureMessage());
			
			loadDataBuffer = _client.prepareBulk();
		}
		
		if(this.enqueueBuffer.size() > 0){
			this.enqueue(this.enqueueBuffer);
			this.enqueueBuffer = new HashMap<String, Map<String, Object>>(MAX_BUFFER_SIZE);
		}
	}
	
	/**
	 * Dequeues n elements from elastic search queue
	 * @param size is the number of elements to be dequeued
	 * @return A collection of items from queue
	 * @throws Exception
	 */
	public synchronized SearchHit[] dequeue(Integer size) throws Exception{
		SearchHit[] hits = this.getNextItems(size);
		
		List<SearchHit> newHits = new LinkedList<SearchHit>();
 		for(SearchHit hit : hits){
			if(random.nextDouble() < DEQUEUE_RND_PERCENT)
				newHits.add(hit);
		}
		hits = newHits.toArray(new SearchHit[0]);
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
		String urlStr = url.toString();
		Map<String, Object> params = this.enqueueBuffer.getOrDefault(urlStr, new HashMap<String, Object>(DEFAULT_QUEUE_FIELDS));
		
		score             = Math.max(score, (Float) params.get(FIELD_PARENT_SCORE));
		Integer inCnt     = (Integer) params.get(FIELD_IN_CNT) + 1 ;
		discoveryTime     = Math.min(discoveryTime, (Integer) params.get(FIELD_DISCOVERY_TIME) + 1 );
		String domainName = InternetDomainName.from(url.getHost()).topPrivateDomain().toString();
		
		params.put(FIELD_PARENT_SCORE, score);
		params.put(FIELD_IN_CNT, inCnt);
		params.put(FIELD_DISCOVERY_TIME, discoveryTime);
		params.put(FIELD_DOMAIN_NAME, domainName);
			
		this.enqueueBuffer.put(urlStr, params);
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
		 	.addFields(FIELD_DOMAIN_NAME)
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
			String domainName = hit.getFields().get(FIELD_DOMAIN_NAME).value();
			
			XContentBuilder builder = jsonBuilder()
			.startObject()
				.field(FIELD_VIS_DOMAIN_NAME, domainName)
				.field(FIELD_VISITED, true)
				.field(FIELD_VISITED_DATE,  _dateFormat.format(new Date()))
			.endObject();
			
			UpdateRequestBuilder request = _client.prepareUpdate()
				.setIndex(QUEUE_NAME)
				.setType(QUEUE_TYPE)
				.setId(id)
				//.setFields(FIELD_DISCOVERY_TIME)
				.setDoc(builder);
			
			bulkBuilder.add(request);
		}
		
		bulkBuilder.get().getItems();
	}
	
	
	
	/**
	 * Truncates additional records in frontier queue
	 * @throws IOException 
	 */
	public void truncateQueue() throws IOException{
		TimeValue scrollTimeValue = new TimeValue(60000);
		SearchResponse response = _client.prepareSearch()
				.setIndices(QUEUE_NAME)
				.setTypes(QUEUE_TYPE)
			 	.setSize(MAX_QUEUE_SIZE)
			 	.setQuery(QueryBuilders.boolQuery()
			 		.must(QueryBuilders.functionScoreQuery()
			 			.add(ScoreFunctionBuilders
			 			.scriptFunction(new Script(SCRIPT_DEQUEUE, ScriptType.INDEXED, "groovy", null)))
			 			.boostMode("replace"))
			 		.filter(QueryBuilders.termQuery(FIELD_VISITED, false)))
			 	.addFields(FIELD_DISCOVERY_TIME)
		        .setScroll(scrollTimeValue)
			 	.get();
		
		BulkRequestBuilder bulkBuilder = _client.prepareBulk();
		XContentBuilder builder = jsonBuilder()
				.startObject()
					.field(FIELD_VIS_DOMAIN_NAME, "")
					.field(FIELD_VISITED, true)
				.endObject();
		
		// Scan for results
		while(true){
			response = _client.prepareSearchScroll(response.getScrollId())
					.setScroll(scrollTimeValue)
					.get();
			
			if((response.status() != RestStatus.OK) 
					|| (response.getHits().getHits().length == 0))
				break;
			
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				UpdateRequestBuilder request = _client.prepareUpdate()
					.setIndex(QUEUE_NAME)
					.setType(QUEUE_TYPE)
					.setId(h.getId())
					.setDoc(builder);
				
				bulkBuilder.add(request);
			}
			
		}
		if(bulkBuilder.numberOfActions()>0)
			bulkBuilder.get();
	}
}
