package com.ir.homework.hw1;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	public static final String DATA_PATH = BASE_PATH + "/AP_DATA/ap89_collection";
	public static final String PRE_PROCESS_SRC_PATH = BASE_PATH + "/AP_DATA_RO/ap89_collection";
	public static final String PRE_PROCESS_DST_PATH = BASE_PATH + "/AP_DATA/ap89_collection";

	public static final String OBJECT_STORE_PATH = BASE_PATH + "/AP_DATA/cache/";
	public static final String QUERY_FILE_PATH   = BASE_PATH + "/AP_DATA/query_desc.51-100.short.txt";
	
	public static final String OUTPUT_FOLDR_PATH   = BASE_PATH + "/AP_DATA/results";
	public static final String OUTPUT_FILE_PATH    = OUTPUT_FOLDR_PATH + "/output1000_";
	public static final String TRECK_EVAL_PATH     = OUTPUT_FOLDR_PATH + "/trec_eval";
	public static final String TRECK_EVAL_PARAMS[] = {OUTPUT_FOLDR_PATH + "/qrels.adhoc.51-100.AP89.txt"};
	
	
	
	// Word assosiations
	
	public static final String DATA_FILE_PREFIX = "ap";
	
	public static final String  HOST = "192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String CLUSTER_NAME = "dead-pool";
	
	public static final String INDEX_NAME = "ap_dataset";
	public static final String INDEX_TYPE = "document";
	
	public static final Integer      BULK_ACTIONS_SIZE = 10000;
	public static final ByteSizeUnit BULK_MEMORY_SIZE  = ByteSizeUnit.GB;
	public static final Boolean ENABLE_BULK_INSERT = true;

	public static final Boolean ENABLE_TF_CACHE = true;
	public static final Boolean ENABLE_PERSISTENT_CACHE = true;
	public static final Boolean ENABLE_SILENT_MODE = true;
	public static final Boolean ENABLE_STEMMING = false;
	
	public static final Integer MAX_RESULTS = 500;
	
	
	
	public static final String TEXT_FIELD_NAME = "TEXT";
	
	// ---------------------------------- Dynamic Initializations ----------------------------------------
	
	public static Client client = null;
	public static BulkProcessor bulkProcessor = null;
	
	// Build Elastic search Client and Bulk processor
	static{
		try {
			Settings settings = Settings.settingsBuilder()
			        .put("cluster.name", CLUSTER_NAME)
			        .put("client.transport.ignore_cluster_name", false)
			        .put("node.client", true)
			        .put("client.transport.sniff", true)
			        .build();
			
			client = TransportClient.builder().settings(settings).build()
			        .addTransportAddress(
			        		new InetSocketTransportAddress(InetAddress.getByName(HOST), PORT));
			
			bulkProcessor = BulkProcessor.builder(
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
			        .setBulkActions(BULK_ACTIONS_SIZE) 
			        .setBulkSize(new ByteSizeValue(1, BULK_MEMORY_SIZE)) 
			        .setFlushInterval(TimeValue.timeValueSeconds(5)) 
			        .setConcurrentRequests(1) 
			        .setBackoffPolicy(
			            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)) 
			        .build();
		} catch (UnknownHostException e) {e.printStackTrace();}
	}
}
