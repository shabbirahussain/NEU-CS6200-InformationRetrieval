package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.IOException;
import java.util.Map;

public interface OutputWritter {
	
	/**
	 * Prints the result to the output stream 
	 * @param label is the value of label to print
	 * @param featureMap is the map containing feature values
	 * @throws IOException 
	 */
	void printResults(Double label, Map<String, Double> featureMap) throws IOException;
	
	/**
	 * Closes the writter
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	void close() throws IOException, ClassNotFoundException;
}
