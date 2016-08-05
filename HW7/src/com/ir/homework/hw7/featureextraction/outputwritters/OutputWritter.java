package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.IOException;
import java.util.Map;

public interface OutputWritter {
	
	/**
	 * Prints the result to the output stream 
	 * @param label is the value of label to print
	 * @param featureMap is the map containing feature values
	 */
	void printResults(Double label, Map<String, Double> featureMap);
	
	/**
	 * Closes the writter
	 * @throws IOException 
	 */
	void close() throws IOException;
}
