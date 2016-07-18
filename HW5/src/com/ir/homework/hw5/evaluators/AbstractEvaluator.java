package com.ir.homework.hw5.evaluators;

import java.io.PrintStream;

public abstract class AbstractEvaluator implements Evaluator {
	private String displayLabel;
	protected Double score;
	
	
	/**
	 * Default constructor
	 * @param displayLabel is the label that should be assigned to the results
	 */
	public AbstractEvaluator(String displayLabel){
		this.displayLabel = displayLabel;
	}
	
	public void printResults(PrintStream out){
		out.println(displayLabel + ": " + score );
	}
}
