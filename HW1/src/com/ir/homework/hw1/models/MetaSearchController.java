/**
 * 
 */
package com.ir.homework.hw1.models;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.io.OutputWriter.OutputRecord;


/**
 * @author shabbirhussain
 *
 */
public class MetaSearchController extends BaseSearchController{
	List<SearchController> controllers;
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 * @param controllers list of controllers who's output should be consented
	 */
	public MetaSearchController(ElasticClient elasticClient, List<SearchController> controllers){
		super(elasticClient);
		this.controllers = controllers;
	}
	
	
	@Override
	public List<OutputRecord> executeQuery(Entry<String, String[]> query) {
		String queryNo   = query.getKey();
		
		Map<String, Float> docScore; 
		List<List<OutputRecord>> controllerOutputs = new LinkedList<List<OutputRecord>>();
	
		// get votes from all controllers
		for(SearchController sc : controllers){
			// skip recursive calls
			if(sc.getClass().equals(this.getClass())) continue;
			
			// get output from given controller
			controllerOutputs.add(sc.executeQuery(query));
		}
		
		// conduct voting to fetch best documents
		docScore = meanDeviationEvaluator(controllerOutputs);
		
		return super.prepareOutput(queryNo, docScore);
	}
	
	/**
	 * Evaluates evaluation winner based on standard deviation
	 * @param controllerOutputs votes of different search 
	 * @return
	 */
	private Map<String, Float> meanDeviationEvaluator(List<List<OutputRecord>> controllerOutputs){
		Map<String, Float> docScore = new HashMap<String, Float>();
		for(List<OutputRecord> lor : controllerOutputs){
			Integer median = (lor.size()/2);
			for(int i=0; i<lor.size(); i++){
				String docNo = lor.get(i).docNo;
				Float score  = docScore.getOrDefault(docNo, 0F);
				score +=  (median - i);
				
				docScore.put(docNo, score);
			}
		}
		return docScore;
	}
}
