package com.ir.homework.hw5.evaluators;

import java.io.PrintStream;
import java.util.Map;

import com.ir.homework.hw5.models.ModelQrel;
import com.ir.homework.hw5.models.ModelQres;

public interface Evaluator {
	
	/**
	 * Prints summary results to the output stream
	 * @param out is the output stream results should be printed
	 */
	void execute(PrintStream out);
	
	/**
	 * Initializes the evaluator
	 * @param qrel is the qrel model
	 * @param qres is the qres model
	 */
	void initialize(Map<String, ModelQrel> qrel, Map<String, ModelQres> qres);
}
