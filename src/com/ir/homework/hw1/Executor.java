/**
 * 
 */
package com.ir.homework.hw1;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.controllers.*;
import com.ir.homework.io.OutputWriter;
import com.ir.homework.io.QueryReader;

import static com.ir.homework.common.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static SearchController sc;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		QueryReader  qr = new QueryReader(QUERY_FILE_PATH);
		OutputWriter ow = new OutputWriter(OUTPUT_FILE_PATH);
		
		sc = new OkapiTFController(INDEX_NAME, INDEX_TYPE);
		
		
		
		OutputWriter.OutputRecord record;
		try {
			ow.open();
			Map<String, String[]> queries=qr.getQueryTokens();
			for(Entry<String, String[]> q : queries.entrySet()){
				record = sc.executeQuery(q);
				ow.writeOutput(record);
			}
			ow.close();
		} catch (ArrayIndexOutOfBoundsException | IOException e) {
			e.printStackTrace();
		}
	}

}
