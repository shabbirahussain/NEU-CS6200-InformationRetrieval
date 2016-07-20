package com.ir.homework.hw5.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw5.models.ModelQrel;

public final class ModelQrelDAO {
	private static final String FIELD_SPERATOR = "\t|\\s";
	
	/**
	 * Loads and builds the relevance model
	 * @param filePath is the full path of the relevance model
	 * @return Collection of RelevanceModels
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException if invalid file is provided
	 */
	public static Map<String, ModelQrel> readModel(String filePath) throws IOException, ArrayIndexOutOfBoundsException{
		Map<String, Map<String, Double>> qDocMap = new HashMap<String, Map<String, Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		
		String line=null;
		while((line=br.readLine()) != null){
			// Read and parse fields
			String fields[]= line.split(FIELD_SPERATOR);
			String query = fields[0];
			String docID = fields[2];
			Double score = Double.parseDouble(fields[3]);
			
			if(score == 0.0) continue;
			// Store results into model
			Map<String, Double> docMap = qDocMap.getOrDefault(query, new ModelQrel());
			docMap.put(docID, score);
			qDocMap.put(query, docMap);
		}
		Map<String, ModelQrel> result = new HashMap<String, ModelQrel>();
		
		for(Entry<String, Map<String, Double>> e: qDocMap.entrySet()){
			result.put(e.getKey(), (ModelQrel) e.getValue()); //sortByValue(e.getValue()));
		}
		
		br.close();
		return result;
	}
}
