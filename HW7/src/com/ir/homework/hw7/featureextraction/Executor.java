/**
 * 
 */
package com.ir.homework.hw7.featureextraction;

import static com.ir.homework.hw7.featureextraction.Constants.*;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;

import com.ir.homework.hw7.featureextraction.controllers.FeatureExtractor;
import com.ir.homework.hw7.featureextraction.controllers.LabelFeatureExtractor;
import com.ir.homework.hw7.featureextraction.controllers.SkipgramFeatureExtractor;
import com.ir.homework.hw7.featureextraction.controllers.UnigramFeatureExtractor;
import com.ir.homework.hw7.featureextraction.filters.FeatureFilter;
import com.ir.homework.hw7.featureextraction.filters.ListFeatureFilter;


/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static long start;
	private static TransportClient _client;
	private static List<FeatureExtractor> _ext;
	private static List<FeatureFilter> _filters;
	private static List<String>  _docList;

	private static PrintStream _out;

	// Create elasticsearch Client
	static{
		Builder settings = Settings.settingsBuilder()
				.put("client.transport.ignore_cluster_name", false)
		        .put("node.client", true)
		        .put("client.transport.sniff", true)
				.put("cluster.name", CLUSTER_NAME);

		try {
			_client = TransportClient.builder().settings(settings.build()).build()
					.addTransportAddress(new InetSocketTransportAddress(
										InetAddress.getByName(HOST), PORT));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		start = System.nanoTime(); 
		System.out.println("Initializing...");

		_ext= new LinkedList<FeatureExtractor>();
		////////// Create feature extractors //////////////////
		_ext.add(new LabelFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "Label"));
		_ext.add(new UnigramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "Content"));
		_ext.add(new SkipgramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "Content.Shingles"));
		
		//////////////////////////////////////////////////////
		
		// Create feature filters
		_filters = new LinkedList<FeatureFilter>();
		_filters.add(new ListFeatureFilter(_client, INDEX_NAME, INDEX_TYPE)
				.addWhiteList(MANUAL_FEAT_LIST, "my_shingle_analyzer")
				.addWhiteList(MANUAL_FEAT_LIST, "my_english"));
		
		
		// Read all documents
		_docList = getDocumentList();
		_out = new PrintStream(FEAT_FILE_PATH);
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		
		System.out.println("Extracting Features...");
		execute();
		
		_out.close();
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
	/**
	 * Executes the feature extraction for document list
	 */
	private static void execute(){
		for(String docID: _docList){
			Map<String, Double> result = new HashMap<String, Double>();
			for(FeatureExtractor e: _ext)
				result.putAll(e.getFeatures(docID));
			
			
			for(FeatureFilter f: _filters)
				result = f.applyFilters(result);
			
			printResults(result);
		}
	}
	
	/**
	 * Formats and outputs the feature map to the pritnstream
	 * @param featMap is the feature map to print
	 */
	private static void printResults(Map<String, Double> featMap){
		_out.println(featMap);
	}
	
	/**
	 * Gets the document list from elastic search
	 * @return List of string containing document ids from elasticsearch
	 */
	private static List<String> getDocumentList(){
		List<String> result = new LinkedList<String>();
		
		TimeValue scrollTimeValue = new TimeValue(60000);
		SearchResponse response = _client.prepareSearch()
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setSize(10000)
				.setScroll(scrollTimeValue)
				.setQuery(QueryBuilders.matchAllQuery())
				.setNoFields()
				.get();
		
		while(true){
			if((response.status() != RestStatus.OK) 
					|| (response.getHits().getHits().length == 0))
				break;
			
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				result.add(h.getId());
			}
			
			// fetch next window
			response = _client.prepareSearchScroll(response.getScrollId())
					.setScroll(scrollTimeValue)
					.get();
			
		}
		return result;
	}
}
