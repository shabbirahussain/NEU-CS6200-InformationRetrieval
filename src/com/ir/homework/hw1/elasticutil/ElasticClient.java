package com.ir.homework.hw1.elasticutil;

import static com.ir.homework.hw1.Constants.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;

public class ElasticClient implements Serializable{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	
	protected String indices;
	protected String types;
	protected Integer maxResults;
	protected String  textFieldName;
	
	private Boolean enableCache;
	private Boolean enableBulkProcessing;
	
	/**
	 * Class stores term statistics 
	 */
	private class TermStats implements Serializable{
		// Serialization version Id
		private static final long serialVersionUID = 1L;
		
		public Map<String, Float> docFrequncyMap;
		public Long termDocCount;
		
		public TermStats(Map<String, Float> docFrequncyMap, Long termDocCount){
			this.docFrequncyMap = docFrequncyMap;
			this.termDocCount = termDocCount;
		}
	}
	
	// Stores the term frequency vector for selected terms and cashed it into map
	private Map<String, TermStats> termStatsMap;
	
	// Stores document stats
	private Map<String, Float> docStatsMap;
	
	private Long  documentCount;
	private Float avgDocLength;
	private Long  vocabSize;
	
	/**
	 * Default constructor
	 */
	public ElasticClient(){
		this.enableCache = true;
		this.enableBulkProcessing = true;
		termStatsMap = new HashMap<String, TermStats>();
		docStatsMap  = new HashMap<String, Float>();
	}
	
	// ---------------------- Setters ---------------------------------
	
	/**
	 * Sets indices of elastic client
	 * @param indices name of the indices
	 * @return
	 */
	public ElasticClient setIndices(String indices){
		this.indices = indices;
		return this;
	}
	
	public ElasticClient setTypes(String types){
		this.types = types;
		return this;
	}
	/**
	 * Sets cached retrieval mode
	 * @param enableCache 
	 * @return
	 */
	public ElasticClient setCache(Boolean enableCache){
		this.enableCache = enableCache;
		return this;
	}
	
	/**
	 * Sets bulk processing mode
	 * @param enableBulkProcessing
	 * @return
	 */
	public ElasticClient setBulkProcessing(Boolean enableBulkProcessing){
		this.enableBulkProcessing = enableBulkProcessing;
		return this;
	}
	
	/**
	 * Sets size of query retrieval
	 * @param size
	 * @return
	 */
	public ElasticClient setLimit(Integer size){
		this.maxResults = size;
		return this;
	}
	
	/**
	 * Sets field to query
	 * @param field
	 * @return
	 */
	public ElasticClient setField(String field){
		this.textFieldName = field;
		return this;
	}

	
	// --------------------------- Getters ----------------------------
	
