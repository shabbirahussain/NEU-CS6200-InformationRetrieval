package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


public class ARFFOutputWritter extends AbstractOutputWritter {
	private File tempFile, outFile;
	private PrintStream tempOut;
	private Map<String, Integer> featMap;
	
	private static final Integer LABEL_INDEX = 0;
	
	/**
	 * Default constructor
	 * @param outFile is the output file for the writer
	 * @throws IOException
	 */
	public ARFFOutputWritter(File outFile) throws IOException {
		this.outFile = outFile; 
		Files.createDirectories(Paths.get(outFile.getParent()));
		
		tempFile = File.createTempFile("ARFFOutputWritter", ".tmp");
		System.out.println(tempFile);
		tempOut = new PrintStream(tempFile);
		
		featMap = new LinkedHashMap<String, Integer>();
		
		featMap.put("LABEL", LABEL_INDEX);
	}
	
	/**
	 * Default constructor
	 * @param outFile is the output file for the writer
	 * @throws IOException
	 */
	public ARFFOutputWritter(String outFile) throws IOException {
		this(new File(outFile));
	}
	
	@Override
	public void printResults(Double label, Map<String, Double> featureMap) {
		tempOut.print("{");
		
		// Print label
		tempOut.print(LABEL_INDEX + " " + label + ", ");
		
		// Print other features
		Integer cnt = 1;
		for(Entry<String, Double> e: featureMap.entrySet()){
			String key = e.getKey();
			Integer nKey = featMap.get(e.getKey());
			if(nKey == null){
				nKey = featMap.size();
				featMap.put(key, nKey);
			}
			tempOut.print(nKey + " " + e.getValue());
			if((cnt++) != featureMap.size())
				tempOut.print(", ");	
		}
		tempOut.println("}");
	}
	
	public void close() throws IOException{
		tempOut.close();
		
		///////////////// Print Headers ///////////////////////
		PrintStream out = new PrintStream(outFile);
		out.println("@RELATION " + outFile.getName());
		out.println();
		
		for(String f: featMap.keySet()){
			f = f.replaceAll("\\s", "_");
			out.println("@ATTRIBUTE " + f + "  NUMERIC");
		}
		out.println();
		out.println("@DATA");
		out.println();

		///////////////// Print body /////////////////////////
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)));
		String line = null;
		while((line = in.readLine()) != null){
			out.println(line);
		}

		in.close();
		out.close();
		tempFile.delete();
	}
	
}
