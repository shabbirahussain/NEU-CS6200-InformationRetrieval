package com.ir.homework.hw2.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CacheManager implements Serializable{
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> docInternalIDLookup;
	private Map<Integer, String> docBusinessIDLookup;
	private Integer masterIdxID; 
	private Integer stableIdxID;
	
	/**
	 * Default constructor
	 */
	public CacheManager(){
		this.docInternalIDLookup = new HashMap<String, Integer>();
		this.docBusinessIDLookup = new HashMap<Integer, String>();
		
		this.masterIdxID = 0;
		this.stableIdxID = 0;
	}
	
	/**
	 * Sets the stable index id to the latest one. The id can only be incremented. Any obsolete updates will be discarded.
	 * @param id is the given id of the stable index
	 * @return
	 */
	public CacheManager setLastStableIndexID(Integer id){
		if(this.stableIdxID == null || this.stableIdxID < id) 
			this.stableIdxID = id;
		return this;
	}
	
	
	// ----------------------------------------------------------------
	

	
	/**
	 * Returns last confirmed index id which is complete and ready to use
	 * @return
	 */
	public Integer getLastStableIndexID(){
		return this.stableIdxID;
	}
	
	/**
	 * Gets the next index file version ID
	 * @return New master index ID
	 */
	public Integer getNextIndexID(){
		return ++this.masterIdxID;
	}
	
	/**
	 * Gets the current index file version ID
	 * @return New master index ID
	 */
	public Integer getCurrIndexID(){
		return this.masterIdxID;
	}
	
	
	
	/** 
	 * Creates or loads a unique ID for each document
	 * @param key is the business key for a document
	 * @return Internal key for unique document ID
	 */
	public Integer translateDocID(String key){
		Integer id = this.docInternalIDLookup.get(key);
		if(id != null) return id; // found existing document id
		
		Integer value = this.docInternalIDLookup.size();
		this.docInternalIDLookup.put(key, value);
		this.docBusinessIDLookup.put(value, key);
		
		return this.translateDocID(key);
	}
	
	/** 
	 * Performs reverse lookup translating internal doc id to business key
	 * @param key is the internal key for the document
	 * @return Business key for unique document ID
	 */
	public String translateDocID(Integer key){
		return docBusinessIDLookup.get(key);
	}
}
