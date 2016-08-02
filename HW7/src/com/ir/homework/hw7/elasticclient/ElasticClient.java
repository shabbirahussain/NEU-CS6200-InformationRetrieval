/**
 * 
 */
package com.ir.homework.hw7.elasticclient;

import java.io.Flushable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.ir.homework.hw2.queryprocessing.QueryProcessor;


/**
 * @author shabbirhussain
 *
 */
public interface ElasticClient extends Flushable{
	// --------------------------- Loaders ----------------------------
	
	/**
	 * Loads data into index
	 * @param id unique identifier of document
	 * @param source is the data to be loaded in JSON format
	 */
	void loadData(String id, XContentBuilder source);
	
	/**
	 * Loads data into elastic search
	 * @param id unique identifier of document
	 * @param source is the data to be loaded in Map<String,Object> format
	 * @throws IOException
	 */
	void loadData(String id, Map<String, Object> source) throws IOException;
	

	/**
	 * Attached elastic client to the object
	 * @param client
	 * @return ElasticClient
	 */
	ElasticClient attachClients(Client client);
}
