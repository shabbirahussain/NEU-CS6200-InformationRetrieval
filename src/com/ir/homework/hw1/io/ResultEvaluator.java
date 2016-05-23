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
	private String commands[];

	public ResultEvaluator(String evaluatorPath, String params[], String outFilePath){
		List<String> commands = new LinkedList<String>();
		commands.add(evaluatorPath);
		commands.addAll(Arrays.asList(params));
		commands.add(outFilePath);
		
		this.commands = commands.toArray(new String[commands.size()]);
	}
	
	/**
	 * Runs evaluation script on output file
	 * @param silent when enabled suppresses the screen output
	 * @return evaluation final score
	 * @throws IOException 
	 */
	public Double runEvaluation(Boolean silent) throws IOException{
		Runtime rt = Runtime.getRuntime();
		//commands = {"ls", "-l"};
		Process proc = rt.exec(commands);

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
		String outFile = (OUTPUT_FOLDR_PATH + "/output1000.txt");
		
		ResultEvaluator re = new ResultEvaluator(TRECK_EVAL_PATH, TRECK_EVAL_PARAMS, outFile);
		re.runEvaluation(false);
	}

}
