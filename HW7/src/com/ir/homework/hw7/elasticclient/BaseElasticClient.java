package com.ir.homework.hw7.elasticclient;


import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

public class BaseElasticClient implements Serializable, ElasticClient{
	// Serialization version Id
	private static final long serialVersionUID = 1L;
	
	public String indices;
	public String types;
	public Integer maxResults;
	public String  textFieldName;
	protected BulkRequestBuilder bulkRequest;

	protected static Client _client = null;
	protected static Stemmer stemer;
	
	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param bulkProcessor is the bulk processor client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param limit maximum number of records to fetch/Load
	 * @param field payload field name to query
	 */
	public BaseElasticClient(String indices, String types, Integer limit, String field ){
		this.indices = indices;
		this.types   = types;
		this.maxResults = limit;
		this.textFieldName = field;
		stemer = new PorterStemmer();
		
		
	}
	
	public ElasticClient attachClients(Client client){
		_client         = client;
		this.bulkRequest = _client.prepareBulk();
		return this;
	}

	// --------------------------- Loaders ----------------------------
	
	public void loadData(String id, XContentBuilder source){
		IndexRequestBuilder irBuilder = _client.prepareIndex()
				.setIndex(this.indices)
				.setType(this.types)
				.setId(id)
				.setSource(source);
		if(bulkRequest.numberOfActions() > maxResults) flush();
		bulkRequest.add(irBuilder.request());
	}
	
	public void loadData(String id, Map<String, Object> source) throws IOException{
		XContentBuilder builder = jsonBuilder().startObject();
		for(Entry<String, Object> e: source.entrySet()){
			builder.field(e.getKey(), e.getValue());
		}
		builder.endObject();
		this.loadData(id, builder);
	}
	
	public void flush(){
		if(bulkRequest.numberOfActions()>0)
			bulkRequest.get();
		bulkRequest = _client.prepareBulk();
	}
}
