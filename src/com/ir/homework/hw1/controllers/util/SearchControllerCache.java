package com.ir.homework.hw1.controllers.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import static com.ir.homework.common.Constants.*;

public class SearchControllerCache implements Serializable{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	
	protected String index;
	protected String type;
	protected Integer maxResults;
	protected String  textFieldName;
	
	/**
	 * Class stores term statistics 
	 */
	private class TermStats{
		public Map<String, Float> docFrequncyMap;
		public Double termImportance;
		
		public TermStats(Map<String, Float> docFrequncyMap, Double termImportance){
			this.docFrequncyMap = docFrequncyMap;
			this.termImportance = termImportance;
		}
	}
	
	// Stores the term frequency vector for selected terms and cashed it into map
	private Map<String, TermStats> termStatsMap;
	
	// Stores document stats
	private Map<String, Float> docStatsMap;
	
	private Long   documentCount;
	private Float avgDocLength;
	
	/**
	 * Default constructor for search controller
	 * @param index index name
	 * @param type type name
	 * @param maxResults maximum number of results to fetch
	 * @param textFieldName data field name to query
	 */
	public SearchControllerCache(String index, String type, Integer maxResults, String textFieldName){
		this.index = index;
		this.type  = type;
		this.maxResults    = maxResults;
		this.textFieldName = textFieldName;
		termStatsMap = new HashMap<String, TermStats>();
		docStatsMap  = new HashMap<String, Float>();
		
		SearchResponse response;
		// Calculate document count
		response = client.prepareSearch()
			.setIndices(index)
			.setTypes(type)
			.setNoFields()
			.get();
		
		documentCount = response.getHits().getTotalHits();
		
		// Calculate document length
		response = client.prepareSearch()
				.setIndices(index)
				.setTypes(type)
				.addAggregation(
						AggregationBuilders
						.avg("AVG_LEN")
						.script(new Script("doc['" + textFieldName + "'].values.size()")))
				.setNoFields()
				.setSize(0)
				.get();
		avgDocLength = ((Double) response.getAggregations().get("AVG_LEN").getProperty("value")).floatValue();
	}
	
	/**
	 * Fetches document length from cache or generates and stores it
	 * @param docNo
	 * @return
	 */
	public Float getDocLength(String docNo){
		// Check if term is previously mapped or not. If yes return from cache
		Float result = docStatsMap.get(docNo);
		if(ENABLE_TF_CACHE && result != null) return result;
		
		// Calculate new results
		result = this.calcDocLength(docNo);
		// Cache it for further use
		docStatsMap.put(docNo, result);
		
		return result;
	}
	
	public Float getAvgDocLength(){
		return avgDocLength;
	}
	
	/**
	 * Calculates document length
	 * @param docNo
	 * @return document length 
	 */
	private Float calcDocLength(String docNo){
		Float result = null;
		SearchResponse response = client.prepareSearch(index)
			.setTypes(type)
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
	 * Fetches and cashes the term frequency 
	 * @param term search term
	 * @return term frequency map indexed by document id
	 * @throws IOException
	 */
	public Map<String,Float> getTermFrequency(String term) throws IOException{
		return this.getTermStats(term).docFrequncyMap;
	}
	
	/**
	 * Fetches term importance from cache or builds new one
	 * @param term
	 * @return
	 */
	public Double getTermImportance(String term){
		return this.getTermStats(term).termImportance;
	}
	
	/** 
	 * Fetches term stats from cache or builds new one
	 * @param term
	 * @return
	 */
	private TermStats getTermStats(String term){
		// Check if term is previously mapped or not. If yes return from cache
		TermStats result = termStatsMap.get(term);
		if(ENABLE_TF_CACHE && result != null) return result;
		
		// Calculate new results
		result = this.calcTermStats(term);
		// Cache it for further use
		termStatsMap.put(term, result);
		
		return result;
	}
	
	/**
	 * Calculates term stats
	 * @param term
	 * @return
	 */
	private TermStats calcTermStats(String term){
		Map<String,Float> tfMap = this.calcTermFrequency(term);
		Double termImportance   = this.calcTermImportance(term);
		
		TermStats result = new TermStats(tfMap, termImportance);
		return result;
	}
	
	/**
	 * Calculates term frequency
	 * @param term
	 * @return
	 */
	private Map<String,Float> calcTermFrequency(String term){
		Map<String,Float> result = null;
		SearchResponse response = client.prepareSearch(index)
			.setTypes(type)
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
	private Double calcTermImportance(String term){
		Double result;
		
		// Get query term document count
		SearchResponse response = client.prepareSearch(index)
				.setTypes(type)
				.setQuery(QueryBuilders.matchQuery(textFieldName, term))
				.setNoFields()
				.get();
		
		Long tatalHits = 0L; 
		if(response.status() == RestStatus.OK){
			tatalHits = response.getHits().getTotalHits();
		}
		
		// Calculate term importance with logarithmic decay
		result = (documentCount / Math.log(tatalHits));
		
		return result;
	}
	
	/**
	 * Main method for testing only
	 * @param args
	 */
	public static void main(String args[]){
		SearchControllerCache sc = new SearchControllerCache(INDEX_NAME, INDEX_TYPE, MAX_RESULTS, TEXT_FIELD_NAME);
		
		System.out.println("len="+sc.getDocLength("AP890912-0225"));
		System.out.println("imp=" + sc.calcTermImportance("nuclear"));
		System.out.println("imp=" + sc.calcTermImportance("the"));
	}
}
