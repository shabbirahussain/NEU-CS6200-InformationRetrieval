/**
 * 
 */
package com.ir.homework.hw1.controllers;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.elasticutil.ElasticClient;
import com.ir.homework.io.OutputWriter;

/**
 * @author shabbirhussain
 *
 */
public abstract class BaseSearchController {
	private Integer maxResults;
	private Boolean addTransEnable;
	protected ElasticClient elasticClient;
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient
	 * @param maxResults
	 */
	public BaseSearchController(ElasticClient elasticClient, Integer maxResults, Boolean addTransEnable){
		this.elasticClient    = elasticClient;
		this.maxResults     = maxResults;
		this.addTransEnable = addTransEnable;
	}

	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Entry<String, Float>> sortByValue(Map map) {
	     List<Entry<String, Float>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	/**
	 * Creates output record format from given map while sorting map accordingly
	 * @param queryNo query identifier
	 * @param map result of search in map
	 * @return
	 */
	protected List<OutputWriter.OutputRecord> prepareOutput(String queryNo, Map<String, Float> map){
		List<Entry<String, Float>> sortedMapList = sortByValue(map);
		List<OutputWriter.OutputRecord> result = new LinkedList<OutputWriter.OutputRecord>();
		
		Long i = 1L;
		for(Entry<String, Float> e : sortedMapList){
			result.add(new OutputWriter.OutputRecord(queryNo, e.getKey(), i++, e.getValue()));
			// Limit results to max results
			if(i>maxResults) break;
		}
		return result;
	}
	
	/**
	 * Applies sigmoid transformaion for given value 
	 * @param value
	 * @return smoothened value over sigmoid
	 */
	private Float sigmoidSmoothing(Float value){
		value = (float) (10/(1+Math.pow(value, -1)));
		return value;
	}
	
	/**
	 * Assigns appropriate score to each term bases on its rarity
	 * @param term string term to be evaluated against corpus
	 * @return 
	 */
	private Float weightTermUniqeness(String term){
		Float result = null;
		
		Long docCnt = this.elasticClient.getDocumentCount();
		Long termDocCnt = this.elasticClient.getTermDocCount(term);
		
		result = ((Long)(docCnt / termDocCnt)).floatValue();
		
		return result;
	}
	
	/**
	 * Performs additional transformations on given term and score
	 * @param term string term to calculate transformations
	 * @param value value to be boosted
	 * @return a weight adjusted result for a term
	 */
	protected Float additionalTransformation(String term, Float value){
		if(!addTransEnable) return value;
		
		Float result = value;
		//result *=  this.weightTermUniqeness(term); 
		result  = this.sigmoidSmoothing(result);
		
		return result;
	}
}
