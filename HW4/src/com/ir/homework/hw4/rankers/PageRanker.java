package com.ir.homework.hw4.rankers;


import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw4.models.LinkInfo;

public class PageRanker extends BaseRanker{
	private static final long serialVersionUID = 1L;
	
	private Double lastPerplexity    = 0.0;
	private Short  cnt = 0;

	private Map<String, Double>   PR;
	private Map<String, LinkInfo> P;
	private Collection<String>    S;
	private Integer N;
	private final Double  d = 0.85;
	
	/** 
	 * Default constructor
	 * @throws UnknownHostException 
	 */
	public PageRanker() throws UnknownHostException{
		super();
		// Initialize P
		this.P = super.pages;
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
				newPR += d*PR.get(q)/p.getValue().L.size();
			}
			PR.put(p.getKey(), newPR);
		}
	}
	
	public List<Entry<String, Float>> getTopPages(){
		return sortByValue(PR);
	}


	@Override
	public Boolean isConverged(Integer tollerance) {
		Double p = super.getPerplexity(PR);
		
		Double currPerplexity = Math.floor(p)/10;
		currPerplexity -= Math.floor(currPerplexity);
		
		if(lastPerplexity.equals(currPerplexity))
			cnt++;
		
		lastPerplexity = currPerplexity;
		return cnt > tollerance;
	}


	@Override
	public void printTopPages(Integer n) {
		List<Entry<String, Float>> topPages;
		System.out.println("\nTop " + n + " PageRank pages: ");
		topPages = sortByValue(PR);
		for(int i=0;i<n;i++)
			System.out.println(topPages.get(i));
	}
}
