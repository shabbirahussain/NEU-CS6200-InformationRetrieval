package com.ir.homework.hw7.featureextraction.controllers;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.client.transport.TransportClient;

import com.ir.homework.hw7.featureextraction.models.MFeature;

public class SearchFeatureExtractor extends AbstractFeatureExtractor {
	private static final long serialVersionUID = 1L;
	private String textFieldName;

	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param textFieldName is the field from which features has to be extracted
	 * @throws UnknownHostException
	 */
	public SearchFeatureExtractor(TransportClient client, String indices, String types, String textFieldName)
			throws UnknownHostException {
		super(client, indices, types);
		this.textFieldName = textFieldName;
	}

	@Override
	public MFeature getFeatures(String docID) {
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
