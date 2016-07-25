package com.ir.homework.hw6.models;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.ir.homework.hw6.Constants.*;

public class FeatMatrix implements Serializable{
	// ------------------ Constants -------------------- //
	private static final long serialVersionUID = 2L;

	// -------------------- Private -------------------- //
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
		
		public Row(){
			this.featMap = new HashMap<String, Double>();
			this.label   = null;
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
	}
	

	// ------------ Interface Methods ------------------ //
	/**
	 * Adds a row to the feature model. A label must be added before adding any features. If no label is provided for given id that row is ignored.
	 * @param docID is the document id for the feature map
	 * @param featureID is the feature id to add
	 * @param value is the actual value to be added
	 */
	public void insertRow(String docID, String featureID, Double value){
		// Store values in sparse matrix
		Row row = this.rows.get(docID);
		if(row == null) return;
		
		row.featMap.put(featureID, value);
		this.rows.put(docID, row);
		
		// Store minimum value for sparse matrix representation
		Feat f = this.feats.getOrDefault(featureID, new Feat(value, value));
		f = new Feat(Math.min(f.minFeatVal, value),
					 Math.max(f.maxFeatVal,  value));
		
		this.feats.put(featureID, f);
	}
	
	/**
	 * Sets the label for the given row
	 * @param docID is the document id for the feature map
	 * @param label is the label to tag the row to
	 */
	public void setLabel(String docID, Object label){
		Row row = this.rows.getOrDefault(docID, new Row());
		row.label = label;
		this.rows.put(docID, row);
	}
	
	
	
	/**
	 * Prints the qualified rows to the output stream
	 * @param out is the given output stream
	 */
	public void printFeatMatrix(PrintStream out){
		out.print("DocumentID" + SEPARATOR);
		for(Entry<String, Feat> e1:feats.entrySet()){
			out.print(e1.getKey() + SEPARATOR);
		}
		out.println("Label");
		
		for(Entry<String, Row> e : rows.entrySet()){
			Row r = e.getValue();
			if(r.label != null && r.featMap.size()>0){
				out.print(e.getKey() + SEPARATOR);
				
				for(Entry<String, Feat> e1:feats.entrySet()){
					Feat f = e1.getValue();
					
					Double val = r.featMap.getOrDefault(e1.getKey(), f.minFeatVal);
					val = (val - f.minFeatVal)/(f.maxFeatVal - f.minFeatVal);
					
					out.print(val + SEPARATOR);
				}
				out.println(r.label);
			}
		}
	}

}
