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

import com.ir.homework.hw1.controllers.*;
import com.ir.homework.hw1.elasticutil.ElasticClient;
import com.ir.homework.hw1.elasticutil.ElasticClientBuilder;
import com.ir.homework.io.OutputWriter;
import com.ir.homework.io.QueryReader;
import com.ir.homework.io.ResultEvaluator;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static ElasticClient elasticClient;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.nanoTime(); 
		List<SearchController> controllers = new LinkedList<SearchController>();
		elasticClient = loadOrCreateCache(ElasticClient.class, OBJECT_STORE_PATH)
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setField(TEXT_FIELD_NAME)
				.setCache(ENABLE_TF_CACHE);
		
		
		
		//////////////////////// Controllers ////////////////////////////
		// OkapiTF
		controllers.add(new OkapiTFController(elasticClient));
		
		// TF-IDF
		controllers.add(new TF_IDFController(elasticClient));
		
		// OkapiBM25
		controllers.add(new OkapiBM25Controller(elasticClient));
		
		// UnigramLM_LaplaceSmoothing
		controllers.add(new UnigramLM_LaplaceSmoothing(elasticClient));
		
		
		// MetaSearchController
		controllers.add(new MetaSearchController(elasticClient, controllers));
		
		////////////////////////////////////////////////////////////////
		
		Double correctnessScore;
		for(SearchController sc : controllers){
			System.out.println("\n================ " + sc.getClass().getSimpleName() + " ==========================");
			correctnessScore = execute(sc);
			System.out.println("==> Correctness Score: " + correctnessScore);
		}
		
		
		if(ENABLE_PERSISTENT_CACHE){
			System.out.println("\n\nSaving cache for future use...");
			saveObject(elasticClient, OBJECT_STORE_PATH);
		}
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}
	
	/**
	 * Executes the search with given controller and prints output to controller specific file
	 * @param sc Search Controller to use
	 * @param queryFilePath
	 * @param outFilePath
	 * @return accutacy score from trec_eval
	 */
	public static Double execute(SearchController sc){
		Double result = null;
		String outFilePath = OUTPUT_FILE_PATH + sc.getClass().getSimpleName() + ".txt";
		
		QueryReader  qr = new QueryReader(QUERY_FILE_PATH, ENABLE_STEMMING);
		OutputWriter ow = new OutputWriter( outFilePath);
		
		List<OutputWriter.OutputRecord> records;
		try {
			ow.open();
			Map<String, String[]> queries=qr.getQueryTokens();
			for(Entry<String, String[]> q : queries.entrySet()){
				elasticClient.resetStatististics();
				if(!ENABLE_SILENT_MODE) System.out.print("Executing Q:"+ q.getKey());
				records = sc.executeQuery(q);
				for(OutputWriter.OutputRecord r: records)
					ow.writeOutput(r);
				if(!ENABLE_SILENT_MODE) System.out.println("\t CacheHits% = " + Math.round(100 * elasticClient.cacheHits / (elasticClient.cacheHits + elasticClient.cacheMiss)));
			}
			ow.close();
			
			
			// run evaluation on output
			if(!ENABLE_SILENT_MODE) System.out.println("\nRunning trec_eval on results["+ outFilePath + "]");
			result = (new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS, outFilePath)).runEvaluation(ENABLE_SILENT_MODE);
			
		} catch (ArrayIndexOutOfBoundsException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param storePath full path of directory where objects are stored
	 */
	private static void saveObject(Object object, String storePath){
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
	 * Creates a new search controller cache
	 * @return
	 */
	private static ElasticClient createSearchControllerCache(){
		System.out.println("Creating new cache...");
		ElasticClient result = ElasticClientBuilder.createElasticClient();
		return result;
	}
	
	/**
	 * Loads or creates a elasticClient
	 * @param c class of object to be loaded
	 * @param storePath full path of directory where objects are stored
	 * @return Uncasted object of given class fetched from store
	 */
	private static ElasticClient loadOrCreateCache(Class c, String storePath){
		ElasticClient result;
		if(ENABLE_PERSISTENT_CACHE){
			System.out.println("Loading cache from: " + storePath);
			result = (ElasticClient) loadObject(ElasticClient.class, storePath);
			if(result == null)
				result = createSearchControllerCache();
		}else{
			result = createSearchControllerCache();
		}
		return result;
	}
}
