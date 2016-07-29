/**
 * 
 */
package com.ir.homework.hw7;

import static com.ir.homework.hw7.Constants.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw7.elasticclient.CachedElasticClient;
import com.ir.homework.hw7.elasticclient.ElasticClient;
import com.ir.homework.hw7.elasticclient.ElasticClientBuilder;
import com.ir.homework.hw7.io.ObjectStore;
import com.ir.homework.hw7.io.OutputWriter;
import com.ir.homework.hw7.io.QueryReader;
import com.ir.homework.hw7.io.ResultEvaluator;
import com.ir.homework.hw7.io.StopWordReader;
import com.ir.homework.hw7.models.*;
import com.ir.homework.hw7.util.QueryAugmentor;
/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static ElasticClient elasticClient, elasticClient1;
	private static ResultEvaluator resultEvaluator;
	private static QueryAugmentor queryAugmentor;
	private static StopWordReader stopWordReader;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start = System.nanoTime(); 
		List<SearchController> controllers = new LinkedList<SearchController>();
		
		
		System.out.println("Loading cache...");
		elasticClient = loadOrDefaultEC(TEXT_FIELD_NAME);	
//		elasticClient1 = loadOrDefaultEC("HEAD");
				
		//////////////////////// Controllers ////////////////////////////
		
		// OkapiTF
		controllers.add(new OkapiTFController(elasticClient));
		
		// TF-IDF
		controllers.add(new TF_IDFController(elasticClient));
		
		// OkapiBM25
		controllers.add(new OkapiBM25Controller(elasticClient));
		
		// UnigramLM LaplaceSmoothing
		controllers.add(new UnigramLM_LaplaceSmoothing(elasticClient));
		
		// UnigramLM Jelinek-Mercer smoothing
		controllers.add(new UnigramLM_JelinekMercer(elasticClient));
		
		// MetaSearchController
		controllers.add(new MetaSearchController(elasticClient, controllers));
		
		// Proximity Search 
		//controllers.add(new ProximitySearchController(elasticClient));
				
		
		////////////////////////////////////////////////////////////////
		
		stopWordReader  = new StopWordReader(STOP_WORDS_FILE_PATH);
		resultEvaluator = new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS);
		queryAugmentor  = new QueryAugmentor(elasticClient, stopWordReader.getStopWords());
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		for(SearchController sc : controllers){
			System.out.println("\n================ " + sc.getClass().getSimpleName() + " ==========================");
			execute(sc);
		}
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		//ObjectStore.saveObject(elasticClient, OBJECTSTORE_PATH);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
	/**
	 * Executes the search with given controller and prints output to controller specific file
	 * @param sc Search Controller to use
	 * @param queryFilePath
	 * @param outFilePath
	 * @return accuracy score from trec_eval
	 */
	public static Double execute(SearchController sc){
		Double result = null;
		String outFilePath  = OUTPUT_FILE_PATH + sc.getClass().getSimpleName() + ".txt";
		String outFilePathA = OUTPUT_FILE_PATH + sc.getClass().getSimpleName() + "_.txt";
		
		
		QueryReader  qr  = new QueryReader(QUERY_FILE_PATH);
		OutputWriter ow  = new OutputWriter( outFilePath);
		OutputWriter owA = new OutputWriter( outFilePathA);
		
		List<OutputWriter.OutputRecord> records;
		try {
			Map<String, String[]> queries=qr.getQueryTokens();
			for(Entry<String, String[]> q : queries.entrySet()){
				if(!(QUERY_NUMBER == null || QUERY_NUMBER == "") 
						&& !q.getKey().contains(QUERY_NUMBER))
					continue;
				
				// Remove stop words from query
				q = queryAugmentor.cleanStopWordsFromQuery(q);

				
				// additional query processing
				if(ENABLE_STEMMING) q = queryAugmentor.stemQuery(q);
				
				if(!ENABLE_SILENT_MODE) {
					System.out.print("Executing  Q:"+ q.getKey() + " [");
					for(String s: q.getValue()) System.out.print("," + s);
					System.out.println("]");
				}
				
				records = sc.executeQuery(q);
				for(int i=0; i<records.size() && i<MAX_RESULTS_OUTPUT; i++)
					ow.writeOutput(records.get(i));
				
				if(ENABLE_PSEUDO_FEEDBACK){
					// Augment the query terms
					q = queryAugmentor.cleanStopWordsFromQuery(
							queryAugmentor.escapeQuery(
								queryAugmentor.expandQuery(q)));
					
					for(String s: q.getValue()) System.out.print("," + s);
					System.out.println("]");
					
					records = sc.executeQuery(q);
					for(int i=0; i<records.size() && i<MAX_RESULTS_OUTPUT; i++)
						owA.writeOutput(records.get(i));
					
					//break;
				}
				if(EVALUATE_INDIVIDUAL_Q){
					ow.close(); owA.close();
					
					evaluate(outFilePath);
					if(ENABLE_PSEUDO_FEEDBACK){
						System.out.println("\nEnhanced Query:");
						evaluate(outFilePathA);
					}
				}
			}
			ow.close(); owA.close();
			
			evaluate(outFilePath);
			if(ENABLE_PSEUDO_FEEDBACK){
				System.out.println("\nEnhanced Query:");
				evaluate(outFilePathA);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Executes evaluations
	 * @param outFilePath is the full path of un-enhanced query output
	 * @return
	 * @throws IOException
	 */
	private static Double evaluate(String outFilePath) throws IOException{
		Double result = null;
		// run evaluation on output
		if(!ENABLE_SILENT_MODE) 
			System.out.println("\nRunning trec_eval on results["+ outFilePath + "]");
		
		result = resultEvaluator.runEvaluation(outFilePath, ENABLE_FULL_TREC_OUTPUT);
		System.out.println("");
		return result;
	}
	
	/**
	 * Tries to load last saved elasticclient
	 * @param textFieldName is the name of the text field to query
	 * @return ElasticClient object
	 * @throws UnknownHostException 
	 */
	private static ElasticClient loadOrDefaultEC(String textFieldName) throws UnknownHostException{
		ElasticClient result;
		ElasticClientBuilder eBuilder = ElasticClientBuilder.createElasticClientBuilder()
				.setClusterName(CLUSTER_NAME)
				.setHost(HOST)
				.setPort(PORT)
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setCustomFetch(ENABLE_HW2_CLIENT)
				.setField(textFieldName);

		try{
			result = (ElasticClient) ObjectStore.get(CachedElasticClient.class, OBJECTSTORE_PATH);
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Failed to load cache re running...");
			result = eBuilder.build();
		}
		eBuilder.build(result);
		return result;
	}
}
