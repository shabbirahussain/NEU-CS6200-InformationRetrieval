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
import com.ir.homework.hw5.io.RelevanceModelDAO;
import com.ir.homework.hw5.models.ModelRelevance;

public class Executor {
	
	public static void main(String[] args) throws ArrayIndexOutOfBoundsException, IOException{
		List<Evaluator> evaluators = new LinkedList<Evaluator>();

		//////////////////////////////////////////////////////////////////////////
		//
		//////////////////////////////////////////////////////////////////////////
		
		
		
		
		
		//////////////////////////////////////////////////////////////////////////
		// Load qrel and results files
		//////////////////////////////////////////////////////////////////////////
		
		Map<String, ModelRelevance> qrel = RelevanceModelDAO.buildModel(QREL_PATH);
		Map<String, ModelRelevance> qres = RelevanceModelDAO.buildModel(QRES_PATH);
		
		if(ENABLE_INDIVIDUAL_OUTPUT){
			for(Entry<String, ModelRelevance> entry: qres.entrySet()){
				Map<String, ModelRelevance> newQres = new HashMap<String, ModelRelevance>();
				newQres.put(entry.getKey(), entry.getValue());
				
				System.out.println("\n" + entry.getKey() + ":");
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
	private static void executeEvaluations(List<Evaluator> evaluators, Map<String, ModelRelevance> qrel, Map<String, ModelRelevance> qres){
		for(Evaluator e: evaluators){
			e.calculateScore(qrel, qres);
			e.printResults(System.out);
		}
	}
}
