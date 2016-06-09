package com.ir.homework.hw2.metainfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Stores meta information Model about indices and helps facilitate interprocess communication
 * @author shabbirhussain
 */
public class MetaInfoController implements Serializable{
	private static final long serialVersionUID = 1L;
	private Map<String, Integer> docInternalIDLookup;
	private Map<Integer, String> docBusinessIDLookup;
	private Integer masterIdxID; 
	private Integer stableIdxID;
	private List<Integer> usableIndexIds;
	
	/**
	 * Default constructor
	 */
	public MetaInfoController(){
		this.docInternalIDLookup = new HashMap<String, Integer>();
		this.docBusinessIDLookup = new HashMap<Integer, String>();
		this.usableIndexIds      = new LinkedList<Integer>();
		
		this.masterIdxID = 0;
		this.stableIdxID = 0;
	}
	
	
	/**
	 * Adds new index to usable list. With data from higher index id considered more reliable. 
	 * @param indexID is the unique index number/ version number
	 * @return MetaInfoController
	 */
	public MetaInfoController addUsableIndex(Integer indexID){
		this.usableIndexIds.add(indexID);
		return this;
	}
	
	/**
	 * Marks a index obsolete. Obsolete indices are no longer valid for use
	 * @param indexID is the unique index number/ version number
	 * @return MetaInfoController
	 */
	public MetaInfoController markIndexObsolete(Integer indexID){
		this.usableIndexIds.remove(indexID);
		return this;
	}
	
	/**
	 * Sets usable indices for future use
	 * @param indexIDList is the list of index id to be set as usable
	 * @return MetaInfoController
	 */
	public MetaInfoController setUsableIndices(List<Integer> indexIDList){
		this.usableIndexIds = indexIDList;
		return this;
	}
	// ----------------------------------------------------------------
	
	/**
	 * Returns the list of usable indices. A usable index is a index which has been successfully written to disk completely and is ready to use.
	 * @return List of usable index
	 */
	public List<Integer> getUsableIndices(){
		return this.usableIndexIds;
	}
	
	/**
	 * Gets the next index file version ID
	 * @return New master index ID
	 */
	public Integer getNextIndexID(){
		return ++this.masterIdxID;
	}
	
	
	// -------------------- Compressor and translators ----------------
	
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
