/**
 * 
 */
package com.ir.homework.hw1;

import static com.ir.homework.hw1.Constants.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.ir.homework.hw1.elasticclient.CachedElasticClient;
import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.elasticclient.ElasticClientBuilder;
import com.ir.homework.hw1.io.OutputWriter;
import com.ir.homework.hw1.io.QueryReader;
import com.ir.homework.hw1.io.ResultEvaluator;
import com.ir.homework.hw1.io.StopWordReader;
import com.ir.homework.hw1.models.*;
import com.ir.homework.hw1.util.QueryAugmentor;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static ElasticClient elasticClient;
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

		elasticClient = loadOrCreateCache(CachedElasticClient.class, OBJECT_STORE_PATH);
		
		
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
		
		////////////////////////////////////////////////////////////////
		
		stopWordReader  = new StopWordReader(STOP_WORDS_FILE_PATH);
		resultEvaluator = new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS);
		queryAugmentor  = new QueryAugmentor(elasticClient, stopWordReader.geStopWords());
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		Double correctnessScore;
		for(SearchController sc : controllers){
			System.out.println("\n================ " + sc.getClass().getSimpleName() + " ==========================");
			correctnessScore = execute(sc);
		}
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		saveObject(elasticClient, OBJECT_STORE_PATH);
		
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
			ow.open();
			owA.open();
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
					if(!ENABLE_SILENT_MODE) 
						System.out.print("Executing PQ:"+ q.getKey() + " [");
					
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
					evaluate(outFilePath, outFilePathA);
					ow.open(); owA.open();
				}
				
				//*/
			}
			ow.close(); owA.close();
			
			evaluate(outFilePath, outFilePathA);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Executes evaluations
	 * @param outFilePath is the full path of un-enhanced query output
	 * @param outFilePathA is the full path of enhanced query output
	 * @return
	 * @throws IOException
	 */
	private static Double evaluate(String outFilePath, String outFilePathA) throws IOException{
		Double result = null;
		// run evaluation on output
		if(!ENABLE_SILENT_MODE) 
			System.out.println("\nRunning trec_eval on results["+ outFilePath + "]");
		
		result = resultEvaluator.runEvaluation(outFilePath, ENABLE_FULL_TREC_OUTPUT);
		if(ENABLE_PSEUDO_FEEDBACK){
			System.out.println("\nEnhanced Query:");
			result = resultEvaluator.runEvaluation(outFilePathA, ENABLE_FULL_TREC_OUTPUT);
		}
		System.out.println("");
		return result;
	}
	
	/**
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param storePath full path of directory where objects are stored
	 */
	private static void saveObject(Object object, String storePath){
		if(!ENABLE_PERSISTENT_CACHE) return;
		System.out.println("\n\nSaving cache for future use...");
		
		String fullFilePath = storePath + object.getClass().getName() + ".ser";
		
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fullFilePath));
			oos.writeObject(object);
			oos.close();
		} catch (IOException e) {e.printStackTrace();}
		return;
	}
	
	/**
	 * 
	 * @param c class of object to be loaded
	 * @param storePath full path of directory where objects are stored
	 * @return Uncasted object of given class fetched from store
	 */
	private static Object loadObject(Class c, String storePath){
		String fullFilePath = storePath + c.getName() + ".ser";
		Object result;
		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullFilePath));
			result = ois.readObject();
			ois.close();
			
			return result;
		}catch(ClassNotFoundException | IOException e){}
		return null;
	}
	
	
	/**
	 * Loads or creates a elasticClient
	 * @param c class of object to be loaded
	 * @param storePath full path of directory where objects are stored
	 * @return Uncasted object of given class fetched from store
	 */
	private static ElasticClient loadOrCreateCache(Class<?> c, String storePath){
		ElasticClient result = null;
		ElasticClientBuilder eBuilder = ElasticClientBuilder.createElasticClientBuilder()
				.setClusterName(CLUSTER_NAME)
				.setHost(HOST)
				.setPort(PORT)
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setCachedFetch(ENABLE_PERSISTENT_CACHE)
				.setField(TEXT_FIELD_NAME);
		
		if(ENABLE_PERSISTENT_CACHE){
			System.out.println("Loading cache from: " + storePath);
			result = (ElasticClient) loadObject(c, storePath);	
		}
		if(result != null) 
			result = eBuilder.build(result);
		else
			result = eBuilder.build();
			
		return result;
	}
}