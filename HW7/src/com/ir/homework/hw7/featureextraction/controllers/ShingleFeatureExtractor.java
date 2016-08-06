package com.ir.homework.hw7.featureextraction.controllers;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;

import com.ir.homework.hw7.featureextraction.models.MFeature;

public class ShingleFeatureExtractor extends AbstractFeatureExtractor {
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
	public ShingleFeatureExtractor(TransportClient client, String indices, String types, String textFieldName)
			throws UnknownHostException {
		super(client, indices, types);
		this.textFieldName = textFieldName;
	}

	@Override
	public MFeature getFeatures(String docID) {
		MFeature result = new MFeature();
		Map<String, Double> tfMap = new HashMap<String, Double>();
		try {
			tfMap = super.getTermFrequency(docID, textFieldName);
		} catch (Exception e1) {}
		
		for(String e: tfMap.keySet()){
			result.put(e, 1.0);
		}
		return result;
	}
}
