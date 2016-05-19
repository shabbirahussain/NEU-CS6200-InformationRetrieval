package com.ir.homework.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.ir.homework.common.Constants.*;

public final class QueryReader {
	private String qPath;
	
	/**
	 * Reads the queries and creates tokens out of them 
	 * @return Map with query id and tokens
	 * @throws IOException 
	 * @throws ArrayIndexOutOfBoundsException invalid query file given
	 */
	public Map<String, String[]> getQueryTokens() throws IOException, ArrayIndexOutOfBoundsException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(qPath)));
		
		Map<String, String[]> output = new HashMap<String, String[]>();
		String line;
		while((line=br.readLine())!= null){
			String split[]   = line.split("\\.", 2);
			if(split.length==2){
				String queryId   = split[0];
				String tokens[]  = createTokens(split[1]);
				output.put(queryId, tokens);
			}
		}
		br.close();
		return output;
	}
	
	/**
	 * Creates an object of Query reader with specified file path
	 * @param qPath
	 */
	public QueryReader(String qPath){
		this.qPath = qPath;
	}

	/**
	 * Creates tokens out of given query
	 * @param query
	 * @return
	 */
	private static String[] createTokens(String query){
		return query.split("(?i)[^a-z|0-9]");
	}
	
	
	/**
	 * Main function for testing only
	 * @param args
	 */
	public static void main(String args[]){
		try{
			(new QueryReader(QUERY_FILE_PATH)).getQueryTokens();
		}catch(Exception e){e.printStackTrace();}
	}
}
