package com.ir.homework.hw5.evaluators;

import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;

import com.ir.homework.hw5.models.ModelQres;

public class RPrecisionEvaluator extends AbstractEvaluator {

	
	public void execute(PrintStream out) {
		out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):");
		
		Double totPrcn = 0.0;
		Double totCntPrcn = ((Integer)qres.size()).doubleValue();
		for(Entry<String, ModelQres> query: qres.entrySet()){
			List<Result> qResult = super.resultMap.get(query.getKey());
			
			Integer R  = qResult.get(0).numRel.intValue();
			if(R<=qResult.size()){
				totPrcn += qResult.get(R-1).prcisn;
			}
		}
		out.println("\t Exact: " + FORMATTER.format(totPrcn/totCntPrcn));
	}
}
