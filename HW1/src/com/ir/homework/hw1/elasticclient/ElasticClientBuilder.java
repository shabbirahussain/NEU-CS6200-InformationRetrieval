package com.ir.homework.hw1.elasticclient;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import static com.ir.homework.hw1.Constants.*;

public class ElasticClientBuilder {
	private Builder settings;
	private Boolean cachedFetch, customFetch;
	private String  host, indices, types, field;
	private Integer port, size;
	
	/** 
	 * Default constructor
	 * @return ElasticClientBuilder
	 */
	public static ElasticClientBuilder createElasticClientBuilder(){
		ElasticClientBuilder result = new ElasticClientBuilder();
		result.settings = Settings.settingsBuilder()
				.put("client.transport.ignore_cluster_name", false)
		        .put("node.client", true)
		        .put("client.transport.sniff", true);
		return result;
	}

	/**
	 * Sets cluster name
	 * @param clusterName
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setClusterName(String clusterName){
		this.settings.put("cluster.name", clusterName);
		return this;
	}
	
	/**
	 * Sets hosts of the elastic search
	 * @param host name
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setHost(String host){
		this.host = host;
		return this;
	}
	
	/**
	 * Sets port of the elastic search
	 * @param port number
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setPort(Integer port){
		this.port = port;
		return this;
	}
	
	/**
	 * Sets index to query
	 * @param indices name to query 
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setIndices(String indices){
		this.indices  = indices;
		return this;
	}
	
	/**
	 * Sets the type to query
	 * @param types name to query
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setTypes(String types){
		this.types = types;
		return this;
	}
	
	/**
	 * Sets maximum number of records to fetch
	 * @param size of query output to limit
	 * @return 
	 */
	public ElasticClientBuilder setLimit(Integer size){
		this.size = size;
		return this;
	}
	
	/**
	 * Sets the payload field to query
	 * @param field name to query against
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setField(String field){
		this.field = field;
		return this;
	}

	/**
	 * Sets the cached retrieval method on/off
	 * @param enable
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setCachedFetch(Boolean enable){
		this.cachedFetch = enable;
		return this;
	}
	
	/**
	 * Sets the custom retrieval method on/off
	 * @param enable
	 * @return ElasticClientBuilder
	 */
	public ElasticClientBuilder setCustomFetch(Boolean enable){
		this.customFetch = enable;
		return this;
	}
	
	// ------------------------ Builder -------------------------------
	
	/**
	 * Builds a new client from information provided so far
	 * @return ElasticClient
	 */
	public ElasticClient build(){
		ElasticClient result = new BaseElasticClient(this.indices, this.types, ENABLE_BULK_INSERT, this.size, this.field);
		
		if(this.customFetch)
			result = new Hw2IndexClient(this.indices, this.types, ENABLE_BULK_INSERT, this.size, this.field);
		
		if (this.cachedFetch)
			result = new CachedElasticClient(result);
		
		this.build(result);
		return result;
	}
	
	/**
	 * Builds transport clients and initialized them on elasticClient
	 * @param elasticClient
	 * @return
	 */
	public ElasticClient build(ElasticClient elasticClient){
		try {
			Client client = TransportClient.builder().settings(settings.build()).build()
			        .addTransportAddress(
			        		new InetSocketTransportAddress(InetAddress.getByName(this.host), this.port));
			
			BulkProcessor bulkProcessor = BulkProcessor.builder(
			        client,  
			        new BulkProcessor.Listener() {
						@Override
						public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
							// TODO Auto-generated method stub
						}

						@Override
						public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
							// TODO Auto-generated method stub
							failure.printStackTrace();
						}

						@Override
						public void beforeBulk(long executionId, BulkRequest request) {
							// TODO Auto-generated method stub
						} 
			        })
			        .setBulkActions(10000) 
			        .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB)) 
			        .setFlushInterval(TimeValue.timeValueSeconds(5)) 
			        .setConcurrentRequests(1) 
			        .setBackoffPolicy(
			            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)) 
			        .build();
			
			elasticClient.attachClients(client, bulkProcessor);
		}catch (UnknownHostException e) {e.printStackTrace();}
		
		return elasticClient;
	}
}
