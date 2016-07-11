package com.ir.homework.hw4.rankers;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.ir.homework.hw4.elasticclient.ElasticClient;
import com.ir.homework.hw4.jgraph.BigDirectedGraph;

public class HITSRanker extends BaseRanker{
	private static final long serialVersionUID = 1L;
	
	private Double lastPerplexityHUB    = 0.0;
	private Double lastPerplexityAUT    = 0.0;

	private BigDirectedGraph<?, DefaultEdge> G;
	private Map<Object, Double>   PR_hub;
	private Map<Object, Double>   PR_auth;
	
	/** 
	 * Default constructor
	 * @throws UnknownHostException 
	 */
	public HITSRanker() throws UnknownHostException{
		super();
		// Initialize P
		System.out.println("Loading links map...");
		this.G = (new ElasticClient()).loadMapFromRootSet();
		
		this.PR_hub  = new HashMap<Object, Double>();
		this.PR_auth = new HashMap<Object, Double>();
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
		for(Object p: G.vertexSet()){
			Double p_auth = 0.0;
			
			for(Iterator<DefaultEdge> iter1 = G.incomingEdgesOf((Integer)p).iterator(); iter1.hasNext();){
				DefaultEdge e = iter1.next();
				String q = G.getEdgeSource(e).toString();
				
				p_auth += PR_hub.getOrDefault(q, 1.0);
			}		
			PR_auth.put(p, p_auth);
			norm += Math.pow(p_auth, 2);					// calculate the sum of the squared auth values to normalise
		}
		norm = Math.sqrt(norm);
		for(Object p: G.vertexSet()){
			PR_auth.put(p, PR_auth.get(p)/norm);// normalise the auth values
		}
		
		//////////// HUB Score Update //////////////
		norm = 0.0;
		for(Object p: G.vertexSet()){
			Double p_hub = 0.0;
			
			for(Iterator<DefaultEdge> iter1 = G.outgoingEdgesOf((Integer) p).iterator(); iter1.hasNext();){
				DefaultEdge e = iter1.next();
				String r = G.getEdgeTarget(e).toString();
				
				p_hub += PR_auth.getOrDefault(r, 1.0);	
			}	
			PR_hub.put(p, p_hub);
			norm += Math.pow(p_hub, 2);					// calculate the sum of the squared auth values to normalise
		}
		norm = Math.sqrt(norm);
		for(Object p: G.vertexSet()){
			PR_hub.put(p, PR_hub.get(p)/norm);// normalise the hub values
		}
		
	}
	
	@Override
	public void printTopPages(Integer n){
		List<Entry<Object, Double>> topPages;
		System.out.println("\nTop " + n + " Auth pages: ");
		topPages = sortByValue(PR_auth);
		for(int i=0;i<n && i<topPages.size();i++){
			Entry<Object, Double> e = topPages.get(i);
			System.out.println(G.decodeVertex((Integer) e.getKey())
					+"\t" + e.getValue());
		}

		System.out.println("\nTop " + n + " Hub pages: ");
		topPages = sortByValue(PR_hub);
		for(int i=0;i<n && i<topPages.size();i++){
			Entry<Object, Double> e = topPages.get(i);
			System.out.println(G.decodeVertex((Integer) e.getKey())
					+"\t" + e.getValue());
		}
	}
	
	/**
	 * Builds the underlying graph
	 */
	public void buildGraph(){
		G.buildGraph();
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
		else 
			cnt = 0;
		
		lastPerplexityAUT = currPerplexityAUT;
		lastPerplexityHUB = currPerplexityHUB;

		return cnt > tollerance;
	}
}
