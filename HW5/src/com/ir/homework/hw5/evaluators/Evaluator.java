package com.ir.homework.hw5.evaluators;

import java.io.PrintStream;
import java.util.Map;

import com.ir.homework.hw5.models.ModelRelevance;

public interface Evaluator {
	
	/**
	 * Prints summary results to the output stream
	 * @param out is the output stream results should be printed
	 */
	void printResults(PrintStream out);
	
	/**
	 * Calculates the score for given collection of relevance and results
	 * @param qrel is the collection of relevance to compare results against
	 * @param qresults is the results to evaluate for quality
	 * @return Quality score of the model
	 */
	Double calculateScore(Map<String, ModelRelevance> qrel, Map<String, ModelRelevance> qresults); 
}
