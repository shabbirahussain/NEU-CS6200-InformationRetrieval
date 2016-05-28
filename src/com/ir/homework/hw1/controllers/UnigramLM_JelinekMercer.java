/**
 * 
 */
package com.ir.homework.hw1.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw1.elasticclient.ElasticClient;
import com.ir.homework.hw1.io.OutputWriter.OutputRecord;


/**
 * @author shabbirhussain
 *
 */
public class UnigramLM_JelinekMercer extends BaseSearchController{
	// Probability smoothing factor between 0 - 1
	private static final Float λ = 0.7F;
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 */
	public UnigramLM_JelinekMercer(ElasticClient elasticClient){
		super(elasticClient);
	}
	
	
	@Override
	public List<OutputRecord> executeQuery(Entry<String, String[]> query) {
		try {
			String queryNo      = query.getKey();
			String []queryTerms = query.getValue();

			Long  V  = super.elasticClient.getVocabSize();
			
			Map<String, Float>   docScore = new HashMap<String, Float>();
			Map<String, Integer> docCount = new HashMap<String, Integer>();
			
			Float bgDocLenSum = super.elasticClient.getAvgDocLen() * super.elasticClient.getDocCount();
			
			for(String term: queryTerms){
				
				/** Unigram LM with Jelinek-Mercer smoothing
				 *  This is a similar language model, except that here we smooth a foreground document language model with a background model from the entire corpus
				 * 	  $$ lm\_jm(d, q) = \sum_{w \in q} \log p\_jm(w|d) \\ p\_jm(w|d) = \lambda \frac{tf_{w,d}}{len(d)} + (1 - \lambda) \frac{\sum_{d'} tf_{w,d'}}{\sum_{d'} len(d')} $$
				 *  Where:
				 *    $\lambda \in (0, 1)$ is a smoothing parameter which specifies the mixture of the foreground and background distributions.
				 *
				 *  Think carefully about how to efficiently obtain the background model here. If you wish, you can instead estimate the corpus probability using $\frac{cf_w}{V}$.
				 */
				
				Long  bgDocCount      = super.elasticClient.getTotalTermCount(term);
				Map<String, Float> tf = super.elasticClient.getDocFrequency(term);
				
				for(Entry<String, Float> tfe: tf.entrySet()){
					String docNo = tfe.getKey();
					
					Float tf_w_d    = tfe.getValue();
					Long  len_d     = super.elasticClient.getTermCount(docNo);
					Float lm_jm_d_q = docScore.getOrDefault(docNo, 0.0F);
					
					float bgProbability = (bgDocCount - tf_w_d) / (bgDocLenSum - len_d);
					Float fgProbability = (tf_w_d / len_d);
					
					Float p_jm_w_d = (λ * fgProbability) + ((1 - λ) * bgProbability);
					lm_jm_d_q += ((Double)Math.log(p_jm_w_d)).floatValue();
					
					docScore.put(docNo, lm_jm_d_q);
					docCount.put(docNo, docCount.getOrDefault(docNo, 0) + 1);
				}
			}
			
			// penalize for missing term
			Integer lenQuery = queryTerms.length;
					
			for(Entry<String, Float> doc: docScore.entrySet()){
				String docNo = doc.getKey();
				Long  len_d  = super.elasticClient.getTermCount(docNo);

				Float lm_laplace_d_q = doc.getValue();
				Double d_lm_laplace_d_q = (lenQuery - docCount.get(docNo)) 
						* Math.log( 1.0 / (len_d + V));
				
				lm_laplace_d_q += d_lm_laplace_d_q.floatValue();
				docScore.put(docNo, lm_laplace_d_q);
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
}
