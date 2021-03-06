/**
 * 
 */
package com.ir.homework.hw7.featureextraction;

import static com.ir.homework.hw7.featureextraction.Constants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.ir.homework.hw7.featureextraction.controllers.NGramFeatureExtractor;
import com.ir.homework.hw7.featureextraction.filters.FeatureFilter;
import com.ir.homework.hw7.featureextraction.filters.ListFeatureFilter;
import com.ir.homework.hw7.featureextraction.outputwritters.ARFFOutputWritter;
import com.ir.homework.hw7.featureextraction.outputwritters.CSVOutputWritter;
import com.ir.homework.hw7.featureextraction.outputwritters.OutputWritter;


/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static final DecimalFormat _percentFormat = new DecimalFormat("##.00%");
	private static final DateFormat    _dateFormat    = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static final String FIELD_LABEL = "Label";
	
	private static long start;
	private static TransportClient _client;
	private static List<FeatureExtractor> _ext;
	private static FeatureExtractor _extLab;
	
	private static List<FeatureFilter> _filters;
	private static List<String>  _docList;

	private static List<OutputWritter> _out;
	private static PrintStream _log = System.out;

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
		log("Info", "Initializing...");
		
		_ext= new LinkedList<>();
		////////// Create feature extractors //////////////////
		_extLab = (new LabelFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, FIELD_LABEL, 1.0));

//		_ext.add(new NGramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "TEXT", "my_shingle_analyzer"));
		_ext.add(new NGramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "TEXT"));
//		_ext.add(new NGramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "Content.Shingles"));
//		_ext.add(new NGramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "Content.Skipgrams"));
//		_ext.add(new NGramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "From"));
//		_ext.add(new NGramFeatureExtractor(_client, INDEX_NAME, INDEX_TYPE, "ContentType"));
		
		//////////////////////////////////////////////////////

		_filters = new LinkedList<>();
		///////// Create feature filters /////////////////////
		if(MANUAL_FEAT_LIST.length > 0)
			_filters.add(new ListFeatureFilter(_client, INDEX_NAME, INDEX_TYPE)
//					.addWhiteList(MANUAL_FEAT_LIST, "my_shingle_analyzer")
//					.addWhiteList(MANUAL_FEAT_LIST, "my_keyword")
//					.addWhiteList(MANUAL_FEAT_LIST, "my_skipgram_analyzer")
//					.addWhiteList(MANUAL_FEAT_LIST, "my_email_analyzer")
					.addWhiteList(MANUAL_FEAT_LIST, "my_english"));

		//////////////////////////////////////////////////////

		_out = new LinkedList<>();
		///////// Create output writers  /////////////////////
//		_out.add(new ARFFOutputWritter(FEAT_FILE_PATH, FEAT_JMODEL_FILE_PATH));
//		_out.add(new ARFFOutputWritter(FEAT_FILE_PATH, FEAT_JMODEL_FILE_PATH, true));
		
		_out.add(new CSVOutputWritter(FEAT_FILE_PATH));
		//////////////////////////////////////////////////////

		// Read all documents
		_docList = getDocumentList();
		log("Info", "Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		log("Info", "Extracting Features...");
		execute();
		log("Info", "Finalizing...");
		
		for(OutputWritter out : _out)
			out.close();
		
		log("Info", "Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
	/**
	 * Executes the feature extraction for document list
	 * @throws IOException 
	 */
	private static void execute() throws IOException{
		Long time = System.currentTimeMillis();
		Double cnt = 0.0;
		for(String docID: _docList){
			if((System.currentTimeMillis() - time)>Math.pow(10, 4)){
				time = System.currentTimeMillis();
				log("Info", "\t" + _percentFormat.format(cnt/_docList.size()) + " docs done" + "[" + cnt + "]");
			}
			
			Map<String, Double> result = new HashMap<String, Double>();
			for(FeatureExtractor e: _ext)
				result.putAll(e.getFeatures(docID));
			
			for(FeatureFilter f: _filters)
				result = f.applyFilters(result);
			
			for(OutputWritter out : _out)
				out.printResults(_extLab.getFeatures(docID).get(FIELD_LABEL), result);
			cnt++;
		}
	}
	
	
	/**
	 * Logs the message to the default logging location
	 * @param type is the type of message 
	 * @param message is the message to log
	 */
	private static void log(String type, String message){
		_log.print("[" + _dateFormat.format(new Date()) + "]");
		_log.print("[" + type + "]");
		_log.print(" " + message);
		_log.println();
	}
	
	/**
	 * Gets the document list from elastic search
	 * @return List of string containing document ids from elasticsearch
	 * @throws FileNotFoundException 
	 */
	private static List<String> getDocumentList() throws FileNotFoundException{
		List<String> result = new LinkedList<String>();
		
		PrintStream out = new PrintStream(FEAT_ID_FILE_PATH); 
		
		TimeValue scrollTimeValue = new TimeValue(60000);
		SearchResponse response = _client.prepareSearch()
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setSize(10000)
				.setScroll(scrollTimeValue)
				.setQuery(QueryBuilders.matchAllQuery())
				.setNoFields()
				.get();
		
		Integer cnt = 1;
		while(true){
			if((response.status() != RestStatus.OK) 
					|| (response.getHits().getHits().length == 0))
				break;
			
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				String id = h.getId();
				
				result.add(id);
				out.println((cnt++) + ", " + id);
			}
			
			// fetch next window
			response = _client.prepareSearchScroll(response.getScrollId())
					.setScroll(scrollTimeValue)
					.get();
//			break;
		}
		out.close();
		
		return result;
	}
}
