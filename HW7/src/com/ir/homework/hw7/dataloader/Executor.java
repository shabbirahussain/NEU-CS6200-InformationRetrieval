/**
 * 
 */
package com.ir.homework.hw7.dataloader;

import static com.ir.homework.hw7.dataloader.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.ir.homework.hw7.dataloader.io.FileLoader;
import com.ir.homework.hw7.dataloader.io.ModelQrelDAO;
import com.ir.homework.hw7.dataloader.models.ModelQrel;
import com.ir.homework.hw7.dataloader.parsers.MimeFileParser;
import com.ir.homework.hw7.dataloader.parsers.Parser;
import com.ir.homework.hw7.elasticclient.ElasticClient;
import com.ir.homework.hw7.elasticclient.ElasticClientBuilder;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static long start;
	private static ElasticClient _elasticClient;
	private static Parser _parser;
	
	/**
	 * @param args 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		start = System.nanoTime(); 
		
		// Initialize
		ElasticClient elasticClient = ElasticClientBuilder.createElasticClientBuilder()
			.setClusterName(CLUSTER_NAME)
			.setHost(HOST)
			.setPort(PORT)
			.setIndices(INDEX_NAME)
			.setTypes(INDEX_TYPE)
			.setLimit(MAX_RESULTS)
			.build();

		
		Map<String, ModelQrel> qrel = ModelQrelDAO.readModel(QREL_PATH);
		//System.out.println(qrel.get("0").size());
		
		
		Parser parser = new MimeFileParser(qrel);
		FileLoader fileLoader = new FileLoader(parser, elasticClient);
		
		// Start load
		Long result = fileLoader.startLoad("0");
		
		//System.out.println("Num of records inserted=" + result);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
	
}