	/**
	 * Fetches and cashes average document length
	 * @return
	 */
	public Float getAvgDocLength(){
		// Check if term is previously mapped or not. If yes return from cache
		Float result = this.avgDocLength;
		if(enableCache && result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		avgDocLength = result = this.calcAvgDocLen();
		
		return result;
	}
	
	/**
	 * Fetches and cashes document count
	 * @return
	 */
	public Long getDocumentCount(){
		// Check if term is previously mapped or not. If yes return from cache
		Long result = this.documentCount;
		if(enableCache && result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		documentCount = result = this.calcDocumentCount();
		
		return result;
	}
	
	/**
	 * Fetches and cashes Vocabulary size of whole corpus
	 * @return
	 */
	public Long getVocabSize(){
		// Check if term is previously mapped or not. If yes return from cache
		Long result = this.vocabSize;
		if(enableCache && result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		vocabSize = result = this.calcVocabSize();
		
		return result;
	}
	
	/**
	 * Fetches and cashes the term frequency 
	 * @param term search term
	 * @return term frequency map indicesed by document id
	 * @throws IOException
	 */
	public Map<String,Float> getTermFrequency(String term) throws IOException{
		return this.getTermStats(term).docFrequncyMap;
	}
	
	/**
	 * Fetches documents a term is found
	 * @param term
	 * @return
	 */
	public Long getTermDocCount(String term){
		return this.getTermStats(term).termDocCount;
	}
	
	/**
	 * Fetches document length from cache or generates and stores it
	 * @param docNo
	 * @return
	 */
	public Float getDocLength(String docNo){
		// Check if term is previously mapped or not. If yes return from cache
		Float result = docStatsMap.get(docNo);
		if(enableCache && result != null) {
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		result = this.calcDocLength(docNo);
		// Cache it for further use
		docStatsMap.put(docNo, result);
		
		return result;
	}
	
	// --------------------------- Loaders ----------------------------
	
	/**
	 * Loads data into index
	 * @param id unique identifier of document
	 * @param source data to be loaded in JSON format
	 */
	public void loadData(String id, XContentBuilder source){
		IndexRequestBuilder irBuilder = client.prepareIndex()
				.setIndex(this.indices)
				.setType(this.types)
				.setId(id)
				.setSource(source);
		
		if (enableBulkProcessing) bulkProcessor.add(irBuilder.request());
		else                      irBuilder.get();
		
		return;
	}
	
	/**
	 * Commits the data to index
	 */
	public void commit(){
		bulkProcessor.close();
	}
	
	// --------------------------- Private methods --------------------
	

	/**
	 * Calculates document count
	 * @return
	 */
	private Long calcDocumentCount(){
		SearchResponse response;
		Long result = 1L;
		
		response = client.prepareSearch()
			.setIndices(this.indices)
			.setTypes(this.types)
			.setNoFields()
			.get();
		
		result = response.getHits().getTotalHits();
		return result;
	}
	
	/**
	 * Calculates average document length
	 * @return
	 */
	private Float calcAvgDocLen(){
		SearchResponse response;
		Float result = 0F;

		response = client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.addAggregation(
						AggregationBuilders
						.avg("AVG_LEN")
						.script(new Script("doc['" + textFieldName + "'].values.size()")))
				.setNoFields()
				.setSize(0)
				.get();
		
		result = ((Double) response.getAggregations().get("AVG_LEN").getProperty("value")).floatValue();
		return result;
	}
	
	
	/**
	 * Calculates vocabulary size
	 * @return
	 */
	private Long calcVocabSize(){
		SearchResponse response;
		Long result = 0L;
		
		response = client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.addAggregation(AggregationBuilders
						.cardinality("VOCAB_SIZE")
						.precisionThreshold(Integer.MAX_VALUE)
						.field(textFieldName)
						//.script(new Script("doc['" + textFieldName + "'].values "))
						)
				.setNoFields()
				.setSize(0)
				.get();
		
		result = ((Double) response.getAggregations().get("VOCAB_SIZE").getProperty("value")).longValue();
		return result;
	}
	
	/** 
	 * Fetches term stats from cache or builds new one
	 * @param term
	 * @return
	 */
	private TermStats getTermStats(String term){
		// Check if term is previously mapped or not. If yes return from cache
		TermStats result = termStatsMap.get(term);
		if(enableCache && result != null){
			this.cacheHits++;
			return result;
		}
		this.cacheMiss++;
		
		// Calculate new results
		result = this.calcTermStats(term);
		// Cache it for further use
		termStatsMap.put(term, result);
		
		return result;
	}

	/**
	 * Calculates document length
	 * @param docNo
	 * @return document length 
	 */
	private Float calcDocLength(String docNo){
		Float result = null;
		SearchResponse response = client.prepareSearch()
			.setIndices(this.indices)
			.setTypes(this.types)
			.setQuery(QueryBuilders.boolQuery()
					.must(QueryBuilders.idsQuery().addIds(docNo))
					.should(QueryBuilders.functionScoreQuery()
							.add(ScoreFunctionBuilders
								.scriptFunction("doc['" + textFieldName + "'].values.size()"))
							.boostMode("replace")))
			.setSize(1)
			.setNoFields()
			.get();
		
		if(response.status() == RestStatus.OK){
			SearchHit hit[] = response.getHits().hits();
			for(SearchHit h:hit){
				Float score = h.getScore();
				if(score>0) result = score;
			}
		}
		return result;
	}
	
	/**
	 * Calculates term stats
	 * @param term
	 * @return
	 */
	private TermStats calcTermStats(String term){
		Map<String,Float> tfMap = this.calcTermFrequency(term);
		Long termDocCount     = this.calcTermDocCount(term);
		
		TermStats result = new TermStats(tfMap, termDocCount);
		return result;
	}
	
	/**
	 * Calculates term frequency
	 * @param term
	 * @return
	 */
	private Map<String,Float> calcTermFrequency(String term){
		Map<String,Float> result = null;
		SearchResponse response = client.prepareSearch()
			.setIndices(this.indices)
			.setTypes(this.types)
			.setQuery(QueryBuilders.functionScoreQuery()
				.add(ScoreFunctionBuilders
						.scriptFunction("_index['" + textFieldName + "']['" + term + "'].tf()"))
				.boostMode("replace"))
			.setSize(maxResults)
			.setNoFields()
			.get();
		
		if(response.status() == RestStatus.OK){
			result = new HashMap<String,Float>();
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				String key  = h.getId();
				Float score = h.getScore();
				if(score>0) result.put(key, score);
			}
		}
		return result;
	}
	
	/**
	 * calculates uniqueness of term
	 * @param term given string term
	 * @return an importance score
	 */
	private Long calcTermDocCount(String term){
		Long result = 0L; 
		
		// Get query term document count
		SearchResponse response = client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.setQuery(QueryBuilders.matchQuery(textFieldName, term))
				.setNoFields()
				.get();
		
		if(response.status() == RestStatus.OK){
			result = response.getHits().getTotalHits();
		}
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
	
	
	
	
	/**
	 * Main method for testing only
	 * @param args
	 */
	public static void main(String args[]){
		ElasticClient sc = ElasticClientBuilder.createElasticClient()
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setField(TEXT_FIELD_NAME);
		
		String docNo = "AP890912-0225";
		
		System.out.println("len["+docNo+"]="+sc.getDocLength(docNo));
		System.out.println("imp=" + sc.calcTermDocCount("nuclear"));
		System.out.println("imp=" + sc.calcTermDocCount("a"));
		
		System.out.println("getVocabSize="+sc.getVocabSize());
		
	}
}
