package com.ir.homework.hw5.evaluators;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw5.models.ModelQrel;
import com.ir.homework.hw5.models.ModelQres;

public abstract class AbstractEvaluator implements Evaluator {
	protected static final DecimalFormat FORMATTER = new DecimalFormat("#0.0000");
	
	protected Map<String, ModelQrel> qrel;
	protected Map<String, ModelQres> qres;
	
	protected Map<String, List<Result>> resultMap;

	class Result{
		public Double prcisn, recall;
		public Boolean relevant;
		public Double numRel;
	} 
	
	

	public void initialize(Map<String, ModelQrel> qrel, Map<String, ModelQres> qres){
		this.qrel = qrel;
		this.qres = qres;
		
		resultMap = new HashMap<String, List<Result>>();
		
		List<Result> tmpResultLst;
		Result 	     tmpResult;
		// Calculate precision and recall for each query
		for(Entry<String, ModelQres> query: qres.entrySet()){
			String qeryKey  = query.getKey();
			ModelQrel qrel1 = this.qrel.get(qeryKey);
			 
			Double  cntRel = 0.0;
			Double  cntRet = 0.0;
			Integer totRel = qrel1.size();
			for(Entry<String, Double> d: query.getValue()){
				cntRet++;
				
				Integer relScore = (qrel1.getOrDefault(d.getKey(), 0.0)>0)? 1: 0;
				Integer docScore = (d.getValue() > 0)? 1: 0;
				
				tmpResultLst = resultMap.getOrDefault(qeryKey, new LinkedList<Result>());
				tmpResult = new Result();
				
				if((tmpResult.relevant = (relScore == docScore)))
					cntRel ++;
					
				tmpResult.prcisn = cntRel/cntRet;
				tmpResult.recall = cntRel/totRel;
				tmpResult.numRel = ((Integer) qrel1.size()).doubleValue();
				
				tmpResultLst.add(tmpResult);
				resultMap.put(qeryKey, tmpResultLst);
			}
			//System.out.println(prcisn);
		}
	}
	
//	public void printResults(PrintStream out){
//		out.println(displayLabel + ": " + f.format(score));
//	}
}
