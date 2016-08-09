package com.ir.homework.hw7.featureextraction.controllers;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;


abstract class AbstractFeatureExtractor implements FeatureExtractor {
	private static final long serialVersionUID = 1L;
	
	private String indices;
	private String types;
	private Client client;
	
	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @throws UnknownHostException 
	 */
	public AbstractFeatureExtractor(TransportClient client, String indices, String types) throws UnknownHostException{
		this.client     = client;
		this.indices    = indices;
		this.types      = types;
	}
	
	
	// ------------------------- Document Statistics ------------------
	/**
	 * Gets the term frequencies for the given document and the field
	 * @param docNo is the document number to search for
	 * @param textFieldName is the text field name to search for
	 * @return TermVectors of a document
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	protected Map<String,Double> getTermFrequency(String docNo, String textFieldName) throws IOException, InterruptedException, ExecutionException{
		Map<String,Double> result = new HashMap<String,Double>();
		
		TermVectorsResponse response = client.termVectors(
				(new TermVectorsRequest())
				.id(docNo)
				.index(this.indices)
				.type(this.types)
				.selectedFields(textFieldName))
			.get();
		
		org.apache.lucene.index.TermsEnum terms = response
				.getFields()
				.terms(textFieldName)
				.iterator();
		
		
		while(terms.next() != null){
			String term = terms.term().utf8ToString();
			Double value = ((Long)terms.totalTermFreq()).doubleValue();
			//System.out.println(term + "\t=" + value);
			result.put(term, value);
		}
	
		return result;
	}
	
	/**
	 * Retrieves the value for the given document and field
	 * @param docNo is the document number to search for
	 * @param fieldName is the field to retrieve the value
	 * @return Contents of the field if found otherwise null
	 */
	protected Object getValue(String docNo, String fieldName){
		Object result = null;
		
		SearchResponse response = client.prepareSearch()
				.setIndices(this.indices)
				.setTypes(this.types)
				.addField(fieldName)
				.setQuery(QueryBuilders.idsQuery(this.types)
						.addIds(docNo))
				.get();
		
		SearchHit[] hits = response.getHits().hits();
		for(SearchHit h:hits){
			result  = h.getFields().get(fieldName).getValue();
		}
		return result;
	}
	
	/**
	 * Builds a feature name from the text given. Feature name follows standards of not containing illegal characters
	 * @param text is the feature text to be converted
	 * @return Feature name
	 */
	protected String getFeatName(String text){
		return text.replaceAll("\\W", "_");
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
}
