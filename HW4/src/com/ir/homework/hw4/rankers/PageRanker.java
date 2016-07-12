package com.ir.homework.hw4.rankers;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;

import com.ir.homework.hw4.elasticclient.ElasticClient;
import com.ir.homework.hw4.jgraph.BigDirectedGraph;

import static com.ir.homework.hw4.Constants.*;


public class PageRanker extends BaseRanker{
	private static final long serialVersionUID = 1L;
	
	private Double lastPerplexity    = 0.0;

	private BigDirectedGraph<?, DefaultEdge> P;
	private Map<Object, Double>  PR;
	private Collection<Object>    S;
	private Integer N;
	private final Double  d = 0.85;
	
	/** 
	 * Default constructor
	 * @throws UnknownHostException 
	 */
	public PageRanker() throws UnknownHostException{
		super();
		// Initialize P
		this.P = (new ElasticClient()).loadFullLinksMap();
		this.S = new LinkedList<Object>();
		
		
		//Initialize N
		this.N  = P.vertexSet().size();
		this.PR = new HashMap<Object, Double>();
		
		System.out.println("Initializing...");
		Double initialRank = 1.0/N;
		for(Object pObj : P.vertexSet()){
			//Initialize PR
			this.PR.put(pObj, initialRank);
			
			//Initialize S
			if(P.outDegreeOf((Integer) pObj) == 0)
				S.add(pObj);
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
		for(Object p: S)
			sinkPR += PR.get(p);
		
		Map<Object, Double> nPR = new HashMap<Object, Double>();
		for(Object p : P.vertexSet()){
			Double newPR  = (1-d)/N;
			newPR += d*sinkPR/N;
			
			for(Object edge : P.incomingEdgesOf((Integer) p)){
				Object q = P.getEdgeSource((DefaultEdge) edge);
				newPR   += d*PR.get(q)/P.outDegreeOf((Integer) q);
			}
			nPR.put(p, newPR);
		}
		this.PR = nPR;
		
//		for(Object obj: P.vertexSet()){
//			Integer p = (Integer) obj;
//			PR.put(p, nPR.get(p));
//		}
	}

	@Override
	public Boolean isConverged(Integer tollerance) {
		Double p = super.getPerplexity(PR);
		
		Double currPerplexity = p;//Math.floor(p)/10;
		//currPerplexity -= Math.floor(currPerplexity);
		
		if(Math.abs(lastPerplexity - currPerplexity) < EPSILON_PRECISION)
			cnt++;
		else 
			cnt = 0;
		lastPerplexity = currPerplexity;
		return cnt > tollerance;
	}
	
	/**
	 * Builds the underlying graph
	 */
	public void buildGraph(){
		P.buildGraph();
	}


	@Override
	public void printTopPages(Integer n) {
		List<Entry<Object, Double>> topPages;
		System.out.println("\nTop " + n + " PageRank pages: ");
		topPages = super.sortByValue(PR);
		for(int i=0;i<n && i<topPages.size();i++){
			Entry<Object, Double> e = topPages.get(i);
			Integer v = (Integer) e.getKey();
			System.out.println(P.decodeVertex(v) 
					+ "\t" + e.getValue()
					+ "\t" + P.inDegreeOf(v));
		}	
	}
}
