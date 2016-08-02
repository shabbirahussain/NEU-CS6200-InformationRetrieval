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
	 * @param qID is the qurey ID of the query for which document has to be parsed
	 * @param filePath is the full path of the file to process
	 * @return Map of fields and data
	 * @throws Exception 
	 */
	Map<String, Object> parseFile(String qID, String filePath) throws Exception;
}
