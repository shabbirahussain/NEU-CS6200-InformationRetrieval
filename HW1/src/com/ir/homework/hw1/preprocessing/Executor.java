/**
 * 
 */
package com.ir.homework.hw1.preprocessing;

import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.elasticclient.ElasticClientBuilder;
import static com.ir.homework.hw1.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static long start;
	/**
	 * @param args 
	 */
	public static void main(String[] args){
		start = System.nanoTime(); 
		
		// Data loader
		ElasticClient elasticClient = ElasticClientBuilder.createElasticClientBuilder()
			.setClusterName(CLUSTER_NAME)
			.setHost(HOST)
			.setPort(PORT)
			.setIndices(INDEX_NAME)
			.setTypes(INDEX_TYPE)
			.setLimit(MAX_RESULTS)
			.build();

		try {
			
			System.out.println("\n\nPreprocessing File...");
			//(new FilePreprocessor(PRE_PROCESS_SRC_PATH, PRE_PROCESS_DST_PATH, DATA_FILE_PREFIX)).preProcessFiles();
			System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
			
			System.out.println("\n\nLoading Data...");
			Long result = (new FileLoader(elasticClient, PRE_PROCESS_DST_PATH, DATA_FILE_PREFIX))
					.startLoad();
			
			System.out.println("Num of records inserted=" + result);
			System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
			
		} catch (Exception e) {e.printStackTrace();}
		
	}

}
