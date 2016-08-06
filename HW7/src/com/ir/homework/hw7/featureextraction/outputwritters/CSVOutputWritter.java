package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class CSVOutputWritter extends AbstractOutputWritter {
	private static final String MY_EXTENSION = ".csv";
	private static final String MISSING_VALUES = "0";
	private static final Integer LABEL_INDEX = 0;
	
	private File tempFile;
	private ObjectOutputStream tempOut;
	private Map<String, Integer> featMap;
	
	
	/**
	 * Default constructor
	 * @param outFile is the output file for the writer
	 * @throws IOException
	 */
	public CSVOutputWritter(String outFile) throws IOException {
		super(outFile, MY_EXTENSION);
		
		tempFile = File.createTempFile(this.getClass().getName(), ".tmp");
		System.out.println(tempFile);
		tempOut = new ObjectOutputStream(new FileOutputStream(tempFile));
		
		featMap = new LinkedHashMap<String, Integer>();
		featMap.put("LABEL", LABEL_INDEX);
	}
	
	@Override
	public void printResults(Double label, Map<String, Double> featureMap) throws IOException {
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
		srtdMap.put(LABEL_INDEX, label);
		
		tempOut.writeObject(srtdMap);
	}
	
	public void close() throws IOException, ClassNotFoundException{
		tempOut.close();
		
		///////////////// Print Headers ///////////////////////
		PrintStream out = new PrintStream(outFile);
		Integer cnt = 1;
		for(String f: featMap.keySet()){
			out.print(f);
			if((cnt++) != featMap.size())
				out.print(", ");	
		}
		out.println();

		///////////////// Print body /////////////////////////
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(tempFile));
		Object obj = null;
		try{
			while((obj = in.readObject()) != null){
				@SuppressWarnings("unchecked")
				TreeMap<Integer, Double> srtdMap = (TreeMap<Integer, Double>) obj;
				
				cnt = 1;
				for(Integer key : featMap.values()){
					String value = MISSING_VALUES;
					Double dVal = srtdMap.get(key);
					if(dVal != null) 
						value = dVal.toString();
					
					out.print(value);
					if((cnt++) != featMap.size())
						out.print(", ");	
				}
				out.println();
			}
		}catch(Exception e){}
		
		in.close();
		out.close();
		tempFile.delete();
	}
	
}
