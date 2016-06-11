/**
 * 
 */
package com.ir.homework.hw1.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
public class ProximitySearchController extends BaseSearchController{
	private static final Float λ = 0.8F;
	private static final Float C = 1500F;
	
	
	/**
	 * constructor for re using cache across controllers
	 * @param elasticClient search cache object
	 */
	public ProximitySearchController(ElasticClient elasticClient){
		super(elasticClient);
	}
	
	
	@Override
	public List<OutputRecord> executeQuery(Entry<String, String[]> query) {
		try {
			String queryNo      = query.getKey();
			String []queryTerms = query.getValue();
			
			Map<String, Float> docScore = new HashMap<String, Float>();
			Map<String, Long[][]> docTermMatrix = new HashMap<String, Long[][]>();
			
			for(int i=0; i<queryTerms.length; i++){
				String term = queryTerms[i];
				
				Map<String, List<Long>> termPositionMat = elasticClient.getPositionVector(term);
				for(Entry<String, List<Long>> e : termPositionMat.entrySet()){
					Long[][] termMatrix = docTermMatrix.getOrDefault(e.getKey(), new Long[queryTerms.length][]);
					termMatrix[i]       =  e.getValue().toArray(new Long[0]);
					//Arrays.sort(termMatrix[i]);
					
					docTermMatrix.put(e.getKey(), termMatrix);
				}
			}
			
			Long  V  = super.elasticClient.getVocabSize();
			for(Entry<String, Long[][]> e: docTermMatrix.entrySet()){
				String docNo   = e.getKey();
				Float score_ps = docScore.getOrDefault(docNo, 0.0F);
				Float s = getMinSpanDistance(e.getValue()).floatValue();
				Float k = getNGramLength(e.getValue()).floatValue();
				Long  len_d     = super.elasticClient.getTermCount(docNo);
				
				//score_ps = λ * (s-k)/k;
				score_ps = (C - s) * k / (len_d + V);
				
				docScore.put(docNo, score_ps);
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
	
	/**
	 * Calculates and returns proximity score of the matrix provided. Requires all inner arrays to be sorted in ascending order.
	 * @param pasMat is the position matrix for a document
	 * @return The minimum distance between all terms
	 */
	private Long getMinSpanDistance(Long[][] pasMat){
		Integer ptr[] = new Integer[pasMat.length];
		Long minSpanTarget = -1L;
		for(int i=0; i<pasMat.length; ptr[i++] = 0){
			if(pasMat[i] != null)
				minSpanTarget++;
		}

		/*System.out.println("new doc\n");
		System.out.println("-----------------------------");
		for(int i=0;i<pasMat.length; i++){
			System.out.print("term["+i+"]");
			for(int j=0;pasMat[i]!= null && j<pasMat[i].length; j++){
				System.out.print("\t" + pasMat[i][j]);
			}
			System.out.println("");
		}
		System.out.println("-----------------------------\n");*/

		Long minSpan = Long.MAX_VALUE;
		while(true){
			
			Long minVal  = Long.MAX_VALUE;
			Long maxVal  = Long.MIN_VALUE;
			Integer minI = null; 
			
			for(int i=0; i<pasMat.length; i++){
				Integer j = ptr[i];
				if (pasMat[i] == null) continue;
				
				Long value = pasMat[i][j];
				minVal = Math.min(minVal, value);
				maxVal = Math.max(maxVal, value);
				
				// current value is less than min J value and there is room for incrementing 
				if(((minI == null) || (value < pasMat[minI][ptr[minI]]))
					&& (ptr[i]+1 < pasMat[i].length)){
					minI = i;
				}
				//System.out.println("["+i+"]["+j+"]\t val="+value+"\tmini="+minI+"\t");
					
			}
			//System.out.println("");
			
			minSpan = Math.min(minSpan, maxVal - minVal);
			
			if(minSpan == minSpanTarget || minI == null) break;
			ptr[minI]++;
		}
		return minSpan;
	}
	
	/**
	 * Gives the max ngram matcheed
	 * @param pasMat is the position matrix for a document
	 * @return The maximum number of ngram matched
	 */
	private Integer getNGramLength(Long[][] pasMat){
		Integer result = 0;
		for(int i=0; i<pasMat.length; i++){
			if(pasMat[i] != null)
				result++;
		}
		return result;
	}
	
	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map is the map to sort
	 * @return List of map entries in sorted form
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Entry> sortByValue(Map<Integer, Long> map) {
	     List<Entry> list = new LinkedList<Entry>(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	public static void main(String args[]){
		ProximitySearchController sc = new ProximitySearchController(null);
		Long[][] pasMat = new Long[][]{{0L, 5L, 10L, 15L, 30L},
												{1L, 3L,  6L,  9L},
												{4L, 8L, 16L, 21L}};

		System.out.println("Min Span = " + sc.getMinSpanDistance(pasMat));
	}
}
