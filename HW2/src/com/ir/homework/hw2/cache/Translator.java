package com.ir.homework.hw2.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Translator implements Serializable{
	private static final long serialVersionUID = 1L;

	private Map<String, Integer> docIDLookup;
	
	public Translator(){
		docIDLookup = new HashMap<String, Integer>();
	}
	
	/** 
	 * Creates or loads a unique ID for each document
	 * @param key Business key for a document
	 * @return Internal key for unique document ID
	 */
	public Integer translateDocID(String key){
		Integer id = this.docIDLookup.get(key);
		if(id != null) return id; // found existing document id
		
		this.docIDLookup.put(key, this.docIDLookup.size());
		return this.translateDocID(key);
	}
	
}
