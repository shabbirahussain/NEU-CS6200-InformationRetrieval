/**
 * 
 */
package com.ir.homework.hw1.controllers;

import java.util.List;
import java.util.Map.Entry;

import com.ir.homework.io.OutputWriter;

/**
 * @author shabbirhussain
 *
 */
public interface SearchController {
	/**
	 * Executes the search using selected controller
	 * @param query in the form of an entry with key as query id and value as array of tokens 
	 * @return Result in the form of record
	 */
	public abstract List<OutputWriter.OutputRecord> executeQuery(Entry<String, String[]> query);
}
