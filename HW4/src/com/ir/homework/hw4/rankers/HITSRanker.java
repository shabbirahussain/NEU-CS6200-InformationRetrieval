package com.ir.homework.hw4.rankers;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw4.models.LinkInfo;

public class HITSRanker extends BaseRanker{
	private static final long serialVersionUID = 1L;
	
	private Double lastPerplexityHUB    = 0.0;
	private Double lastPerplexityAUT    = 0.0;
	private Short  cnt = 0;

	private Map<String, LinkInfo> G;
	private Map<String, Double>   PR_hub;
	private Map<String, Double>   PR_auth;
	
	/** 
	 * Default constructor
	 * @throws UnknownHostException 
	 */
	public HITSRanker() throws UnknownHostException{
		super();
		// Initialize P
		System.out.println("Loading links map...");
		this.G = super.pages;
		
		this.PR_hub  = new HashMap<String, Double>();
		this.PR_auth = new HashMap<String, Double>();
	}
	
	
	/**
	 * Ranks one iteration of page rank
	 */
	public void rankPages(){
//		 1 G := set of pages
//		 2 for each page p in G do
//		 3   p.auth = 1 	// p.auth is the authority score of the page p
//		 4   p.hub  = 1 	// p.hub is the hub score of the page p
//		 5 function HubsAndAuthorities(G)
//		 6   for step from 1 to k do 		// run the algorithm for k steps
//		 7     norm = 0
//		 8     for each page p in G do  	// update all authority values first
//		 9       p.auth = 0
//		10       for each page q in p.incomingNeighbors do // p.incomingNeighbors is the set of pages that link to p
//		11          p.auth += q.hub
//		12       norm += square(p.auth)  	// calculate the sum of the squared auth values to normalise
//		13     norm = sqrt(norm)
//		14     for each page p in G do  	// update the auth scores 
//		15       p.auth = p.auth / norm 	// normalise the auth values
//		16     norm = 0
//		17     for each page p in G do  	// then update all hub values
//		18       p.hub = 0
//		19       for each page r in p.outgoingNeighbors do // p.outgoingNeighbors is the set of pages that p links to
//		20         p.hub += r.auth
//		21       norm += square(p.hub) 		// calculate the sum of the squared hub values to normalise
//		22     norm = sqrt(norm)
//		23     for each page p in G do  	// then update all hub values
//		24       p.hub = p.hub / norm   	// normalise the hub values
		
		////////////// AUTH Score Update ///////////
		Double norm = 0.0;
		for(Entry<String, LinkInfo> e: G.entrySet()){	// update all authority values first
			String key = e.getKey();
			Double p_auth = 0.0;
			
			for(String q: e.getValue().M){				// p.incomingNeighbors is the set of pages that link to p
				p_auth += PR_hub.getOrDefault(q, 1.0);
			}		
			PR_auth.put(key, p_auth);
			norm += Math.pow(p_auth, 2);					// calculate the sum of the squared auth values to normalise
		}
		norm = Math.sqrt(norm);
		for(Entry<String, LinkInfo> e: G.entrySet()){	// update the auth scores 
			String key = e.getKey();
			PR_auth.put(key, PR_auth.get(e.getKey())/norm);// normalise the auth values
		}
		
		//////////// HUB Score Update //////////////
		norm = 0.0;
		for(Entry<String, LinkInfo> e: G.entrySet()){	// then update all hub values
			String key = e.getKey();
			Double p_hub = 0.0;
			
			for(String r: e.getValue().L){				// p.outgoingNeighbors is the set of pages that p links to
				p_hub += PR_auth.getOrDefault(r, 1.0);	
			}	
			PR_hub.put(key, p_hub);
			norm += Math.pow(p_hub, 2);					// calculate the sum of the squared auth values to normalise
		}
		norm = Math.sqrt(norm);
		for(Entry<String, LinkInfo> e: G.entrySet()){	// update the hub scores 
			String key = e.getKey();
			PR_hub.put(key, PR_hub.get(e.getKey())/norm);// normalise the hub values
		}
		
	}
	
	@Override
	public void printTopPages(Integer n){
		List<Entry<String, Float>> topPages;
		System.out.println("\nTop " + n + " Auth pages: ");
		topPages = sortByValue(PR_auth);
		for(int i=0;i<n;i++)
			System.out.println(topPages.get(i));
		

		System.out.println("\nTop " + n + " Hub pages: ");
		topPages = sortByValue(PR_hub);
		for(int i=0;i<n;i++)
			System.out.println(topPages.get(i));
		
	}

	@Override
	public Boolean isConverged(Integer tollerance) {
		Double p = super.getPerplexity(PR_hub);
		Double currPerplexityHUB = Math.floor(p)/10;
		currPerplexityHUB -= Math.floor(currPerplexityHUB);
		
		p = super.getPerplexity(PR_auth);
		Double currPerplexityAUT = Math.floor(p)/10;
		currPerplexityAUT -= Math.floor(currPerplexityAUT);
		
		if(lastPerplexityHUB.equals(currPerplexityHUB)
				&& lastPerplexityAUT.equals(currPerplexityAUT))
			cnt++;
		
		lastPerplexityAUT = currPerplexityAUT;
		lastPerplexityHUB = currPerplexityHUB;

		return cnt > tollerance;
	}
}
