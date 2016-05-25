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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;


public class CachedElasticClient extends BaseElasticClient{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	private Integer numberOfTerm = 10;
	
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
		public Double bgProbability;
		
		public TermStats(Map<String, Float> docFrequncyMap, Long termDocCount, List<String> relatedTerms, Double bgProbability){
			this.docFrequncyMap = docFrequncyMap;
			this.termDocCount   = termDocCount;
			this.relatedTerms   = relatedTerms;
			this.bgProbability  = bgProbability;
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
	 * @param client is the transport client
	 * @param bulkProcessor is the bulk processor client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param enableBulkProcessing flag enables/diables bulk processing
	 * @param limit maximum number of records to fetch
	 * @param field payload field name to query
	 */
	public CachedElasticClient(String indices, String types, Boolean enableBulkProcessing, Integer limit, String field ){
		super(indices, types, enableBulkProcessing, limit, field);
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
		// Check if term is previously mapped or not. If yes return from cache
		Float result = this.avgDocLength;
		if(result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		avgDocLength = result = super.getAvgDocLen();
		
		return result;
	}
	
	@Override
	public Long getVocabSize(){
		// Check if term is previously mapped or not. If yes return from cache
		Long result = this.vocabSize;
		if(result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		vocabSize = result = super.getVocabSize();
		
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
		if(result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		documentCount = result = super.getDocCount();
		
		return result;
	}
	
	@Override
	public Map<String,Float> getTermFrequency(String docNo, Float minScore, Float maxScore) throws IOException, InterruptedException, ExecutionException{
		DocStats result = this.getDocStats(docNo);
		if(result.termFrequncyMap != null){
			this.cacheHits++;
			return result.termFrequncyMap;
		}
		this.cacheMiss++;
		result.termFrequncyMap = super.getTermFrequency(docNo, minScore, maxScore);
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
		if(result != null){
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		result = (new TermStats(super.getDocFrequency(term), 
								super.getDocCount(term),
								null, //super.getSignificantTerms(term, numberOfTerm)));
								super.getBGProbability(term)));
		
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
		if(result != null){
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		result = (new DocStats( null,//super.getTermFrequency(docNo),
								super.getTermCount(docNo)));
		
		// Cache it for further use
		docStatsMap.put(docNo, result);
		
		return result;
	}
	
	@Override
	public Double getBGProbability(String term) {
		return termStatsMap.get(term).bgProbability;
	}

	//  ==================== Cache statistics ====================
	public Integer cacheHits = 0;
	public Integer cacheMiss = 0;
	
	/**
	 * Resets the statistics counter
	 */
	public void resetStatististics(){
		cacheHits = 0;
		cacheMiss = 0;
	}
	//  ==========================================================
}
