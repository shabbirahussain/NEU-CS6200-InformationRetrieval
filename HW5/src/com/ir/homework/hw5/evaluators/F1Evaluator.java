package com.ir.homework.hw5.evaluators;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;

import com.ir.homework.hw5.models.ModelQres;

public class F1Evaluator extends AbstractEvaluator {
	public static final Integer[] K_VAL = {5,10,15,20,30,100,200,500,1000};
	private static final DecimalFormat f = new DecimalFormat("###0");
	
	
	public void execute(PrintStream out) {
		out.println("F1:");
		
		for(Integer k: K_VAL){
			Double totPrcn = 0.0;
			Double cntPrcn = 0.0;
			for(Entry<String, ModelQres> query: qres.entrySet()){
				List<Result> qResult = super.resultMap.get(query.getKey());
				if(qResult != null && k<=qResult.size()){
					Double precision = qResult.get(k-1).prcisn;
					Double recall    = qResult.get(k-1).recall;
					totPrcn += 2 * precision * recall /(precision + recall);
					cntPrcn += 1;
				}		
			}
			out.println("  At " + f.format(k) + " docs:\t" 
					+ FORMATTER.format(totPrcn/cntPrcn));
		}
	}
	
}
