package com.ir.homework.hw1.elasticclient;

import static com.ir.homework.hw1.Constants.CLUSTER_NAME;
import static com.ir.homework.hw1.Constants.HOST;
import static com.ir.homework.hw1.Constants.INDEX_NAME;
import static com.ir.homework.hw1.Constants.INDEX_TYPE;
import static com.ir.homework.hw1.Constants.MAX_RESULTS;
import static com.ir.homework.hw1.Constants.PORT;
import static com.ir.homework.hw1.Constants.TEXT_FIELD_NAME;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsRequest.FilterSettings;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

public class BaseElasticClient implements Serializable, ElasticClient{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	
	public String indices;
	public String types;
	public Integer maxResults;
	public String  textFieldName;
	
	public Boolean enableBulkProcessing;

	private static Client _client = null;
	private static BulkProcessor _bulkProcessor = null;
	private static Stemmer stemer;
	
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
	public BaseElasticClient(String indices, String types, Boolean enableBulkProcessing, Integer limit, String field ){
		this.indices = indices;
		this.types   = types;
		this.maxResults = limit;
		this.textFieldName = field;
		this.enableBulkProcessing = enableBulkProcessing;
		this.stemer = new PorterStemmer();
	}
	
	public ElasticClient attachClients(Client client, BulkProcessor bulkProcessor){
		_client        = client;
		_bulkProcessor = bulkProcessor;
		
		return this;
	}

	// --------------------------- Loaders ----------------------------
	
	public void loadData(String id, XContentBuilder source){
		IndexRequestBuilder irBuilder = _client.prepareIndex()
				.setIndex(this.indices)
				.setType(this.types)
				.setId(id)
				.setSource(source);
		
		if (enableBulkProcessing) _bulkProcessor.add(irBuilder.request());
		else                      irBuilder.get();
		
		return;
	}
	
	public void commit(){
		_bulkProcessor.flush();
		_bulkProcessor.close();
	}
	
	// --------------------------- Getters ----------------------------
	
	public Integer getMaxResults(){
		return this.maxResults;
	}
	
	public Float getAvgDocLen(){
		SearchResponse response;
		Float result = 0F;

		response = _client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.addAggregation(AggregationBuilders
						.avg("AVG_LEN")
						.script(new Script("doc['" + textFieldName + "'].values.size()")))
				.setNoFields()
				.setSize(0)
				.get();
		
		result = ((Double) response.getAggregations().get("AVG_LEN").getProperty("value")).floatValue();
		return result;
	}
	
