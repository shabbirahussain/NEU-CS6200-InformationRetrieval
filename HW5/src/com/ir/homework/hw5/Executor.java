package com.ir.homework.hw5;


import static com.ir.homework.hw5.Constants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ir.homework.hw5.evaluators.*;
import com.ir.homework.hw5.io.ModelQrelDAO;
import com.ir.homework.hw5.io.ModelQresDAO;
import com.ir.homework.hw5.models.ModelQrel;
import com.ir.homework.hw5.models.ModelQres;

public class Executor {
	
	public static void main(String[] args) throws ArrayIndexOutOfBoundsException, IOException{
		List<Evaluator> evaluators = new LinkedList<Evaluator>();
		
		//////////////////////////////////////////////////////////////////////////
		// Add evaluators to the system
		//////////////////////////////////////////////////////////////////////////

		evaluators.add(new PrecisionEvaluator());
		evaluators.add(new RPrecisionEvaluator());
		evaluators.add(new RecallEvaluator());
		evaluators.add(new F1Evaluator());
		evaluators.add(new PRGraphEvaluator());
		
		
		
		//////////////////////////////////////////////////////////////////////////
		// Load qrel and results files
		//////////////////////////////////////////////////////////////////////////
		
		Map<String, ModelQrel> qrel = ModelQrelDAO.readModel(QREL_PATH);
		Map<String, ModelQres> qres = ModelQresDAO.readModel(QRES_PATH);
		
		if(ENABLE_INDIVIDUAL_OUTPUT){
			for(Entry<String, ModelQres> entry: qres.entrySet()){
				//if(!entry.getKey().equals("54")) continue;
				Map<String, ModelQres> newQres = new HashMap<String, ModelQres>();
				newQres.put(entry.getKey(), entry.getValue());
				
				System.out.println("\nQueryid (Num):\t" + entry.getKey());
				executeEvaluations(evaluators, qrel, newQres);
			}
		}
		
		// Print summary results
		System.out.println("\nSummary:");
		executeEvaluations(evaluators, qrel, qres);
	}
	
	/**
	 * Calculates evaluation results
	 * @param evaluators is the list of evaluators to use
	 * @param qrel is the relevance model
	 * @param qres is the output model to evaluate
	 */
	private static void executeEvaluations(List<Evaluator> evaluators, Map<String, ModelQrel> qrel, Map<String, ModelQres> qres){
		for(Evaluator e: evaluators){
			e.initialize(qrel, qres);
			e.execute(System.out);
		}
	}
}
