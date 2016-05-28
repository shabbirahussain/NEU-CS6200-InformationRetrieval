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
		List<String> commandsCopy = new LinkedList<String>(commands);
		commandsCopy.add(outFilePath);
		
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(commandsCopy.toArray(new String[0]));

		BufferedReader stdInput = new BufferedReader(new 
		     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));
		
		Double result = null, avgPrecision = null, rPrecision = null;
		// read the output from the command
		String line = null, lastLine = "";
		while ((line = stdInput.readLine()) != null) {
			if(silent) System.out.println(line);
			
			if(lastLine.contains("R-Precision"))
				rPrecision = Double.parseDouble(line.replace("Exact:", "").trim());
			else if(lastLine.contains("Average precision"))
				avgPrecision = Double.parseDouble(line);
			
			lastLine = line;
		}
		System.out.println("====> Average precision: " + avgPrecision + "\t R-Precision: " + rPrecision);

		result = avgPrecision;
		
		// read any errors from the attempted command
		while ((line = stdError.readLine()) != null) {
		    System.err.println(line);
		}

		return result;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String outFile = (OUTPUT_FOLDR_PATH + "/output1000_TF_IDFController.txt");
		
		ResultEvaluator re = new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS);
		re.runEvaluation(outFile, true);
	}

}
