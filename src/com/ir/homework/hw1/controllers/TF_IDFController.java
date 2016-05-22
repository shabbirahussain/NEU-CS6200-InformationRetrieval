/**
 * 
 */
package com.ir.homework.hw1.controllers;

import static com.ir.homework.hw1.Constants.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.controllers.util.SearchControllerCache;
import com.ir.homework.io.OutputWriter.OutputRecord;


/**
 * @author shabbirhussain
 *
 */
public class TF_IDFController extends BaseSearchController implements SearchController{
	
	/**
	 * constructor for re using cache across controllers
	 * @param searchCache search cache object
	 * @param maxResults maximum number of results
	 */
	public TF_IDFController(SearchControllerCache searchCache, Integer maxResults){
		super(searchCache, maxResults);
	}
	
	
	@Override
	public List<OutputRecord> executeQuery(Entry<String, String[]> query) {
		try {
			String queryNo   = query.getKey();
			String []queryTerms = query.getValue();
			
			Map<String, Float> docScore = new HashMap<String, Float>();
			for(String term: queryTerms){
				Map<String, Float> tf = searchCache.getTermFrequency(term);
				
				for(Entry<String, Float> tfe: tf.entrySet()){
					String docNo = tfe.getKey();
					
					Float tf_w_d    = tfe.getValue();
					Float len_d     = super.searchCache.getDocLength(docNo);
					Float avg_len_d = super.searchCache.getAvgDocLength();
					
					Long  D   = super.searchCache.getDocumentCount();
					Long  df_w = super.searchCache.getTermDocCount(term);
					
					Float tfidf_d_q   = docScore.getOrDefault(docNo, 0.0F);
					/**TF-IDF
					 * This is the second vector space model. The scoring function is as follows.
					 *  $$ okapi\_tf(w, d) = \frac{tf_{w,d}}{tf_{w,d} + 0.5 + 1.5 \cdot (len(d) / avg(len(d)))} $$
					 *  Where:
					 *  	$tf_{w,d}$ is the term frequency of term $w$ in document $d$
					 *  	$len(d)$ is the length of document $d$
					 *  	$avg(len(d))$ is the average document length for the entire corpus
					 *  
					 *  $$ tfidf(d, q) = \sum_{w \in q} okapi\_tf(w, d) \cdot \log \frac{D}{df_w} $$
					 *  Where:
					 *  	$D$ is the total number of documents in the corpus
					 *  	$df_w$ is the number of documents which contain term $w$
					 */
					Float okapi_tf = (float) (tf_w_d / (tf_w_d + 0.5 + 1.5*(len_d/avg_len_d)));
					okapi_tf = super.sigmoidSmoothing(okapi_tf);
					tfidf_d_q += (float) (okapi_tf * Math.log(D / df_w));

					docScore.put(docNo, tfidf_d_q);
				}
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start = System.nanoTime();
		
		SearchControllerCache sc = new SearchControllerCache(INDEX_NAME, INDEX_TYPE, MAX_RESULTS, TEXT_FIELD_NAME);
		
		TF_IDFController okc = new TF_IDFController(sc, MAX_RESULTS);

		Map<String, String[]> queries = new HashMap<String, String[]>();
		queries.put("test-key", (new String[]{"cat", "dog"}));
		for(Entry<String, String[]> q : queries.entrySet()){
			okc.executeQuery(q);
		}
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}
}
