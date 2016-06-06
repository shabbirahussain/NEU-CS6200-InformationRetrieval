package com.ir.homework.hw1.util;

import java.io.IOException;
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
	private Set<String> stopWordsSet;
	
	/**
	 * Default constructor
	 * @param searchClient elastic search client to use
	 * @param stopWordsFilePath is the path of stop words file
	 */
	public QueryAugmentor(ElasticClient searchClient, Set<String> stopWordsSet){
		this.searchClient = searchClient;
		this.stemer = new PorterStemmer();
		this.stopWordsSet = stopWordsSet;
		
		this.toIndex = 3;
		this.numOfSignTerms = 3;
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
		// TODO 
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
		// Stem original query 
		query = this.stemQuery(query);
		
		List<String> terms  = Arrays.asList(query.getValue());
		Map<String, Double> sTermsMap = new HashMap<String, Double>();
		
		for(int i=0;i<terms.size();i++){
			String term = terms.get(i);
			Double termBGProb = searchClient.getBGProbability(term);
			
			List<String> sigTermsRaw = searchClient.getSignificantTerms(term, 10);
			if(sigTermsRaw !=null ) {
				for(String sTerm : sigTermsRaw){
					Double sTermBGProb = searchClient.getBGProbability(sTerm);
					
					// new terms should increase the domain not limit them ?
					if(termBGProb<sTermBGProb){
						Double termScore = sTermsMap.getOrDefault(sTerm, 1.0);
						termScore += sTermBGProb;
						termScore /=2.0; // more terms better the score
						sTermsMap.put(sTerm, termScore);
					}	
				}
			}
		}
		
		// Rank terms with rarity
		/*Map<String, Float> termMap = new HashMap<String, Float>();
		for(String term: sTerms){
			Float s = weightTermUniqeness(term);
			termMap.put(term, s);
		}//*/
		
		List<Entry<String, Float>> sortedMap = this.sortByValue(sTermsMap);
		//System.out.println(sortedMap);
		
		List<String> sTerms = new LinkedList<String>();
		Integer i=0;
		for(Entry<String, Float> e : sortedMap){
			String term = e.getKey();
			sTerms.add(term);
			if((++i) >= numOfSignTerms) break;
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
		
		result = ((Double)(1000.0 / termDocCnt)).floatValue();
		
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
		//uTokens.addAll(tokens);
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
}
