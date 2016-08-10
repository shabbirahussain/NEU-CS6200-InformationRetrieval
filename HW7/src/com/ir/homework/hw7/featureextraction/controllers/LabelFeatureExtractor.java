package com.ir.homework.hw7.featureextraction.controllers;

import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;

import com.ir.homework.hw7.featureextraction.models.MFeature;

public class LabelFeatureExtractor extends AbstractFeatureExtractor {
	private static final long serialVersionUID = 1L;
	private String textFieldName;
	private Double defaultLabel;

	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @param textFieldName is the field from which features has to be extracted
	 * @param defaultLabel is the default label to assign in case actual label is missing
	 * @throws UnknownHostException
	 */
	public LabelFeatureExtractor(TransportClient client, String indices, String types, String textFieldName, Double defaultLabel)
			throws UnknownHostException {
		super(client, indices, types);
		this.textFieldName = textFieldName;
	}
	

	@Override
	public MFeature getFeatures(String docID) {
		MFeature result = new MFeature();
		Object val = null;
		try{
			val = super.getValue(docID, textFieldName);
			if(val != null) val = this.defaultLabel;
		}catch(Exception e){}
		
		result.put(this.textFieldName, (Double)val);
		
		return result;
	}
}
