/**
 * 
 */
package com.ir.homework.hw1.io;

import static com.ir.homework.hw1.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
/**
 * @author shabbirhussain
 * Evaluates the results generated using treckeval
 */
public final class ResultEvaluator {
	private List<String> commands;

	/**
	 * Default constructor
	 * @param evaluatorPath
	 * @param params
	 */
	public ResultEvaluator(String evaluatorPath, String params[]){
		this.commands = new LinkedList<String>();
		this.commands.add(evaluatorPath);
		this.commands.addAll(Arrays.asList(params));
	}
	
	/**
	 * Runs evaluation script on output file
	 * @param outFilePath is the path of output file to evaluate
	 * @param silent when enabled suppresses the screen output
	 * @return evaluation final score
	 * @throws IOException 
	 */
	public Double runEvaluation(String outFilePath, Boolean silent) throws IOException{
		commands.add(outFilePath);
		
		Runtime rt = Runtime.getRuntime();
		//commands = {"ls", "-l"};
		Process proc = rt.exec(commands.toArray(new String[commands.size()]));

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		String line = null, lastLine = null;
		while ((line = stdInput.readLine()) != null) {
			if(!silent) System.out.println(line);
			lastLine = line;
		}

		// read any errors from the attempted command
		while ((line = stdError.readLine()) != null) {
		    System.err.println(line);
		}

		Double result = Double.parseDouble(lastLine.replaceAll("Exact:", ""));
		return result;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String outFile = (OUTPUT_FOLDR_PATH + "/output1000_TF_IDFController.txt");
		
		ResultEvaluator re = new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS);
		re.runEvaluation(outFile, false);
	}

}
