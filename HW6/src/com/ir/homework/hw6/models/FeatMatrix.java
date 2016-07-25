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
	private static final long serialVersionUID = 1L;

	// -------------------- Private -------------------- //
	private Map<String, Row>    rows; // Stores features in a hash map
	private Map<String, Object> minFeatVal;

	/**
	 * Stores the row information for each row
	 * @author shabbirhussain
	 */
	private class Row implements Serializable{
		private static final long serialVersionUID = 1L;
		public Map<String, Object> featMap;
		public Object label;
		
		public Row(){
			this.featMap = new HashMap<String, Object>();
			this.label   = null;
		}
	}
	
	// ----------------- Constructors ------------------ //
	public FeatMatrix() {
		rows       = new HashMap<String, Row>();
		minFeatVal = new LinkedHashMap<String, Object>();
	}
	

	// ------------ Interface Methods ------------------ //
	/**
	 * Adds a row to the feature model. A label must be added before adding any features. If no label is provided for given id that row is ignored.
	 * @param docID is the document id for the feature map
	 * @param featureID is the feature id to add
	 * @param value is the actual value to be added
	 */
	public <V extends Comparable<V>> void insertRow(String docID, String featureID, V value){
		// Store values in sparse matrix
		Row row = this.rows.get(docID);
		if(row == null) return;
		
		row.featMap.put(featureID, value);
		this.rows.put(docID, row);
		
		// Store minimum value for sparse matrix representation
		@SuppressWarnings("unchecked")
		V featVal    = (V) this.minFeatVal.getOrDefault(featureID, value);
		V minFeatVal = (value.compareTo(featVal)==-1)? value : featVal;
		this.minFeatVal.put(featureID, minFeatVal);
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
		for(Entry<String, Row> e : rows.entrySet()){
			Row r = e.getValue();
			if(r.label != null){
				out.print(r.label    + SEPARATOR);
				out.print(e.getKey() + SEPARATOR);
				
				for(Entry<String, Object> f:minFeatVal.entrySet()){
					out.print(r.featMap.getOrDefault(f.getKey(), f.getValue())
							+ SEPARATOR);
				}
				out.println("\b");
			}
		}
	}

}
