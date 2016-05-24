/**
 * 
 */
package com.ir.homework.hw1.elasticclient;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;


/**
 * @author shabbirhussain
 *
 */
public interface ElasticClient {
	// --------------------------- Loaders ----------------------------
	
	/**
	 * Loads data into index
	 * @param id unique identifier of document
	 * @param source data to be loaded in JSON format
	 */
	 void loadData(String id, XContentBuilder source);
	
	/**
	 * Commits the data to index
	 */
	 void commit();
	
	// --------------------------- Getters ----------------------------
	/**
	 * Gets average document length
	 * @return Average number of terms present in corpus per document
	 */
	Float getAvgDocLen();
	
	/**
	 * Gets vocabulary size
	 * @return Number of unique words in the corpus
	 */
	Long getVocabSize();
	
	// ------------------------- Document Statistics ------------------
	/**
	 * Gets term frequency
	 * @param docNo document to be searched for
	 * @return Term and count mapping for the given document
	 */
	Map<String,Float> getTermFrequency(String docNo);
	
	/**
	 * Gets document length in terms of count of terms
	 * @param docNo to be searched for
	 * @return Number of terms in given document 
	 */
	Long getTermCount(String docNo);
	
	// ---------------------- Term statistics -------------------------\
	/**
	 * Gets document frequency
	 * @param term to be searched for
	 * @return Document and count mapping for the given term
	 * @throws IOException 
	 */
	Map<String,Float> getDocFrequency(String term) throws IOException;

	
	/**
	 * Fetches maximum results to be fetched
	 * @return maximum results configured during client initialization
	 */
	Integer getMaxResults();
	
	/**
	 * Gets document count of term
	 * @param term to be searched
	 * @return Number of documents term has been found
	 * @throws IOException 
	 */
	Long getDocCount(String term) throws IOException;
	
	/**
	 * Fetches and cashes document count from cache or generates and stores it
	 * @return Number of documents in corpus
	 */
	Long getDocCount();
	
	/**
	 * Attaches client and bulk processor
	 * @param client
	 * @param bulkProcessor
	 */
	public ElasticClient attachClients(Client client, BulkProcessor bulkProcessor);
	
}
