package com.ir.homework.hw7.dataloader.models;


import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.Flushable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.ir.homework.hw7.dataloader.parsers.Parser;


public class MInputData implements Flushable{
	public String indices;
	public String types;
	public Integer maxResults;
	public Parser parser;
	
	protected BulkRequestBuilder bulkRequest;

	public Client client;
	
	/**
	 * Default constructor
	 * @param client is the elastic client
	 * @param parser is the parser used for parsing
	 * @param clusterName is the name of cluster
	 * @param host is the host name
	 * @param port is the port
	 * @param client is the transport client
	 * @param bulkProcessor is the bulk processor client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param limit maximum number of records to Load
	 * @throws UnknownHostException 
	 */
	public MInputData(TransportClient client, Parser parser, String indices, String types, Integer limit) throws UnknownHostException{
		this.client     = client;
		this.parser     = parser;
		this.indices    = indices;
		this.types      = types;
		this.maxResults = limit;
		
		this.bulkRequest = client.prepareBulk();
	}
	

	// --------------------------- Loaders ----------------------------
	
	public void storeData(String id, XContentBuilder source){
		IndexRequestBuilder irBuilder = client.prepareIndex()
				.setIndex(this.indices)
				.setType(this.types)
				.setId(id)
				.setSource(source);
		if(bulkRequest.numberOfActions() > maxResults) flush();
		bulkRequest.add(irBuilder.request());
	}
	
	public void storeData(String id, Map<String, Object> source) throws IOException{
		XContentBuilder builder = jsonBuilder().startObject();
		for(Entry<String, Object> e: source.entrySet()){
			builder.field(e.getKey(), e.getValue());
		}
		builder.endObject();
		this.storeData(id, builder);
	}
	
	public void flush(){
		if(bulkRequest.numberOfActions()>0)
			bulkRequest.get();
		bulkRequest = client.prepareBulk();
	}
}
