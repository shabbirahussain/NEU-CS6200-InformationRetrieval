package com.ir.homework.hw4.rankers;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;

import com.ir.homework.hw4.elasticclient.ElasticClient;


public abstract class BaseRanker implements Serializable, Ranker{
	private static final Double LOG_BASE2 = Math.log(2);
	//protected DirectedGraph pages;
	
	/**
	 * Default constructor
	 * @param Loads pages map
	 * @throws UnknownHostException 
	 */
	public BaseRanker() throws UnknownHostException{
		System.out.println("Loading links map...");
		//this.pages  = elasticClient.loadLinksMap();
	}

	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static <K, V extends Number> List<Entry<K, V>> sortByValue(Map<K,V> map) {
	     List<Entry<K, V>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	/**
	 * Gets perplexity of distribution
	 * @return Value of perplexity
	 */
	public static <K, V extends Number>  Double getPerplexity(Map<K, V> rankMap){
		Double entropy = 0.0;
		for(Entry<K, V> e: rankMap.entrySet()){
			V pxi = e.getValue();
			if(pxi.equals(0)){
				entropy += -pxi.doubleValue()*Math.log(pxi.doubleValue())/LOG_BASE2;
			}
		}
		return Math.pow(2, entropy);
	}
}