	public Long getVocabSize(){
		SearchResponse response;
		Long result = 0L;
		
		response = _client.prepareSearch()
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
	
	// ------------------------- Document Statistics ------------------
	public Map<String,Float> getTermFrequency(String docNo, Float minScore, Float maxScore) throws IOException, InterruptedException, ExecutionException{
		// TODO optimize this function
		
		Map<String,Float> result = null;
		FilterSettings filters = new FilterSettings();
		filters.minDocFreq = minScore.intValue();
		filters.maxDocFreq = maxScore.intValue();
		
		TermVectorsResponse response = _client.termVectors(
				(new TermVectorsRequest())
				.id(docNo)
				.index(this.indices)
				.type(this.types)
				.selectedFields(textFieldName)
				.filterSettings(filters))
			.get();
		
		result = new HashMap<String,Float>();
		org.apache.lucene.index.TermsEnum terms = response
				.getFields()
				.terms(textFieldName)
				.iterator();
		/*
		XContentBuilder jsonBuilder = JsonXContent.contentBuilder();
		response.toXContent(jsonBuilder, ToXContent.EMPTY_PARAMS);
		String rawJson = jsonBuilder.string();
		System.out.println(rawJson);
		*/
		while(terms.next() != null){
			String term = terms.term().utf8ToString();
			Float value = ((Long)terms.totalTermFreq()).floatValue();
			System.out.println(term + "\t=" + value);
			result.put(term, value);
		}
	
		return result;
	}
	
	public Map<String,Float> getTermFrequency(String docNo) throws IOException, InterruptedException, ExecutionException{
		return this.getTermFrequency(docNo, 0.0F, Float.MAX_VALUE);
	}
	
	public Long getTermCount(String docNo){
		Long result = null;
		SearchResponse response = _client.prepareSearch()
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
				result = ((Float)h.getScore()).longValue();
			}
		}
		return result;
	}
	
	// ---------------------- Term statistics -------------------------
	public Map<String,Float> getDocFrequency(String term) throws IOException{
		Map<String,Float> result = null;
		
		TimeValue scrollTimeValue = new TimeValue(60000);
		Integer  windowMaxResults = 10000; 
		if(maxResults < 10000){
			windowMaxResults = maxResults;
		}
		
		SearchRequestBuilder builder = _client.prepareSearch()
			.setIndices(this.indices)
			.setTypes(this.types)
			.setQuery(QueryBuilders.functionScoreQuery()
				.add(ScoreFunctionBuilders
						.scriptFunction("_index['" + textFieldName + "']['" + term + "'].tf()"))
				.boostMode("replace"))
			.setSize(windowMaxResults)
	        .setScroll(scrollTimeValue)
			.setNoFields();

		SearchResponse response = builder.get();
		
		// Scan for results
		Integer resultsSoFar = 0;
		result = new HashMap<String,Float>();
		while(true){
			if((response.status() != RestStatus.OK) 
					|| (response.getHits().getHits().length == 0)
					|| (resultsSoFar >= maxResults))
				break;
			resultsSoFar += response.getHits().getHits().length;
			
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				String key  = h.getId();
				Float score = h.getScore();
				if(score>0) result.put(key, score);
			}
			
			// fetch next window
			response = _client.prepareSearchScroll(response.getScrollId())
					.setScroll(scrollTimeValue)
					.get();
			
		}
		return result;
	}
	
	public Long getDocCount(String term) throws IOException{
		Long result = 0L; 
		
		// Get query term document count
		SearchResponse response = _client.prepareSearch()
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
	
	public Long getDocCount(){
		SearchResponse response;
		Long result = 1L;
		
		response = _client.prepareSearch()
			.setIndices(this.indices)
			.setTypes(this.types)
			.setNoFields()
			.get();
		
		result = response.getHits().getTotalHits();
		return result;
	}
	
	public List<String> getSignificantTerms(String term, Integer numberOfTerm) throws IOException{
		List<String> result = new LinkedList<String>();
		
		// Get query term document count
		SearchResponse response = _client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.setQuery(QueryBuilders.termsQuery(textFieldName, term))
				.addAggregation(AggregationBuilders
						.significantTerms("SIG_TERM")
						.field(textFieldName))
				.setSize(numberOfTerm * 100)
				.setNoFields()
				.get();
		
		SignificantTerms sigTerms = response.getAggregations().get("SIG_TERM");
		for(SignificantTerms.Bucket entry : sigTerms.getBuckets()){
			String key   = entry.getKey().toString();
			key = key.replaceAll("[^a-z]", " ").trim();
			
			if(!key.equals(term) && !result.contains(key)){
				result.add(key);      // Term
			}
			if(result.size() >= numberOfTerm) break;
		}
	
		return result;
	}
	
	@Override
	public Long getTotalTermCount(String term) {
		Long result = null;
		
		// Get query term document count
		SearchResponse response = _client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.setQuery(QueryBuilders.termsQuery(textFieldName, term))
				.addAggregation(AggregationBuilders
						.sum("TOT_TF")
						.script(new Script("_index['" + textFieldName + "']['" + term + "'].tf()")))
				.setSize(0)
				.setNoFields()
				.get();

		result = ((Double) response.getAggregations()
					.get("TOT_TF")
					.getProperty("value"))
				.longValue();
		return result;
	}
	
	@Override
	public Double getBGProbability(String term) {
		Double result = null; 
		
		// Get query term document count
		SearchResponse response = _client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(AggregationBuilders
						.sum("BG_PROB")
						.script((new Script("_index['" + textFieldName + "']['" + term + "'].tf()"
								+ ""))))
				.setNoFields()
				.get();
		result = (Double) response.getAggregations().get("BG_PROB").getProperty("value");
		return Math.log(result);
	}
	
	public static void main(String arg[]) throws IOException, InterruptedException, ExecutionException{
		ElasticClientBuilder eBuilder = ElasticClientBuilder.createElasticClientBuilder()
				.setClusterName(CLUSTER_NAME)
				.setHost(HOST)
				.setPort(PORT)
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setCachedFetch(false)
				.setField(TEXT_FIELD_NAME);
		
		BaseElasticClient ec = (BaseElasticClient) eBuilder.build();
		//ec.getTermFrequency("AP890220-0147", 9F,500F);
		testSignificant(ec, "atomic");
		testSignificant(ec, "downstream");
		testSignificant(ec, "opec");
		System.out.println(ec.getBGProbability("downstream"));
		System.out.println(ec.getBGProbability("marlar"));
		System.out.println(ec.getBGProbability("coahoma"));
		System.out.println(ec.getBGProbability("opec"));
		System.out.println(ec.getBGProbability("petroleum"));
	}
	
	private static void testSignificant(BaseElasticClient ec, String term) throws IOException{
		term = ec.stemer.stem(term).toString();
		System.out.println("" + term + " => " + ec.getSignificantTerms(term, 10));
	}
}
