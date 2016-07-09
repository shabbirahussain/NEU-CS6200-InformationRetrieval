package com.ir.homework.hw4.elasticclient;

import static com.ir.homework.hw4.Constants.*;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.Flushable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
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
import org.elasticsearch.search.SearchHitField;

import com.ir.homework.hw4.models.LinkInfo;


public class ElasticClient implements Flushable{
	private static final Integer d = 200;
	private static Client _client = null;
	private static Random random = new Random();

	// Local buffers
	private BulkRequestBuilder loadDataBuffer; // Stores the requests for loading data
	
	
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
	}
	
	// --------------------------- Loaders ----------------------------
	/**
	 * Loads data into elasticsearch
	 * @param inLink is the src link of the document
	 * @param dstLink is the target document id
	 * @return Number of instructions in the buffer
	 * @throws IOException
	 */
	public synchronized Integer loadLinks(String inLink, String dstLink) throws IOException{
		// Store link map
		String mapId = inLink + "#" + dstLink;
			
		XContentBuilder source = jsonBuilder()
				.startObject()
					.field(FIELD_SRC_LINK, inLink)
					.field(FIELD_DST_LINK, dstLink)
				.endObject();
		
		IndexRequestBuilder irBuilder = _client.prepareIndex()
				.setIndex(LINK_MAP_NAME)
				.setType(LINK_MAP_TYPE)
				.setId(mapId)
				.setSource(source);
		
		loadDataBuffer.add(irBuilder);
		
		return this.loadDataBuffer.numberOfActions();
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
		
	}
	
	/**
	 * Loads the connectivity matrix from elastic search
	 * @return Links connectivity matrix as map
	 */
	public Map<String, LinkInfo> loadLinksMap(){
		Map<String, LinkInfo> result = new HashMap<String, LinkInfo>();
		
		TimeValue scrollTimeValue = new TimeValue(60000);
		
		SearchRequestBuilder builder = _client.prepareSearch()
				.setIndices(LINK_MAP_NAME)
				.setTypes(LINK_MAP_TYPE)
				.addFields(FIELD_SRC_LINK, FIELD_DST_LINK)
				.setSize(10000)
				.setScroll(scrollTimeValue);
		
		SearchResponse response = builder.get();
		LinkInfo info;
		
		DecimalFormat f = new DecimalFormat("##.00");
		Long tot = response.getHits().getTotalHits();
		Long cnt = 0L;
		while(true){
			cnt += response.getHits().getHits().length;
			System.out.println(f.format(cnt*100.0/tot) + "%");
			
			if((response.status() != RestStatus.OK) || (response.getHits().getHits().length == 0))
				break;
			
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				
				String dst = h.getFields().get(FIELD_DST_LINK).getValue();
				SearchHitField shf = h.getFields().get(FIELD_SRC_LINK);
				
				info = result.getOrDefault(dst, new LinkInfo());
				if(shf != null) {
					String src = shf.getValue();
					info.M.add(src);
				
					// Add outlinks count
					info = result.getOrDefault(src, new LinkInfo());
					info.L.add(dst);
					result.put(src, info);
				}
				// Add in links map
				result.put(dst, info);
			}
			
			// fetch next window
			response = _client.prepareSearchScroll(response.getScrollId())
					.setScroll(scrollTimeValue)
					.get();
		}
		
		return result;
	}
	
	/**
	 * Loads the connectivity matrix from elastic search
	 * @return Links connectivity matrix as map
	 */
	public Map<String, LinkInfo> loadMapFromRootSet(){
		Map<String, LinkInfo> result = new HashMap<String, LinkInfo>();
		LinkInfo info;
		
		SearchResponse response = _client.prepareSearch()
				.setIndices(DAT_IDX_NAME)
				.setTypes(DAT_IDX_TYPE)
				.setQuery(QueryBuilders.matchQuery(FIELD_TEXT, QUERY_TERMS))
				.setNoFields()
				.setSize(1)
				.get();
		
		List<String> rootSet = new LinkedList<String>();
		SearchHit hit[] = response.getHits().hits();
		for(SearchHit h:hit){
			String id = h.getId();
			rootSet.add(id); // Add base document to root set;
			
			// All outlinks
			SearchResponse res2 = _client.prepareSearch()
					.setIndices(LINK_MAP_NAME)
					.setTypes(LINK_MAP_TYPE)
					.setQuery(QueryBuilders.termQuery(FIELD_SRC_LINK, id)) // All pages it points to
					.addFields(FIELD_SRC_LINK, FIELD_DST_LINK)
					.setSize(10000)
					.get();
			System.out.println(id);
			System.out.println(res2.getHits().getTotalHits());
			SearchHit hit2[] = res2.getHits().hits();
			for(SearchHit h2:hit2){
				rootSet.add(h2.getFields().get(FIELD_DST_LINK).getValue());
				
				
				///////////////////////////////////////////////////////////
				String dst = h2.getFields().get(FIELD_DST_LINK).getValue();
				SearchHitField shf = h2.getFields().get(FIELD_SRC_LINK);
				
				info = result.getOrDefault(dst, new LinkInfo());
				if(shf != null) {
					String src = shf.getValue();
					info.M.add(src);
				
					// Add outlinks count
					info = result.getOrDefault(src, new LinkInfo());
					info.L.add(dst);
					result.put(src, info);
				}
				// Add in links map
				result.put(dst, info);
			}
			
			// All inlinks
			SearchResponse res3 = _client.prepareSearch()
					.setIndices(LINK_MAP_NAME)
					.setTypes(LINK_MAP_TYPE)
					.setQuery(QueryBuilders.termQuery(FIELD_DST_LINK, id)) 
					.addFields(FIELD_SRC_LINK, FIELD_DST_LINK)
					.setSize(10000)
					.get();
			
			SearchHit hit3[] = res3.getHits().hits();
			Double threshold = d.doubleValue() / hit3.length;
			for(SearchHit h3:hit3){
				if(random.nextDouble() < threshold){
					rootSet.add(h3.getFields().get(FIELD_SRC_LINK).getValue());
					
					///////////////////////////////////////////////////////////
					String dst = h3.getFields().get(FIELD_DST_LINK).getValue();
					SearchHitField shf = h3.getFields().get(FIELD_SRC_LINK);
					
					info = result.getOrDefault(dst, new LinkInfo());
					if(shf != null) {
					String src = shf.getValue();
					info.M.add(src);
					
					// Add outlinks count
					info = result.getOrDefault(src, new LinkInfo());
					info.L.add(dst);
					result.put(src, info);
					}
					// Add in links map
					result.put(dst, info);
				}
			}
		}
		return result;
	}
	// ----------------------------------------------------------------
	
	public static void main(String args[]) throws UnknownHostException{
		System.out.println((new ElasticClient()).loadMapFromRootSet().size());
	}
}
