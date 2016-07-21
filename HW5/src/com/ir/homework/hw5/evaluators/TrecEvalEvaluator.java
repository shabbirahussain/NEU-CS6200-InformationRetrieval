/**
 * 
 */
package com.ir.homework.hw5.evaluators;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.ir.homework.hw5.Constants.*;

/**
 * @author shabbirhussain
 * Evaluates the results generated using treckeval
 */
public final class TrecEvalEvaluator extends AbstractEvaluator{
	private List<String> commands;

	/**
	 * Default constructor
	 * @param evaluatorPath is the base path of the trec_eval
	 * @param params is the list of parameters to be given to trec_eval
	 */
	public TrecEvalEvaluator(String evaluatorPath, String params[]){
		this.commands = new LinkedList<String>();
		this.commands.add(evaluatorPath);
		if(EVALUATE_INDIVIDUAL_Q)
			this.commands.add("-q");
		
		this.commands.addAll(Arrays.asList(params));
	}
	
	
	@Override
	public void execute(PrintStream out) {
		List<String> commandsCopy = new LinkedList<String>(commands);
		commandsCopy.add(srcFile);
		
		out.println("\n==============================================");
		out.println("Trec Eval Results:");
		out.println("==============================================");
		try{
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(commandsCopy.toArray(new String[0]));
	
			BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(proc.getInputStream()));
	
//			BufferedReader stdError = new BufferedReader(new 
//			     InputStreamReader(proc.getErrorStream()));
			
			// read the output from the command
			String line = null;
			while ((line = stdInput.readLine()) != null) {
				out.println(line);
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
	}

}
