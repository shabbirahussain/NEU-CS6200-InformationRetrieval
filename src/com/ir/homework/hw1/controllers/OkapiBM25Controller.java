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
public class OkapiBM25Controller extends BaseSearchController implements SearchController{
	private static final Double K1 = 0.9;
	private static final Double K2 = 0.0; // K2 is ignored
	private static final Double B  = 1.0;
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 */
	public OkapiBM25Controller(ElasticClient elasticClient){
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
					
					Long D          = super.elasticClient.getDocumentCount();
					Long df_w       = super.elasticClient.getTermDocCount(term);

					Float bm25_d_q = docScore.getOrDefault(docNo, 0.0F);
					/**Okapi BM25
					 * BM25 is a language model based on a binary independence model. Its matching score is as follows.
					 *  $$ bm25(d, q) = \sum_{w \in q} \left[ \log\left( \frac{D + 0.5}{df_w + 0.5} \right) \cdot \frac{tf_{w,d} + k_1 \cdot tf_{w,d}}{tf_{w,d} + k_1 \left((1-b) + b \cdot \frac{len(d)}{avg(len(d))}\right)} \cdot \frac{tf_{w,q} + k_2 \cdot tf_{w,q}}{tf_{w,q} + k_2} \right] $$
					 *  Where:
					 *  	$tf_{w,q}$ is the term frequency of term $w$ in query $q$
					 *  	$k_1$, $k_2$, and $b$ are constants. You can use the values from the slides, or try your own.
					 */
					Double d_bm25_d_q = Math.log((D + 0.5)/(df_w + 0.5)) * ((tf_w_d + K1 * tf_w_d)/(tf_w_d + K1 * ((1 - B) + (B * len_d/avg_len_d))));
					// Normalize score for multiple instances
					d_bm25_d_q  = super.additionalTransformation(term, d_bm25_d_q.floatValue()).doubleValue();
					bm25_d_q += d_bm25_d_q.floatValue();
					
					docScore.put(docNo, bm25_d_q);
				}
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
}
