package com.ir.homework.hw1.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.concurrent.ExecutionException;

import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.io.OutputWriter;

import opennlp.tools.stemmer.PorterStemmer;

public class QueryAugmentor {
	private ElasticClient searchClient;
	private PorterStemmer stemer;
	private Integer toIndex;
	private Integer numOfSignTerms;
	private Float expansionThreshold;
	private Set<String> stopWordsSet;
	
	/**
	 * Default constructor
	 * @param searchClient elastic search client to use
	 * @param stopWordsFilePath is the path of stop words file
	 */
	public QueryAugmentor(ElasticClient searchClient, String stopWordsFilePath){
		this.searchClient = searchClient;
		this.stemer = new PorterStemmer();
		
		try {
			this.stopWordsSet = geStopWords(stopWordsFilePath);
		} catch (IOException e) {e.printStackTrace();}
		
		this.toIndex = 3;
		this.numOfSignTerms = 5;
		this.expansionThreshold = 0.5F;
	}
	
	/**
	 * Expands query using top n documents
	 * @param query given as a pair of key and tokens
	 * @param outputRecord top records in output
	 * @return Tokenized query as an Entry 
	 * @throws IOException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public Entry<String, String[]> expandQuery(Entry<String, String[]> query, List<OutputWriter.OutputRecord> outputRecord) throws IOException, InterruptedException, ExecutionException{
		Map<String, Long> termMap = new HashMap<String, Long>(); 
		List<String> terms = Arrays.asList(query.getValue());
		
		for(int i=0; i<this.toIndex && i<outputRecord.size(); i++){
			Set<String> sudoTerms = this.searchClient
					.getTermFrequency(outputRecord.get(i).docNo, 5F, 10F)
					.keySet();
			System.out.println("Processing doc=" + outputRecord.get(i).docNo + "\tsize=" + sudoTerms.size() + "\tmapsize=" + termMap.size());
			for(String sudoTerm: sudoTerms){
				sudoTerm = sudoTerm.replaceAll("[^a-z]", "");
				if(terms.contains(sudoTerm)) continue;
				Long sudoScore = this.searchClient.getDocCount(sudoTerm);
				
				termMap.put(sudoTerm, sudoScore);
			}
		}
		String[] qTerms = sortByValue(termMap).subList(0, this.toIndex).toArray(new String[0]);
		
		query.setValue(qTerms);
		return query;
	}
	
	/**
	 * Expands query using top n documents
	 * @param query given as a pair of key and tokens
	 * @param outputRecord top records in output
	 * @return Tokenized query as an Entry 
	 * @throws IOException 
	 */
	public Entry<String, String[]> expandQuery(Entry<String, String[]> query) throws IOException{
		List<String> terms  = Arrays.asList(query.getValue());
		List<Float>  score  = new LinkedList<Float>();
		List<String> sTerms = new LinkedList<String>();
		
		Float sampleSpace = 0F;
		for(String term : terms){
			Float s = weightTermUniqeness(term);
			score.add(s);
			sampleSpace += s;
		}
		System.out.println("");
		for(int i=0;i<terms.size();i++){
			Float s = score.get(i) / sampleSpace;
			System.out.print(s+"\t"+terms.get(i));
			if(s<this.expansionThreshold){
				System.out.print(" +");
				sTerms.addAll(searchClient.getSignificantTerms(terms.get(i), this.numOfSignTerms));
			}
			System.out.println("");
		}
		sTerms.addAll(terms);
		query.setValue(sTerms.toArray(new String[0]));
		
		return query;
	}
	
	/**
	 * Assigns appropriate score to each term bases on its rarity
	 * @param term string term to be evaluated against corpus
	 * @return Term significance
	 * @throws IOException 
	 */
	private Float weightTermUniqeness(String term) throws IOException{
		Float result = null;
		Long termDocCnt = this.searchClient.getDocCount(term)+1;
		
		result = ((Double)(1.0 / termDocCnt)).floatValue();
		
		return result;
	}
	
	/**
	 * Adds escape characters to the query
	 * @param query
	 * @return Escaped version of given query
	 */
	public Entry<String, String[]> escapeQuery(Entry<String, String[]> query){
		List<String> terms  = Arrays.asList(query.getValue());
		Set<String>  result = new HashSet<String>();
		
		for(String term : terms){
			result.add(term.replaceAll("[^a-z]", " "));
		}
		
		query.setValue(result.toArray(new String[0]));
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
	
	/**
	 * Removes stop words from query
	 * @param query given as a pair of key and tokens
	 * @return Tokenized query as an Entry 
	 * @throws IOException 
	 */
	public Entry<String, String[]> cleanStopWordsFromQuery(Entry<String, String[]> query) throws IOException{
		List<String> tokens = Arrays.asList(query.getValue());
		Set<String> uTokens = new HashSet<String>();
		
		for(String t: tokens){
			if(!this.stopWordsSet.contains(t.trim())) uTokens.add(t);
		}
		query.setValue(uTokens.toArray(new String[0]));
		
		return query;
	} 
	
	/**
	 * Reads and returns list of stop words
	 * @param stopFilePath is the full path of stopwords file
	 * @return Set of stop words
	 * @throws IOException 
	 */
	private Set<String> geStopWords(String stopFilePath) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stopFilePath)));
		Set<String> result = new HashSet<String>();
		
		String line;
		while((line=br.readLine())!= null){
			line = line.trim();
			result.add(line);
		}
		
		return result;
	}
}
