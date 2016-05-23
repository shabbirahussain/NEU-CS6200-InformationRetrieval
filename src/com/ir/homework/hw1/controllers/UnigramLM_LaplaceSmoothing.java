/**
 * 
 */
package com.ir.homework.hw1.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.elasticutil.ElasticClient;
import com.ir.homework.hw1.io.OutputWriter.OutputRecord;


/**
 * @author shabbirhussain
 *
 */
public class UnigramLM_LaplaceSmoothing extends BaseSearchController{
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 */
	public UnigramLM_LaplaceSmoothing(ElasticClient elasticClient){
		super(elasticClient);
	}
	
	
	@Override
	public List<OutputRecord> executeQuery(Entry<String, String[]> query) {
		try {
			String queryNo   = query.getKey();
			String []queryTerms = query.getValue();
			
			Map<String, Float> docScore = new HashMap<String, Float>();
			for(String term: queryTerms){
				Map<String, Float> tf = elasticClient.getDocFrequency(term);
				
				for(Entry<String, Float> tfe: tf.entrySet()){
					String docNo = tfe.getKey();
					
					Float tf_w_d    = tfe.getValue();
					Long  len_d     = super.elasticClient.getTermCount(docNo);
					Long  V			= super.elasticClient.getVocabSize();

					Float lm_laplace_d_q = docScore.getOrDefault(docNo, 0.0F);
					/** Unigram LM with Laplace smoothing
					 * This is a language model with Laplace (“add-one”) smoothing. We will use maximum likelihood estimates of the query based on a multinomial model “trained” on the document. The matching score is as follows.
					 *  $$ lm\_laplace(d, q) = \sum_{w \in q} \log p\_laplace(w|d) \\ p\_laplace(w|d) = \frac{tf_{w,d} + 1}{len(d) + V} $$
					 *  Where:
					 *  	$V$ is the vocabulary size – the total number of unique terms in the collection.
					 */
					
					Float p_laplace = (tf_w_d + 1)/ (len_d + V);
					// Normalize score for multiple instances
					p_laplace  = super.additionalTransformation(term, p_laplace);
					lm_laplace_d_q += ((Double)Math.log(p_laplace)).floatValue();
					
					docScore.put(docNo, lm_laplace_d_q);
				}
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
}
