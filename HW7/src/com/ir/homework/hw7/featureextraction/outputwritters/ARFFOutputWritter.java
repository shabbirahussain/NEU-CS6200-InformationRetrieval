package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Internal model
 * @author shabbirhussain
 */
class InternalModel implements Serializable{
	private static final long serialVersionUID = 1L;
	Map<String, Integer> featMap;
	Set<Double> labelSet;
	
	/**
	 * Default constructor
	 */
	public InternalModel(){
		this.featMap  = new LinkedHashMap<>();
		this.labelSet = new HashSet<>();
	}
}

public class ARFFOutputWritter extends AbstractOutputWritter{
	private static final String MY_EXTENSION = ".arff";
	private static final Integer LABEL_INDEX = 0;
	private static Double JUNK_LABEL = 0.0;
	
	private File tempFile, modelFile;
	private PrintStream tempOut;
	private Boolean enforceModel;
	private InternalModel iModel;
	
	/**
	 * Default constructor
	 * @param outFile is the output file for the writer
	 * @param modelFile is the model file name for the Serialized model file
	 * @throws IOException
	 */
	public ARFFOutputWritter(String outFile, String modelFile) throws IOException {
		super(outFile, MY_EXTENSION);
		
		tempFile = File.createTempFile(this.getClass().getName(), ".tmp");
		System.out.println(tempFile);
		
		this.tempOut      = new PrintStream(tempFile);
		this.modelFile    = new File(modelFile);
		this.enforceModel = false;
		
		iModel = new InternalModel();
		this.iModel.featMap.put("LABEL", LABEL_INDEX);
	}
	
	/**
	 * Default constructor which uses given model to generate test set. Enforcing a given model prevents new attributes or features to be stored.
	 * @param outFile is the output file for the writer
	 * @param modelFile is the model file name for the Serialized model file
	 * @param enforceModel is flag which specifies to load the model and enforce the model
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public ARFFOutputWritter(String outFile, String modelFile, Boolean enforceModel) throws IOException, ClassNotFoundException {
		this(outFile, modelFile);
		this.enforceModel = enforceModel;
		this.loadModel();
	}
	
	@Override
	public void printResults(Double label, Map<String, Double> featureMap) {
		if(!this.enforceModel)
			this.iModel.labelSet.add(label);
		
		TreeMap<Integer, Double> srtdMap = new TreeMap<>();
		// Map features to keys
		for(Entry<String, Double> e: featureMap.entrySet()){
			String key = e.getKey();
			Integer nKey = this.iModel.featMap.get(e.getKey());
			if(nKey == null && !this.enforceModel){
				nKey = this.iModel.featMap.size();
				this.iModel.featMap.put(key, nKey);
			}
			if(nKey != null) srtdMap.put(nKey, e.getValue());
		}
		
		tempOut.print("{");
		
		// Assign a junk default label if no label is provided
		if(!this.iModel.labelSet.contains(label))
			label = JUNK_LABEL;
			
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
		this.tempOut.close();
		if(!this.enforceModel)
			this.saveModel();

		PrintStream out = new PrintStream(this.outFile);
		this.printHeaders(out);
		this.printData(out);
		out.close();
		
		this.tempFile.delete();
	}
	
	/**
	 * Saves the model
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private void saveModel() throws FileNotFoundException, IOException{
		ObjectOutputStream  out = new ObjectOutputStream(new FileOutputStream(this.modelFile));
		out.writeObject(this.iModel);
		out.close();
	}
	
	/**
	 * Loads the internal model
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	private void loadModel() throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream  in = new ObjectInputStream(new FileInputStream(this.modelFile));
		this.iModel = (InternalModel) in.readObject();
		in.close();
	}
	
	/**
	 * Prints the headers of the ARFF file
	 * @param out is the out stream to print to
	 */
	private void printHeaders(PrintStream out){
		out.println("@RELATION " + this.outFile.getName());
		out.println();
		
		// Print class
		Iterator<String> itr = this.iModel.featMap.keySet().iterator();
		String f = itr.next();
		out.print("@ATTRIBUTE " + f + "  {");
		Integer cnt=1;
		for(Double l: this.iModel.labelSet){
			out.print(l);
			if((cnt++) != this.iModel.labelSet.size())
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
	}
	
	/**
	 * Prints the body to the output stream
	 * @param out is the out stream to print to
	 * @throws IOException 
	 */
	private void printData(PrintStream out) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile)));
		String line = null;
		while((line = in.readLine()) != null){
			out.println(line);
		}
		in.close();
	}
}
