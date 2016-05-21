/**
 * 
 */
package com.ir.homework.hw1;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.controllers.*;
import com.ir.homework.hw1.controllers.util.SearchControllerCache;
import com.ir.homework.io.OutputWriter;
import com.ir.homework.io.QueryReader;

import static com.ir.homework.common.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static SearchControllerCache searchCache;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.nanoTime(); 
		
		searchCache = new SearchControllerCache(INDEX_NAME, INDEX_TYPE, MAX_RESULTS, TEXT_FIELD_NAME);
		
		
		
		////////////////// Controllers //////////////////////////////////
		SearchController sc = new OkapiTFController(searchCache, MAX_RESULTS);
		
		
		execute(sc, QUERY_FILE_PATH, OUTPUT_FILE_PATH);
		
		
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}
	
	/**
	 * Executes the search with given controller and prints output to controller specific file
	 * @param sc Search Controller to use
	 * @param queryFilePath
	 * @param outFilePath
	 */
	public static void execute(SearchController sc, String queryFilePath, String outFilePath){
		outFilePath = outFilePath + sc.getClass().getSimpleName() + ".txt";
		
		QueryReader  qr = new QueryReader(queryFilePath);
		OutputWriter ow = new OutputWriter( outFilePath);
		
		List<OutputWriter.OutputRecord> records;
		try {
			ow.open();
			Map<String, String[]> queries=qr.getQueryTokens();
			for(Entry<String, String[]> q : queries.entrySet()){
				System.out.println("Executing Q:"+ q.getKey());
				records = sc.executeQuery(q);
				System.out.println("Found "+ records.size() + " results.");
				for(OutputWriter.OutputRecord r: records)
					ow.writeOutput(r);
				
			}
			ow.close();
		} catch (ArrayIndexOutOfBoundsException | IOException e) {
			e.printStackTrace();
		}
	}
}
