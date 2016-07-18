package com.ir.homework.hw5.evaluators;

import java.util.Map;

import com.ir.homework.hw5.models.ModelRelevance;

public class PrecisionEvaluator extends AbstractEvaluator {
	/**
	 * Default Constructor
	 * @param displayLabel is the label to be displayed when result is displayed
	 * @param k is the number of top documents to examine
	 */
	public PrecisionEvaluator(String displayLabel) {
		super(displayLabel);
	}

	public Double calculateScore(Map<String, ModelRelevance> qrel, Map<String, ModelRelevance> qresults) {
		
		
		
		return null;
	}

}
