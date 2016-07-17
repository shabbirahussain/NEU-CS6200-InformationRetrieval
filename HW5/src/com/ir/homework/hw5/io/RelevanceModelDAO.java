package com.ir.homework.hw5.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.ir.homework.hw5.models.ModelRelevance;

public final class RelevanceModelDAO {
	private static final String FIELD_SPERATOR = "\t";
	
	/**
	 * Loads and builds the relevance model
	 * @param filePath is the full path of the relevance model
	 * @return Collection of RelevanceModels
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException if invalid file is provided
	 */
	public static Map<String, ModelRelevance> buildModel(String filePath) throws IOException, ArrayIndexOutOfBoundsException{
		Map<String, ModelRelevance> result = new HashMap<String, ModelRelevance>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		
		String line=null;
		while((line=br.readLine()) != null){
			// Read and parse fields
			String fields[]= line.split(FIELD_SPERATOR);
			String query = fields[0];
			String docID = fields[1];
			Double score = Double.parseDouble(fields[2]);
			
			// Store results into model
			ModelRelevance docMap = result.getOrDefault(query, new ModelRelevance());
			docMap.put(docID, score);
			result.put(query, docMap);
		}
		br.close();
		return result;
	}
}
