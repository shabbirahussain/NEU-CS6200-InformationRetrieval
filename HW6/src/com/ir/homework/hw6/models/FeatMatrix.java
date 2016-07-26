package com.ir.homework.hw6.models;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import static com.ir.homework.hw6.Constants.*;

public class FeatMatrix implements Serializable{
	// ------------------ Constants -------------------- //
	private static final long   serialVersionUID = 3L;
	private static final Double RANDOM_PERCENTAGE = 0.8; // How much results should flow in training file
	
	private static final Random rnd = new Random();
	// -------------------- Private -------------------- //
	private Set<String> queries;
	private Map<String, Row>  rows; // Stores features in a hash map
	private Map<String, Feat> feats;

	/**
	 * Stores the row information for each row
	 * @author shabbirhussain
	 */
	private class Row implements Serializable{
		private static final long serialVersionUID = 1L;
		public Map<String, Double> featMap;
		public Object label;
		public String qryID;
		public String docID;
		
		public Row(){
			this.featMap = new HashMap<String, Double>();
			this.label   = null;
			this.qryID   = null;
			this.docID   = null;
		}
	}
	
	private class Feat implements Serializable{
		private static final long serialVersionUID = 1L;
		public Double minFeatVal, maxFeatVal;
		
		/**
		 * Default constructor 
		 * @param fMin is the minimum value of the feature
		 * @param fMax is the maximum value of the feature
		 */
		public Feat(Double fMin, Double fMax){
			this.minFeatVal = fMin;
			this.maxFeatVal = fMax;
		}
	}
	
	// ----------------- Constructors ------------------ //
	/**
	 * Default constructor
	 */
	public FeatMatrix() {
		rows  = new HashMap<String, Row>();
		feats = new LinkedHashMap<String, Feat>();
		
		queries = new HashSet<String>();
	}
	

	// ------------ Interface Methods ------------------ //
	/**
	 * Adds a row to the feature model. A label must be added before adding any features. If no label is provided for given id that row is ignored.
	 * @param qryID is the identifier for the query
	 * @param docID is the document id for the feature map
	 * @param featureID is the feature id to add
	 * @param value is the actual value to be added
	 */
	public void insertRow(String qryID, String docID, String featureID, Double value){
		String rowID = getDocQueryKey(qryID, docID);
		// Store values in sparse matrix
		Row row = this.rows.get(rowID);
		if(row == null) return;
		
		row.featMap.put(featureID, value);
		this.rows.put(rowID, row);
		
		// Store minimum value for sparse matrix representation
		Feat f = this.feats.getOrDefault(featureID, new Feat(value, value));
		f = new Feat(Math.min(f.minFeatVal, value),
					 Math.max(f.maxFeatVal,  value));
		
		this.feats.put(featureID, f);
	}
	
	/**
	 * Sets the label for the given row
	 * @param qryID is the identifier for the query
	 * @param docID is the document id for the feature map
	 * @param label is the label to tag the row to
	 */
	public void setLabel(String qryID, String docID, Object label){
		Row row = this.rows.getOrDefault(docID, new Row());
		row.label = label;
		row.qryID = qryID;
		row.docID = docID;
		String rowID = getDocQueryKey(qryID, docID);
		this.rows.put(rowID, row);
		
		this.queries.add(qryID);
	}
	
	
	
	/**
	 * Prints the qualified rows to the output stream
	 * @param out is the given output stream
	 * @param train is the training output stream
	 * @param test is the testing output stream
	 */
	public void printFeatMatrix(PrintStream out, PrintStream train, PrintStream test){
		StringBuilder sb = new StringBuilder();
		
		sb.append("DocumentID" + SEPARATOR);
		for(Entry<String, Feat> e1:feats.entrySet()){
			sb.append(e1.getKey() + SEPARATOR);
		}
		sb.append("Label");
		String dat = sb.toString();
		out.println  (dat);
		train.println(dat);
		test.println (dat);
		
		
		Set<String> trainSet = new HashSet<String>();
		for(String q:queries){
			if(rnd.nextDouble()<=RANDOM_PERCENTAGE)
				trainSet.add(q);
		}
		
		for(Entry<String, Row> e : rows.entrySet()){
			Row r = e.getValue();
			sb = new StringBuilder();
			if(r.label != null && r.featMap.size()>0){
				sb.append(e.getKey() + SEPARATOR);
				
				for(Entry<String, Feat> e1:feats.entrySet()){
					Feat f = e1.getValue();
					
					Double val = r.featMap.getOrDefault(e1.getKey(), f.minFeatVal);
					val = (val - f.minFeatVal)/(f.maxFeatVal - f.minFeatVal);
					
					sb.append(val + SEPARATOR);
				}
				sb.append(r.label+"\n");
			}
			
			dat = sb.toString();
			out.println(dat);
			if(trainSet.contains(r.qryID))
				train.print(dat);
			else
				test.print(dat);
		}
	}
	
	/**
	 * Given query id and the document id generates a combined feature key
	 * @param qryID is the id of the query
	 * @param docID is the id of the document
	 * @return A combined key of both document and query
	 */
	private String getDocQueryKey(String qryID, String docID){
		return qryID + "-" + docID; 
	}
}
