package com.ir.homework.hw7.featureextraction.controllers;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.transport.TransportClient;

public class LabelFeatureExtractor extends AbstractFeatureExtractor {
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
	public LabelFeatureExtractor(TransportClient client, String indices, String types, String textFieldName)
			throws UnknownHostException {
		super(client, indices, types);
		this.textFieldName = textFieldName;
	}

	@Override
	public Map<String, Double> getFeatures(String docID) {
		Map<String, Double> result = new HashMap<String, Double>();
		Object val = null;
		try{
			val = super.getValue(docID, textFieldName);
		}catch(Exception e){}
		
		result.put(this.textFieldName, (Double)val);
		
		return result;
	}
}
