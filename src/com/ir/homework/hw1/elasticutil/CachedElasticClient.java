package com.ir.homework.hw1.elasticutil;

import static com.ir.homework.hw1.Constants.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;


public class CachedElasticClient extends BaseElasticClient{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	
	// -------------------- Storage cache -----------------------------
	/**
	 * Class stores term statistics 
	 */
	class TermStats implements Serializable{
		// Serialization version Id
		private static final long serialVersionUID = 1L;
		
		public Map<String, Float> docFrequncyMap;
		public Long termDocCount;
		
		public TermStats(Map<String, Float> docFrequncyMap, Long termDocCount){
			this.docFrequncyMap = docFrequncyMap;
			this.termDocCount   = termDocCount;
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
	public CachedElasticClient(Client client, BulkProcessor bulkProcessor, String indices, String types, Boolean enableBulkProcessing, Integer limit, String field ){
		super(client, bulkProcessor, indices, types, enableBulkProcessing, limit, field);
		termStatsMap = new HashMap<String, TermStats>();
		docStatsMap  = new HashMap<String, DocStats>();
	}
	
	// --------------------------- Getters ----------------------------
	
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
	public Map<String,Float> getTermFrequency(String docNo){
		return this.getDocStats(docNo).termFrequncyMap;
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
		result = (new TermStats(super.getDocFrequency(term), super.getDocCount(term)));
		
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
		result = (new DocStats(super.getTermFrequency(docNo), super.getTermCount(docNo)));
		
		// Cache it for further use
		docStatsMap.put(docNo, result);
		
		return result;
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
