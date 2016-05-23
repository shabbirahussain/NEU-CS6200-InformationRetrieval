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
public class UnigramLM_JelinekMercerSmoothing extends BaseSearchController implements SearchController{
	private static final Float λ = 0.4F;
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 * @param maxResults maximum number of results
	 */
	public UnigramLM_JelinekMercerSmoothing(ElasticClient elasticClient, Integer maxResults, Boolean addTransEnable){
		super(elasticClient, maxResults, addTransEnable);
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
					Long  V			= super.elasticClient.getVocabSize();

					Float lm_jm_d_q = docScore.getOrDefault(docNo, 0.0F);
					/** Unigram LM with Jelinek-Mercer smoothing
					 * This is a similar language model, except that here we smooth a foreground document language model with a background model from the entire corpus.
					 *  $$ lm\_jm(d, q) = \sum_{w \in q} \log p\_jm(w|d) \\ p\_jm(w|d) = \lambda \frac{tf_{w,d}}{len(d)} + (1 - \lambda) \frac{\sum_{d'} tf_{w,d'}}{\sum_{d'} len(d')} $$
					 *  Where:
					 *  	$\lambda \in (0, 1)$ is a smoothing parameter which specifies the mixture of the foreground and background distributions.
					 *  
					 *  Think carefully about how to efficiently obtain the background model here. If you wish, you can instead estimate the corpus probability using $\frac{cf_w}{V}$.
					 */
					
					Float p_laplace = λ * (tf_w_d + 1)/(len_d + V) + (1-λ);
					lm_jm_d_q += ((Double)Math.log(p_laplace)).floatValue();
					
					docScore.put(docNo, lm_jm_d_q);
				}
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
}
