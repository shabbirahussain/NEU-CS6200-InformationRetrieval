package com.ir.homework.hw1.elasticclient;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;


public class CachedElasticClient implements ElasticClient{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	private Integer numberOfTerm = 10;
	private ElasticClient searchClient;
	
	// -------------------- Storage cache -----------------------------
	/**
	 * Class stores term statistics 
	 */
	class TermStats implements Serializable{
		// Serialization version Id
		private static final long serialVersionUID = 1L;
		
		public Map<String, Float> docFrequncyMap;
		public Long termDocCount;
		public List<String> relatedTerms;
		public Long totTermCnt;
		
		/**
		 * Default constructor for term statistics
		 * @param docFrequncyMap is the count of terms in each document
		 * @param termDocCount is the count of documents the term is found
		 * @param relatedTerms is the list if related terms
		 * @param totTermCnt is the total frequency of the term in corpus
		 */
		public TermStats(Map<String, Float> docFrequncyMap, Long termDocCount, List<String> relatedTerms, Long totTermCnt){
			this.docFrequncyMap = docFrequncyMap;
			this.termDocCount   = termDocCount;
			this.relatedTerms   = relatedTerms;
			this.totTermCnt     = totTermCnt;
		}
	}
	
	/**
	 * Class stores docs statistics 
	 */
	class DocStats implements Serializable{
		// Serialization version Id
		private static final long serialVersionUID = 1L;
		
		public Map<String, Float> termFrequncyMap;
		public Long docTermCount;
		
		/**
		 * Default constructor of document statistics
		 * @param termFrequncyMap is the count of terms in a single document
		 * @param docTermCount is the number of documents that term is found in
		 */
		public DocStats(Map<String, Float> termFrequncyMap, Long docTermCount){
			this.termFrequncyMap = termFrequncyMap;
			this.docTermCount    = docTermCount;
		}
	}
		
	private Map<String, TermStats> termStatsMap;
	private Map<String, DocStats>  docStatsMap;
	
	private Long  documentCount;
	private Float avgDocLength;
	private Long  vocabSize;
	
	// ----------------------------------------------------------------

	/**
	 * Default constructor
	 * @param searchClient is the search client
	 */
	public CachedElasticClient(ElasticClient searchClient){
		this.searchClient = searchClient;
		termStatsMap = new HashMap<String, TermStats>();
		docStatsMap  = new HashMap<String, DocStats>();
	}
	
	// --------------------------- Getters ----------------------------
	
	@Override
	public List<String> getSignificantTerms(String term, Integer numberOfTerm) throws IOException{
		this.numberOfTerm = numberOfTerm;
		return this.getTermStats(term).relatedTerms;
	}
	
	@Override
	public Float getAvgDocLen(){
		if(true) return 247.69F;
		
		// Check if term is previously mapped or not. If yes return from cache
		Float result = this.avgDocLength;
		if(result != null) return result;
		
		// Calculate new results
		avgDocLength = result = searchClient.getAvgDocLen();
		
		return result;
	}
	
	@Override
	public Long getVocabSize(){
		// Check if term is previously mapped or not. If yes return from cache
		Long result = this.vocabSize;
		if(result != null) return result;
		
		// Calculate new results
		vocabSize = result = searchClient.getVocabSize();
		
		return result;
	}
	
	@Override
	public Map<String,Float> getDocFrequency(String term) throws IOException{
		return this.getTermStats(term).docFrequncyMap;
	}
	
	@Override
	public Long getDocCount(String term) throws IOException{
		return this.getTermStats(term).termDocCount;
	}

	@Override
	public Long getDocCount(){
		// Check if term is previously mapped or not. If yes return from cache
		Long result = this.documentCount;
		if(result != null) return result;
		
		// Calculate new results
		documentCount = result = searchClient.getDocCount();
		
		return result;
	}
	
	@Override
	public Map<String,Float> getTermFrequency(String docNo, Float minScore, Float maxScore) throws IOException, InterruptedException, ExecutionException{
		DocStats result = this.getDocStats(docNo);
		if(result.termFrequncyMap != null) return result.termFrequncyMap;

		result.termFrequncyMap = searchClient.getTermFrequency(docNo, minScore, maxScore);
		return result.termFrequncyMap;
	}
	
	@Override
	public Map<String,Float> getTermFrequency(String docNo) throws IOException, InterruptedException, ExecutionException{
		return this.getTermFrequency(docNo, 0.0F, Float.MAX_VALUE);
	}
	
	@Override
	public Long getTermCount(String docNo){
		return this.getDocStats(docNo).docTermCount;
	}

	// --------------------------- Private methods --------------------
	/**
	 * Gets TermStats object
	 * @param term to search for
	 * @return Term Statistics as object
	 * @throws IOException 
	 */
	private TermStats getTermStats(String term) throws IOException{
		// Check if term is previously mapped or not. If yes return from cache
		TermStats result = termStatsMap.get(term);
		if(result != null) return result;
		
		// Calculate new results
		result = (new TermStats(searchClient.getDocFrequency(term), 
								searchClient.getDocCount(term),
								searchClient.getSignificantTerms(term, numberOfTerm),
								searchClient.getTotalTermCount(term)));
		
		// Cache it for further use
		termStatsMap.put(term, result);
		
		return result;
	}
	
	/**
	 * Gets DocStats object
	 * @param docNo document to search for
	 * @return Document Statistics as object
	 * @throws IOException 
	 */
	private DocStats getDocStats(String docNo){
		// Check if term is previously mapped or not. If yes return from cache
		DocStats result = docStatsMap.get(docNo);
		if(result != null) return result;
		
		// Calculate new results
		result = (new DocStats( null,//searchClient.getTermFrequency(docNo),
								searchClient.getTermCount(docNo)));
		
		// Cache it for further use
		docStatsMap.put(docNo, result);
		
		return result;
	}
	
	@Override
	public Long getTotalTermCount(String term){
		return termStatsMap.get(term).totTermCnt;
	}
	
	//  ==========================================================

	@Override
	public void loadData(String id, XContentBuilder source) {
		searchClient.loadData(id, source);
	}

	@Override
	public void commit() {
		searchClient.commit();
	}

	@Override
	public Integer getMaxResults() {
		return searchClient.getMaxResults();
	}

	@Override
	public ElasticClient attachClients(Client client, BulkProcessor bulkProcessor) {
		return searchClient.attachClients(client, bulkProcessor);
	}

	@Override
	public Double getBGProbability(String term) {
		return searchClient.getBGProbability(term);
	}
}
