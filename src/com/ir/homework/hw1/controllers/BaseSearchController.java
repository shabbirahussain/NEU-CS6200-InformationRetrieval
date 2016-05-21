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

import com.ir.homework.hw1.controllers.util.SearchControllerCache;
import com.ir.homework.io.OutputWriter;

/**
 * @author shabbirhussain
 *
 */
public abstract class BaseSearchController {
	private Integer maxResults;
	protected SearchControllerCache searchCache;
	
	/**
	 * constructor for re using cache across controllers
	 * @param searchCache
	 * @param maxResults
	 */
	public BaseSearchController(SearchControllerCache searchCache, Integer maxResults){
		this.searchCache = searchCache;
		this.maxResults  = maxResults;
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
}
