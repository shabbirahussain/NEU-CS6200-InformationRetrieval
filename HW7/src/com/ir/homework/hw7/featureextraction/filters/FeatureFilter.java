package com.ir.homework.hw7.featureextraction.filters;

import java.util.Map;

public interface FeatureFilter {
	/**
	 * Filters the given feature list with set criteria
	 * @param featureMap is the map of features to filter
	 * @return Map of features and values
	 */
	Map<String, Double> applyFilters(Map<String, Double> featureMap); 
}
