/**
 * 
 */
package com.ir.homework.hw6.io;


import java.io.*;

import com.ir.homework.hw6.models.FeatMatrix;

/**
 * @author shabbirhussain
 */
public final class FeatLoader {
	public static final String SEPARATOR = "\t|\\s";
	private FeatMatrix fMat;
	
	/**
	 * Default constructor
	 * @param elasticClient client to use for loading
	 * @param fMat is the feature matrix to be loaded
	 */
	public FeatLoader(FeatMatrix fMat){
		this.fMat = fMat;
		if(fMat == null)
			this.fMat = new FeatMatrix();
	}
	
	/**
	 * Loads the features in given feature matrix
	 * @param dataFilePath full path of the data file to load
	 * @param dataFilePrefix prefix to use for filtering data files
	 * @throws IOException 
	 */
	public void loadFeatures(String dataFilePath, String dataFilePrefix) throws IOException{	
		File folder = new File(dataFilePath);
		File[] listOfFiles = folder.listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(dataFilePrefix)) {
				System.out.println("[Info]: Loading file [" + file.getName() + "]");
				String featureID = file.getName();
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line = null;
				while((line=br.readLine())!= null){
					String[] fields = line.split(SEPARATOR);
					
					String docID = this.getDocQueryKey(fields[0], fields[2]);
					Double value = Double.parseDouble(fields[4]);
					fMat.insertRow(docID, featureID, value);
				}
				br.close();
			}
		}
		return ;
	}
	
	/**
	 * Loads the labels from the qrel file
	 * @param qrelPath is the qrel file path
	 * @throws IOException
	 */
	public void loadLabels(String qrelPath) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(qrelPath)));
		String line = null;
		while((line = br.readLine()) != null){
			String[] fields = line.split(SEPARATOR);
			
			String docID = this.getDocQueryKey(fields[0], fields[2]);
			Double label = Double.parseDouble(fields[3]);
			fMat.setLabel(docID, label);
		}
		br.close();
	}
	
	/**
	 * Given query id and the document id generates a combined feature key
	 * @param queryID is the id of the query
	 * @param docID is the id of the document
	 * @return A combined key of both document and query
	 */
	private String getDocQueryKey(String queryID, String docID){
		return queryID + "-" + docID; 
	}
}
