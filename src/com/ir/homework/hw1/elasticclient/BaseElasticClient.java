package com.ir.homework.hw1.elasticclient;

import static com.ir.homework.hw1.Constants.CLUSTER_NAME;
import static com.ir.homework.hw1.Constants.ENABLE_PERSISTENT_CACHE;
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

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;

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
		
		this.enableBulkProcessing = true;
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
	public Map<String,Float> getTermFrequency(String docNo){
		// TODO
		if(true) return null;
		Map<String,Float> result = null;
		SearchResponse response = _client.prepareSearch()
			.setIndices(this.indices)
			.setTypes(this.types)
			.setQuery(QueryBuilders.idsQuery(this.types)
					.addIds(docNo))
			.addScriptField("TERMS", (new Script("doc['" + textFieldName + "']")))
			.setSize(10000)
			.setNoFields()
			.get();
		
		if(response.status() == RestStatus.OK){
			result = new HashMap<String,Float>();
			SearchHit hit[]=response.getHits().hits();
			for(SearchHit h:hit){
				SearchHitField shf  = h.getFields().get("TERMS");
				Float score = 0.0F;
				for(Object sh : shf){
					String key = sh.toString();
					result.put(key, score);
				}
			}
		}
		return result;
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
		SearchResponse response = _client.prepareSearch()
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
	
	public List<String> getSignificantTerms(List<String> term, Integer numberOfTerm){
		List<String> result = new LinkedList<String>(); 
		
		// Get query term document count
		SearchResponse response = _client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.setQuery(QueryBuilders.termsQuery(textFieldName, term))
				.addAggregation(AggregationBuilders.significantTerms("SIG_TERM")
						.field(textFieldName))
				.setSize(numberOfTerm)
				.setNoFields()
				.get();
		
		if(response.status() == RestStatus.OK){
			SignificantTerms sigTerms = response.getAggregations().get("SIG_TERM");
			for(SignificantTerms.Bucket entry : sigTerms.getBuckets()){
				String key   = entry.getKey().toString();
				result.add(key);      // Term
			}
		}
		return result;
	}
	
	public static void main(String arg[]){
		ElasticClientBuilder eBuilder = ElasticClientBuilder.createElasticClientBuilder()
				.setClusterName(CLUSTER_NAME)
				.setHost(HOST)
				.setPort(PORT)
				.setIndices(INDEX_NAME)
				.setTypes(INDEX_TYPE)
				.setLimit(MAX_RESULTS)
				.setCachedFetch(false)
				.setField(TEXT_FIELD_NAME);
		
		eBuilder.build().getTermFrequency("AP890512-0154");
	}
}
