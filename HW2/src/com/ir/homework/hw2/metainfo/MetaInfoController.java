/**
 * 
 */
package com.ir.homework.hw2.metainfo;

import java.util.List;

import com.ir.homework.hw2.indexers.IndexManager;

/**
 * Used for thread synchronization
 * @author shabbirhussain
 *
 */
public class MetaInfoController {
	private String  indexID;
	private static MetaInfoModel model = new MetaInfoModel();
	/**
	 * Default constructor
	 * @param model is the model of meta info
	 * @param instanceID is the unique index identifier 
	 */
	public MetaInfoController(String indexID){
		this.indexID = indexID;
	}

	/**
	 * Sets usable indices for future use
	 * @param indexIDList is the list of index id to be set as usable
	 */
	public synchronized void setUsable(Integer version){
		model.setUsable(this.indexID, version);
	}
	
	/**
	 * Sets unusable indices for future use
	 * @param indexIDList is the list of index id to be set as usable
	 */
	public synchronized void setUnUsable(Integer version){
		model.setUnUsable(this.indexID, version);
	}

	/**
	 * Gets the next index file version ID
	 * @return New index ID
	 */
	public synchronized Integer getNextIndexID(){
		return model.getNextIndexID(this.indexID);
	}

	/**
	 * Returns the list of usable indices. A usable index is a index which has been successfully written to disk completely and is ready to use. 
	 * @return List of usable index
	 */
	public synchronized List<Integer> getUsableIndices(){
		return model.getUsableIndices(this.indexID);
	}

	/** 
	 * Creates or loads a unique ID for each document
	 * @param key is the business key for a document
	 * @return Internal key for unique document ID
	 */
	public synchronized Integer translateDocID(String key){
		return model.translateDocID(this.indexID, key);
	}

	/** 
	 * Performs reverse lookup translating internal doc id to business key
	 * @param key is the internal key for the document
	 * @return Business key for unique document ID
	 */
	public synchronized String translateDocID(Integer key){
		return model.translateDocID(this.indexID, key);
	}
	
	/**
	 * Gets the index manager for the given index version
	 * @param idxVer is the version of index
	 * @return IndexManager
	 */
	public IndexManager getIndexManager(Integer idxVer){
		return new IndexManager(this.indexID, idxVer, this);
	}

}

