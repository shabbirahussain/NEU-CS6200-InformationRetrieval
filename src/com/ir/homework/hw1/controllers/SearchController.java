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
	public abstract List<OutputWriter.OutputRecord> executeQuery(Entry<String, String[]> query);
}
