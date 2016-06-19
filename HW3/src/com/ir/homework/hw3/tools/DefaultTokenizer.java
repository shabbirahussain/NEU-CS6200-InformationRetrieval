/**
 * 
 */
package com.ir.homework.hw3.tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.PorterStemmer;

/**
 * @author shabbirhussain
 *
 */
public class DefaultTokenizer {
	private String regExp;
	private Set<String> stopWords;
	private Boolean stemmingEnabled;
	private PorterStemmer stemmer;
	
	/**
	 * Default constructor
	 * @param regExp is the regular expression for tokenizer
	 */
	public DefaultTokenizer(String regExp){
		this.regExp = regExp;
		this.stemmingEnabled = false;
		this.stemmer = new PorterStemmer();
	}
	
	/**
	 * Adds stop words to filter 
	 * @param stopWords is the set of stop words to filter
	 * @return DefaultTokenizer
	 */
	public DefaultTokenizer setStopWordsFilter(Set<String> stopWords){
		this.stopWords = stopWords;
		return this;
	}
	
	/**
	 * Sets stemming in tokenizer
	 * @param value Enable/Disable
	 * @return DefaultTokenizer
	 */
	public DefaultTokenizer setStemming(Boolean value){
		this.stemmingEnabled = value;
		return this;
	}
	
	public List<String> tokenize(String data){
		Pattern p = Pattern.compile(this.regExp);
		Matcher m = p.matcher(data);
		List<String> result = new LinkedList<String>();
		while (m.find()) {
			String term = m.group();
			if(this.stemmingEnabled) 
				term = stemmer.stem(term);
			if(stopWords.contains(term)) continue;
			
			result.add(term);
		}
		return result;
	}
}
