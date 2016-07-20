package com.ir.homework.hw5.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw5.models.ModelQrel;
import com.ir.homework.hw5.models.ModelQres;
import static com.ir.homework.hw5.Constants.*;

public final class ModelQresDAO {
	private static final String FIELD_SPERATOR = "\t|\\s";
	
	/**
	 * Loads and builds the relevance model
	 * @param filePath is the full path of the relevance model
	 * @return Collection of RelevanceModels
	 * @throws IOException
	 * @throws ArrayIndexOutOfBoundsException if invalid file is provided
	 */
	public static Map<String, ModelQres> readModel(String filePath) throws IOException, ArrayIndexOutOfBoundsException{
		Map<String, Map<String, Double>> qDocMap = new HashMap<String, Map<String, Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		
		String line=null;
		while((line=br.readLine()) != null){
			// Read and parse fields
			String fields[]= line.split(FIELD_SPERATOR);
			String query = fields[0];
			String docID = fields[2];
			Double score = Double.parseDouble(fields[4]);
			
			//score = (score>MAX_GRADE)?MAX_GRADE:score;
			
			// Store results into model
			Map<String, Double> docMap = qDocMap.getOrDefault(query, new ModelQrel());
			docMap.put(docID, score);
			qDocMap.put(query, docMap);
		}
		Map<String, ModelQres> result = new HashMap<String, ModelQres>();
		
		for(Entry<String, Map<String, Double>> e: qDocMap.entrySet()){
			result.put(e.getKey(), sortDscByValue(e.getValue())); //sortByValue(e.getValue()));
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
	private static ModelQres sortAscByValue(Map<String, Double> map) {
		 ModelQres list = new ModelQres(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ModelQres sortDscByValue(Map<String, Double> map) {
		 ModelQres list = new ModelQres(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
}
