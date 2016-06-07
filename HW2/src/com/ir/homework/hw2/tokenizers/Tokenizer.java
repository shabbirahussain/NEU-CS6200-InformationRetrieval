/**
 * 
 */
package com.ir.homework.hw2.tokenizers;

import java.util.List;

/**
 * @author shabbirhussain
 *
 */
public interface Tokenizer {
	/**
	 * Creates tokens out of data given to the function
	 * @param data is the raw string data  given
	 * @return List of tokens out of data
	 */
	public List<String> tokenize(String data);
}
