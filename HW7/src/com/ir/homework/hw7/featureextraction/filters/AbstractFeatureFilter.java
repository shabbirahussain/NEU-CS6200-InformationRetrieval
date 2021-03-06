package com.ir.homework.hw7.featureextraction.filters;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

abstract class AbstractFeatureFilter implements FeatureFilter{
	protected String indices;
	protected String types;
	protected Client client;
	
	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @throws UnknownHostException 
	 */
	public AbstractFeatureFilter(TransportClient client, String indices, String types) throws UnknownHostException{
		this.client     = client;
		this.indices    = indices;
		this.types      = types;
	}
	
	/**
	 * Analyzes the query
	 * @param query is the query to use
	 * @param analyzer is the analyzer to use
 	 * @return List of terms/features
	 */
	protected List<String> analyzeQuery(String query, String analyzer){
		List<String> results = new LinkedList<String>();
		
		AnalyzeRequest request = (new AnalyzeRequest())
				.index(indices)
				.text(query)
				.analyzer(analyzer);
		try {
			List<AnalyzeResponse.AnalyzeToken> tokens = client.admin()
					.indices()
					.analyze(request)
					.get()
					.getTokens();
			
			for (AnalyzeResponse.AnalyzeToken token : tokens)
				results.add(this.getFeatName(token.getTerm()));
		} catch (Exception e) {}
		return results;
	}
	
	/**
	 * Builds a feature name from the text given. Feature name follows standards of not containing illegal characters
	 * @param text is the feature text to be converted
	 * @return Feature name
	 */
	protected String getFeatName(String text){
		return text.replaceAll("\\W", "_");
	}
}
