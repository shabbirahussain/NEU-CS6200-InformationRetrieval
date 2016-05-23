/**
 * 
 */
package com.ir.homework.hw1.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.elasticutil.ElasticClient;
import com.ir.homework.io.OutputWriter.OutputRecord;


/**
 * @author shabbirhussain
 *
 */
public class OkapiTFController extends BaseSearchController implements SearchController{
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 */
	public OkapiTFController(ElasticClient elasticClient){
		super(elasticClient);
	}
	
	
	@Override
	public List<OutputRecord> executeQuery(Entry<String, String[]> query) {
		try {
			String queryNo   = query.getKey();
			String []queryTerms = query.getValue();
			
			Map<String, Float> docScore = new HashMap<String, Float>();
			for(String term: queryTerms){
				Map<String, Float> tf = elasticClient.getTermFrequency(term);
				
				for(Entry<String, Float> tfe: tf.entrySet()){
					String docNo = tfe.getKey();
					
					Float tf_w_d    = tfe.getValue();
					Float len_d     = super.elasticClient.getDocLength(docNo);
					Float avg_len_d = super.elasticClient.getAvgDocLength();

					Float tf_d_q = docScore.getOrDefault(docNo, 0.0F);
					/**Okapi TF
					 * This is a vector space model using a slightly modified version of TF to score documents. The Okapi TF score for term $w$ in document $d$ is as follows.
					 *  $$ okapi\_tf(w, d) = \frac{tf_{w,d}}{tf_{w,d} + 0.5 + 1.5 \cdot (len(d) / avg(len(d)))} $$
					 *  Where:
					 *  	$tf_{w,d}$ is the term frequency of term $w$ in document $d$
					 *  	$len(d)$ is the length of document $d$
					 *  	$avg(len(d))$ is the average document length for the entire corpus
					 *  
					 *  The matching score for document $d$ and query $q$ is as follows.
					 *  $$ tf(d, q) = \sum_{w \in q} okapi\_tf(w, d) $$
					 */
					Float okapi_tf = (float) (tf_w_d / (tf_w_d + 0.5 + 1.5*(len_d/avg_len_d)));
					// Normalize score for multiple instances
					okapi_tf  = super.additionalTransformation(term, okapi_tf);
					tf_d_q += okapi_tf;
					
					docScore.put(docNo, tf_d_q);
				}
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
}
