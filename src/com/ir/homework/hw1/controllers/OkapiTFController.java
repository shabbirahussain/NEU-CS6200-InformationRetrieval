/**
 * 
 */
package com.ir.homework.hw1.controllers;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static com.ir.homework.common.Constants.*;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;



import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.ir.homework.io.OutputWriter.OutputRecord;

/**
 * @author shabbirhussain
 *
 */
public class OkapiTFController extends SearchController{
	
	public OkapiTFController(String index, String type) {
		super(index, type);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.nanoTime();
		
		OkapiTFController okc = new OkapiTFController(INDEX_NAME, INDEX_TYPE);
		
		Map<String, String[]> queries = new HashMap<String, String[]>();
		queries.put("test-key", (new String[]{"cat", "dog"}));
		for(Entry<String, String[]> q : queries.entrySet()){
			okc.executeQuery(q);
		}
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}

	@Override
	public OutputRecord executeQuery(Entry<String, String[]> q) {
		try {
			/*
				{
				    "size": 0, 
				    "aggs" : {
				      "average_terms" : {
				        "avg" : {
				          "script": "doc['TEXT'].values.size()"}}
				    }
				}
			 * */
			XContentBuilder builder = jsonBuilder()
					.startObject()
						//.field("size", 0)
						.field("query")
				        .field("aggs").startObject()
				        	.field("average_terms").startObject()
				        		.field("avg").startObject()
					        		.field("script", "doc['TEXT'].values.size()")
				        		.endObject()
					        .endObject()
			        	.endObject()
				    .endObject();
			
			System.out.println(builder.prettyPrint().string());
			SearchResponse response1 = client.prepareSearch(index)
					.setTypes(type)
					.setQuery(builder)
					.get();
			
			System.out.println(response1.getHits().getTotalHits());
			
			/*
			SearchResponse response = client.prepareSearch(index)
					.setTypes(type)
					.setQuery("{\"query\" : {\"match_all\" : {}},"
							+ "\"aggs\" : {\"avg_size\" : {\"avg\" : {\"terms\" : {\"TEXT\" : \"_size\"}}}")
					.get();
			//*/
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
		
	}
	
	
}
