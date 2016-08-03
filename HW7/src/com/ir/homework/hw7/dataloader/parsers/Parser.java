/**
 * 
 */
package com.ir.homework.hw7.dataloader.parsers;

import java.util.Map;

/**
 * @author shabbirhussain
 * Generic interface to be implemented by all parsers
 */
public interface Parser {
	/**
	 * Parses the file into a map
	 * @param data is the data to be parsed
	 * @return Map of fields and data
	 * @throws Exception 
	 */
	Map<String, Object> parse(String data) throws Exception;
	
	/**
	 * Cleans the given text as per internal rules
	 * @param text is the given content to parse
	 * @return String of cleaned content
	 */
	String cleanContent(String text);
	
}
