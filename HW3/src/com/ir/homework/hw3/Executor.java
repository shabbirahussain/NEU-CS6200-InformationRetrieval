/**
 * 
 */
package com.ir.homework.hw3;

import static com.ir.homework.hw3.Constants.*;

import java.util.HashSet;
import java.util.Set;

import com.ir.homework.hw3.elasticclient.ElasticClient;
import com.ir.homework.hw3.io.StopWordReader;
import com.ir.homework.hw3.processes.FrontierTruncator;
import com.ir.homework.hw3.processes.WebCrawler;
import com.ir.homework.hw3.tools.DefaultTokenizer;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

/**
 * @author shabbirhussain
 *
 */
public final class Executor{
	private static ElasticClient    _elasticClient;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Initalizing
		DefaultTokenizer tokenizer = new DefaultTokenizer(TOKENIZER_REGEXP)
				.setStemming(true)
				.setStopWordsFilter((new StopWordReader(STOP_WORDS_FILE_PATH))
						.getStopWords());
		
		Set<String> queryTerms = new HashSet<String>();
		Stemmer stemmer = new PorterStemmer();
		for(String term : QUERY_TERMS){
			queryTerms.add(stemmer.stem(term).toString());
		}
		ElasticClient elasticClient = new ElasticClient();
		
		// Create threads
		(new FrontierTruncator(_elasticClient, TRUNCATION_INTERVAL)).start();
	
		for(Short i=0;i<MAX_NO_THREADS; i++){
			(new WebCrawler(elasticClient,
					tokenizer,
					queryTerms)).start();
			try{Thread.sleep(1000); }catch(Exception e){}
		}
	}
	
	
	
	
	
	
	
}
