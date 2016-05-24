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
import com.ir.homework.hw1.elasticclient.CachedElasticClient;
import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.elasticclient.ElasticClientBuilder;
import com.ir.homework.hw1.io.OutputWriter;
import com.ir.homework.hw1.io.QueryReader;
import com.ir.homework.hw1.io.ResultEvaluator;

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

		elasticClient = loadOrCreateCache(CachedElasticClient.class, OBJECT_STORE_PATH);
		
		
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
	 * @return accuracy score from trec_eval
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
				if(!ENABLE_SILENT_MODE) System.out.println("Executing Q:"+ q.getKey());
				records = sc.executeQuery(q);
				for(OutputWriter.OutputRecord r: records)
					ow.writeOutput(r);
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
	 * @return New instance of elastic client
	 */
	private static ElasticClient createElasticClient(){
		System.out.println("Creating new elastic client...");
		
		ElasticClient result = ElasticClientBuilder.createElasticClientBuilder()
				.setClusterName(CLUSTER_NAME)
				.setHost(HOST)
				.setPort(PORT)
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setCachedFetch(ENABLE_PERSISTENT_CACHE)
				.setField(TEXT_FIELD_NAME)
				.build();
		
		return result;
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
