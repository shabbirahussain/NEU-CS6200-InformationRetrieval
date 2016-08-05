package com.ir.homework.hw7.featureextraction.controllers;

import java.io.Serializable;
import java.util.Map;

public interface FeatureExtractor extends Serializable{
	
	/**
	 * Extracts a list of features for given document ID
	 * @param docID is the given 
	 * @return Map of features and values
	 */
	Map<String, Double> getFeatures(String docID);
}
