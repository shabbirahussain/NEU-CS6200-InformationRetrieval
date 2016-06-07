package com.ir.homework.hw1.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public final class StopWordReader {
	private String stopWordsFilePath;
	/**
	 * Default constructor
	 * @param stopWordFilePath is the path of stop words file
	 */
	public StopWordReader(String stopWordsFilePath){
		this.stopWordsFilePath = stopWordsFilePath;
	}
	

	/**
	 * Reads and returns list of stop words
	 * @return Set of stop words
	 * @throws IOException 
	 */
	public Set<String> getStopWords() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.stopWordsFilePath)));
		Set<String> result = new HashSet<String>();
		
		String line;
		while((line=br.readLine())!= null){
			line = line.trim();
			result.add(line);
		}
		br.close();
		return result;
	}
}
