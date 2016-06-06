/**
 * 
 */
package com.ir.homework.hw1.models;

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

			Long  V			= super.elasticClient.getVocabSize();
			
			Map<String, Float>   docScore = new HashMap<String, Float>();
			Map<String, Integer> docCount = new HashMap<String, Integer>();
			for(String term: queryTerms){
				Map<String, Float> tf = elasticClient.getDocFrequency(term);
				
				for(Entry<String, Float> tfe: tf.entrySet()){
					String docNo = tfe.getKey();
					
					Float tf_w_d    = tfe.getValue();
					Long  len_d     = super.elasticClient.getTermCount(docNo);

					Float lm_laplace_d_q = docScore.getOrDefault(docNo, 0.0F);
					/** Unigram LM with Laplace smoothing
					 * This is a language model with Laplace (“add-one”) smoothing. We will use maximum likelihood estimates of the query based on a multinomial model “trained” on the document. The matching score is as follows.
					 *  $$ lm\_laplace(d, q) = \sum_{w \in q} \log p\_laplace(w|d) \\ p\_laplace(w|d) = \frac{tf_{w,d} + 1}{len(d) + V} $$
					 *  Where:
					 *  	$V$ is the vocabulary size – the total number of unique terms in the collection.
					 */
					
					Float p_laplace = (tf_w_d + 1)/ (len_d + V);
					lm_laplace_d_q += ((Double)Math.log(p_laplace)).floatValue();
					
					docScore.put(docNo, lm_laplace_d_q);
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
