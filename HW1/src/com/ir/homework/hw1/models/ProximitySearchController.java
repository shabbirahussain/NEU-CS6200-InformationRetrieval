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
					Arrays.sort(termMatrix[i]);
					
					docTermMatrix.put(e.getKey(), termMatrix);
				}
			}
			
			for(Entry<String, Long[][]> e: docTermMatrix.entrySet()){
				String docNo   = e.getKey();
				Float score_ps = docScore.getOrDefault(docNo, 0.0F);
				score_ps = 1F/(1F + getMinSpanDistance(e.getValue()));
					
				docScore.put(docNo, score_ps);
			}
			return super.prepareOutput(queryNo, docScore);
		} catch (Exception e1) {e1.printStackTrace();}
		return null;
	}
	
	/**
	 * Calculates and returns proximity score of the matrix provided
	 * @return The minimum distance between all terms
	 */
	private Long getMinSpanDistance(Long[][] positionMatrix){
		Integer ptr[] = new Integer[positionMatrix.length];
		Long minSpanTarget = -1L;
		for(int i=0; i<positionMatrix.length; ptr[i++] = 0){
			if(positionMatrix[i] != null)
				minSpanTarget++;
		}

//		System.out.println("new doc\n");
//		System.out.println("-----------------------------");
//		for(int i=0;i<positionMatrix.length; i++){
//			System.out.print("term["+i+"]");
//			for(int j=0;positionMatrix[i]!= null && j<positionMatrix[i].length; j++){
//				System.out.print("\t" + positionMatrix[i][j]);
//			}
//			System.out.println("");
//		}
//		System.out.println("-----------------------------\n");

		Boolean flgContinue = true;
		Long minSpan = Long.MAX_VALUE;
		while(flgContinue){
			flgContinue = false;
			
			Map<Integer, Long> idxValMap = new HashMap<Integer, Long>();
			for(int i=0; i<positionMatrix.length; i++){
				Integer j = ptr[i];
				if (positionMatrix[i] == null) continue;
				
				//System.out.println("["+i+"]["+j+"]");
				Long value = positionMatrix[i][j];
				idxValMap.put(i, value);
			}
			//System.out.println("");
			
			List<Entry> sortedIdxValMap = sortByValue(idxValMap);
			for(Entry e : sortedIdxValMap){
				Integer i = (Integer) e.getKey();
				if(ptr[i] < positionMatrix[i].length-1){
					ptr[i]++;
					flgContinue = true;
					break;
				}
			}
			
			Long minVal = (Long) sortedIdxValMap.get(0).getValue();
			Long maxVal = (Long) sortedIdxValMap.get(sortedIdxValMap.size()-1).getValue();
			minSpan = Math.min(minSpan, maxVal - minVal);
			
			if(minSpan == minSpanTarget) break;
		}
		return minSpan;
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
		Long[][] positionMatrix = new Long[][]{{0L, 5L, 10L, 15L, 30L},
												{1L, 3L,  6L,  9L},
												{4L, 8L, 16L, 21L}};

		System.out.println("Min Span = " + sc.getMinSpanDistance(positionMatrix));
	}
}
