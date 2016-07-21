package com.ir.homework.hw5.evaluators;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw5.models.ModelQrel;
import com.ir.homework.hw5.models.ModelQres;

public class NDCGEvaluator extends AbstractEvaluator {
	public static final Integer[] K_VAL = {1000};
	private static final DecimalFormat f = new DecimalFormat("###0");
	
	
	public void execute(PrintStream out) {
		out.println("nDCG:");
		
		for(Integer k: K_VAL){
			Double totPrcn = 0.0;
			Double cntPrcn = ((Integer)qres.size()).doubleValue();
			
			for(Entry<String, ModelQres> query: qres.entrySet()){
				String queryKey = query.getKey();
				
				ModelQrel qrelMap = super.qrel.get(queryKey);
				ModelQres qres    = super.qres.get(queryKey);

				Double ndcg = 0.0;
				for(int i=0;i<qres.size() && i<k;i++){
					ndcg += qrelMap.getOrDefault(qres.get(i).getKey(),0.0)
							* ((i==0)? 1 : (Math.log(2)/Math.log(i+1)));
				}
				
				//List<Entry<String, Double>> qrel = sortDscByValue(qrelMap);
				Map<String, Double> qresMap = new HashMap<String, Double>();
				for(Entry<String, Double> e: qres){
					qresMap.put(e.getKey(), qrelMap.getOrDefault(e.getKey(),0.0));
				}
				List<Entry<String, Double>> qrel = sortDscByValue(qresMap);
				
				Double indcg = 0.0;
				for(int i=0;i<qrel.size() && i<k;i++){
					indcg += qrel.get(i).getValue()
							* ((i==0)? 1 : (Math.log(2)/Math.log(i+1)));
				}
				totPrcn += 	ndcg/indcg;
			}
			out.println("  At " + f.format(k) + " docs:\t" 
					+ FORMATTER.format(totPrcn/cntPrcn));
		}
	}
	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param <K>
	 * @param <V>
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static <K, V extends Number> List<Entry<K, V>> sortDscByValue(Map<K,V> map) {
	     List<Entry<K, V>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
}
