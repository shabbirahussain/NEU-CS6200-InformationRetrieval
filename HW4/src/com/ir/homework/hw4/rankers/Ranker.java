package com.ir.homework.hw4.rankers;


public interface Ranker {
	/**
	 * Ranks one iteration of page rank
	 */
	public void rankPages();

	/**
	 * Checks if ranking has converged or not
	 * @param tollerance is the number of iteration to check
	 * @return True iff ranking has converged
	 */
	Boolean isConverged(Integer tollerance);
	
	/**
	 * Prints top n results
	 * @param n is the number of top pages to print
	 */
	void printTopPages(Integer n);
}
