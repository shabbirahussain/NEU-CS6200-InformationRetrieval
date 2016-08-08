package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


public class ARFFOutputWritter extends AbstractOutputWritter {
	private static final String MY_EXTENSION = ".arff";
	private static final Integer LABEL_INDEX = 0;
	
	private File tempFile;
	private PrintStream tempOut;
	private Map<String, Integer> featMap;
	private Set<Double> labelSet;
	
	/**
	 * Default constructor
	 * @param outFile is the output file for the writer
	 * @throws IOException
	 */
	public ARFFOutputWritter(String outFile) throws IOException {
		super(outFile, MY_EXTENSION);
		
		tempFile = File.createTempFile(this.getClass().getName(), ".tmp");
		System.out.println(tempFile);
		tempOut = new PrintStream(tempFile);
		
		featMap  = new LinkedHashMap<>();
		labelSet = new HashSet<>();
		
		featMap.put("LABEL", LABEL_INDEX);
	}
	
	@Override
	public void printResults(Double label, Map<String, Double> featureMap) {
		labelSet.add(label);
		
		TreeMap<Integer, Double> srtdMap = new TreeMap<>();
		// Map features to keys
		for(Entry<String, Double> e: featureMap.entrySet()){
			String key = e.getKey();
			Integer nKey = featMap.get(e.getKey());
			if(nKey == null){
				nKey = featMap.size();
				featMap.put(key, nKey);
			}
			srtdMap.put(nKey, e.getValue());
			
		}
		
		tempOut.print("{");
		tempOut.print(LABEL_INDEX + " " + label + ", ");
		Integer cnt = 1;
		for(Entry<Integer, Double> e: srtdMap.entrySet()){
			tempOut.print(e.getKey() + " " + e.getValue());
			if((cnt++) != srtdMap.size())
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
		
		// Print class
		Iterator<String> itr=featMap.keySet().iterator();
		String f = itr.next();
		out.print("@ATTRIBUTE " + f + "  {");
		Integer cnt=1;
		for(Double l: labelSet){
			out.print(l.toString());
			if((cnt++) != labelSet.size())
				out.print(", ");
		}
		out.println("}");
		
		for(; itr.hasNext();){
			f=itr.next();
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
