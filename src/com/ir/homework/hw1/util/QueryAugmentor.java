package com.ir.homework.hw1.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.io.OutputWriter;

import opennlp.tools.stemmer.PorterStemmer;

public class QueryAugmentor {
	private ElasticClient searchClient;
	private PorterStemmer stemer;
	private Integer toIndex;
	
	/**
	 * Default constructor
	 * @param searchClient elastic search client to use
	 */
	public QueryAugmentor(ElasticClient searchClient){
		this.searchClient = searchClient;
		this.stemer = new PorterStemmer();
		
		this.toIndex = 1;
	}
	
	/**
	 * Expands query using top n documents
	 * @param query given as a pair of key and tokens
	 * @param outputRecord top records in output
	 * @return Tokenized query as an Entry 
	 * @throws IOException 
	 */
	public Entry<String, String[]> expandQuery(Entry<String, String[]> query, List<OutputWriter.OutputRecord> outputRecord) throws IOException{
		Map<String, Long> termMap = new HashMap<String, Long>(); 
		List<String> terms = Arrays.asList(query.getValue());
		
		for(int i=0; i<this.toIndex && i<outputRecord.size(); i++){
			Set<String> sudoTerms = this.searchClient
					.getTermFrequency(outputRecord.get(i).docNo)
					.keySet();
			System.out.println("Processing doc=" + outputRecord.get(i).docNo + "\tsize=" + sudoTerms.size() + "\tmapsize=" + termMap.size());
			for(String sudoTerm: sudoTerms){
				sudoTerm = sudoTerm.replaceAll("[^a-z]", "");
				if(terms.contains(sudoTerm)) continue;
				System.out.println(sudoTerm);
				Long score = this.searchClient.getDocCount(sudoTerm);
				
				if(score>1) termMap.put(sudoTerm, score);
			}
		}
		String[] qTerms = sortByValue(termMap).subList(0, this.toIndex).toArray(new String[0]);
		
		query.setValue(qTerms);
		return query;
	}
	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Entry<String, Float>> sortByValue(Map map) {
	     List<Entry<String, Float>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	/**
	 * Stems the query and adds stemmed version
	 * @param query given as a pair of key and tokens
	 * @return Tokenized query as an Entry 
	 */
	public Entry<String, String[]> stemQuery(Entry<String, String[]> query){
		List<String> tokens  = Arrays.asList(query.getValue());
		Set<String> uTokens = new HashSet<String>();
		for(String t: tokens){
			uTokens.add(stemer.stem(t));
		}
		uTokens.addAll(tokens);
		query.setValue(uTokens.toArray(new String[0]));
		
		return query;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
