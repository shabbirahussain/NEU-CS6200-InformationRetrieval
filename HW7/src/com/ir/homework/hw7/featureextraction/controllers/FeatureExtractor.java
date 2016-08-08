package com.ir.homework.hw7.featureextraction.controllers;

import java.io.Serializable;

import com.ir.homework.hw7.featureextraction.models.MFeature;

public interface FeatureExtractor extends Serializable{
	
	/**
	 * Extracts a list of features for given document ID
	 * @param docID is the given 
	 * @return Map of features and values
	 */
	MFeature getFeatures(String docID);
}
