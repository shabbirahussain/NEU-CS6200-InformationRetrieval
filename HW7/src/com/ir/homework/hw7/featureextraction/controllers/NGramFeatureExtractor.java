package com.ir.homework.hw7.featureextraction.controllers;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.client.transport.TransportClient;

import com.ir.homework.hw7.featureextraction.models.MFeature;

public class NGramFeatureExtractor extends AbstractFeatureExtractor {
	private static final long serialVersionUID = 1L;
	private String textFieldName;
	private String analyzer;

	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param textFieldName is the field from which features has to be extracted
	 * @throws UnknownHostException
	 */
	public NGramFeatureExtractor(TransportClient client, String indices, String types, String textFieldName)
			throws UnknownHostException {
		super(client, indices, types);
		this.textFieldName = textFieldName;
		this.analyzer = null;
	}
	
	/**
	 * Default constructor. It dynamically generates features from given analyzer
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param textFieldName is the field from which features has to be extracted
	 * @param analyzer is the analyzer to use to generate terms
	 * @throws UnknownHostException
	 */
	public NGramFeatureExtractor(TransportClient client, String indices, String types, String textFieldName, String analyzer)
			throws UnknownHostException {
		this(client, indices, types, textFieldName);
		this.analyzer = analyzer;
	}

	@Override
	public MFeature getFeatures(String docID) {
		if(this.analyzer == null) 
			return getFeaturesFromTermVec(docID);
		else
			return getFeaturesFromAnalyzer(docID);
	}
	
	/**
	 * Extracts features by dynamically generating them using given analyzer
	 * @param docID is the document id to use
	 * @return Map of features and values
	 */
	private MFeature getFeaturesFromAnalyzer(String docID){
		MFeature result = new MFeature();
		List<String> tokens = new LinkedList<>();
		
		try{
			String query = super.getValue(docID, this.textFieldName).toString();
			tokens = super.analyzeQuery(query, this.analyzer);
		}catch(Exception e){}
		
		
		for(String e: tokens){
			result.put(super.getFeatName(e), 1.0);
		}
		return result;
	}
	
	/**
	 * Extracts features from the term vectors
	 * @param docID is the document id to use
	 * @return Map of features and values
	 */
	private MFeature getFeaturesFromTermVec(String docID) {
		MFeature result = new MFeature();
		Map<String, Double> tfMap = new HashMap<String, Double>();
		
		try{
			tfMap = super.getTermFrequency(docID, textFieldName);
		}catch(Exception e){}
		
		
		for(Entry<String, Double> e: tfMap.entrySet()){
			result.put(super.getFeatName(e.getKey()),
					tfSmoothing(e.getValue()));
		}
		return result;
	}
	
	/**
	 * Normalizes the tf count over the smoothing curve
	 * @param val is the value of tf to normalize
	 * @return Exponentially flattened values for TF 
	 */
	private Double tfSmoothing(Double val){
		Double result = 2.0;
		result /=(1 + Math.exp(-val/3.0));
		
		return result;
	}
}
