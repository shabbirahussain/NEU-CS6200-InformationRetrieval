package com.ir.homework.hw4.rankers;


import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw4.elasticclient.ElasticClient;
import com.ir.homework.hw4.models.LinkInfo;

public class PageRanker implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final Double LOG_BASE2 = Math.log(2);

	public static ElasticClient  _elasticClient;
	
	private Map<String, Double>   PR;
	private Map<String, LinkInfo> P;
	private Collection<String>    S;
	private Integer N;
	private final Double  d = 0.85;
	
	/** 
	 * Default constructor
	 */
	public PageRanker(){
		// Initialize P
		System.out.println("Loading links map...");
		this.P = _elasticClient.loadLinksMap();
		this.S = new LinkedList<String>();
		
		//Initialize N
		this.N = P.size();
		this.PR = new HashMap<String, Double>();
		
		System.out.println("Initializing...");
		Double initialRank = 1.0/N;
		for(Entry<String, LinkInfo> e: P.entrySet()){
			//Initialize PR
			this.PR.put(e.getKey(), initialRank);
			
			//Initialize S
			if(e.getValue().M.size() == 0)
				S.add(e.getKey());
		}
	}
	
	
	/**
	 * Ranks one iteration of page rank
	 */
	public void rankPages(){
//		// P is the set of all pages; |P| = N
//		// S is the set of sink nodes, i.e., pages that have no out links
//		// M(p) is the set of pages that link to page p
//		// L(q) is the number of out-links from page q
//		// d is the PageRank damping/teleportation factor; use d = 0.85 as is typical
//
//		foreach page p in P
//		  PR(p) = 1/N                          /* initial value */
//
//		while PageRank has not converged do
//		  sinkPR = 0
//		  foreach page p in S                  /* calculate total sink PR */
//		    sinkPR += PR(p)
//		  foreach page p in P
//		    newPR(p) = (1-d)/N                 /* teleportation */
//		    newPR(p) += d*sinkPR/N             /* spread remaining sink PR evenly */
//		    foreach page q in M(p)             /* pages pointing to p */
//		      newPR(p) += d*PR(q)/L(q)         /* add share of PageRank from in-links */
//		  foreach page p
//		    PR(p) = newPR(p)
//
//		return PR

		Double sinkPR = 0.0;
		for(String p: S)
			sinkPR += PR.get(p);
		
		Double newPR = 0.0;
		for(Entry<String, LinkInfo> p: P.entrySet()){
			newPR  = (1-d)/N;
			newPR += d*sinkPR/N;
			
			for(String q: p.getValue().M){
				newPR += d*PR.get(q)/p.getValue().L;
			}
			PR.put(p.getKey(), newPR);
		}
	}
	
	/**
	 * Gets perplexity of distribution
	 * @return Value of perplexity
	 */
	public Double getPerplexity(){
		Double entropy = 0.0;
		for(Entry<String, Double> e: PR.entrySet()){
			Double pxi = e.getValue();
			if(pxi != 0){
				entropy += -pxi*Math.log(pxi)/LOG_BASE2;
			}
		}
		return Math.pow(2, entropy);
	}
	
	/**
	 * Gets list of pages as per current rank sorted by rank
	 * @return List of pages and rank
	 */
	public List<Entry<String, Float>> getTopPages(){
		return sortByValue(PR);
	} 

	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Entry<String, Float>> sortByValue(Map map) {
	     List<Entry<String, Float>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	public static void main(String[] args) throws IOException {
		_elasticClient = new ElasticClient();
		PageRanker pr = new PageRanker();
		pr.rankPages();
		List<Entry<String, Float>> topPages = pr.getTopPages();
		for(int i=0;i<500;i++)
			System.out.println(topPages.get(i));
		
	}
}
