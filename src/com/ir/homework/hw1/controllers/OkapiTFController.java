/**
 * 
 */
package com.ir.homework.hw1.controllers;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.controllers.util.SearchControllerCache;
import com.ir.homework.io.OutputWriter.OutputRecord;
import static com.ir.homework.common.Constants.*;


/**
 * @author shabbirhussain
 *
 */
public class OkapiTFController extends BaseSearchController implements SearchController{
	
	/**
	 * constructor for re using cache across controllers
	 * @param searchCache search cache object
	 * @param maxResults maximum number of results
	 */
	public OkapiTFController(SearchControllerCache searchCache, Integer maxResults){
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
					Float newScore  = tfe.getValue();
					Float docLen    = super.searchCache.getDocLength(docNo);
					Float avgDocLen = super.searchCache.getAvgDocLength();

					//$$ okapi\_tf(w, d) = \frac{tf_{w,d}}{tf_{w,d} + 0.5 + 1.5 \cdot (len(d) / avg(len(d)))} $$
					newScore = (float) (newScore / Math.round(newScore + 0.5 + 1.5*(docLen/avgDocLen)));
					
					// Normalize score for multiple instances 
					newScore = (float) (1/(1+Math.pow(newScore, -1)));
					
					// Multiply with term uniqueness to weigh importance
					newScore = (float) (newScore * super.searchCache.getTermImportance(term));

					// $$ tf(d, q) = \sum_{w \in q} okapi\_tf(w, d) $$
					Float oldScore = docScore.getOrDefault(docNo, 0.0F);
					docScore.put(docNo, oldScore + newScore);
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
		
		OkapiTFController okc = new OkapiTFController(sc, MAX_RESULTS);

		Map<String, String[]> queries = new HashMap<String, String[]>();
		queries.put("test-key", (new String[]{"cat", "dog"}));
		for(Entry<String, String[]> q : queries.entrySet()){
			okc.executeQuery(q);
		}
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}
}
