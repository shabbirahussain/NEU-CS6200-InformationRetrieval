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
		Map<String, Map<String, Double>> qDocMap = new HashMap<String, Map<String, Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		
		String line=null;
		while((line=br.readLine()) != null){
			// Read and parse fields
			String fields[]= line.split(FIELD_SPERATOR);
			String query = fields[0];
			String docID = fields[1];
			Double score = Double.parseDouble(fields[2]);
			
			// Store results into model
			Map<String, Double> docMap = qDocMap.getOrDefault(query, new HashMap<String, Double>());
			docMap.put(docID, score);
			qDocMap.put(query, docMap);
		}
		Map<String, ModelRelevance> result = new HashMap<String, ModelRelevance>();
		
		for(Entry<String, Map<String, Double>> e: qDocMap.entrySet()){
			result.put(e.getKey(), (ModelRelevance) sortByValue(e.getValue()));
		}
		
		br.close();
		return result;
	}
	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <K, V extends Number> List<Entry<K, V>> sortByValue(Map<K,V> map) {
	     List<Entry<K, V>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
}
