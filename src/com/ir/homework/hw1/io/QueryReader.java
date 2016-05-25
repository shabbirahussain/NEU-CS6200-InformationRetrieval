package com.ir.homework.hw1.io;

import static com.ir.homework.hw1.Constants.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import opennlp.tools.stemmer.PorterStemmer;


public final class QueryReader {
	private String qPath;
	
	/**
	 * Creates an object of Query reader with specified file path
	 * @param qPath
	 */
	public QueryReader(String qPath){
		this.qPath = qPath;
	}
	
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
				String tokens[]  = createTokens(split[1].toLowerCase());
				List<String> cleanTokens = new LinkedList<String>();
				
				for(int i=0;i<tokens.length; i++){
					// Remove blank terms
					if(tokens[i].trim().length()>0) {
						cleanTokens.add(tokens[i]);
					}
				}
				tokens = cleanTokens.toArray(new String[0]);
				output.put(queryId, tokens);
			}
		}
		br.close();
		return output;
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
