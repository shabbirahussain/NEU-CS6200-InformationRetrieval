package com.ir.homework.hw2.metainfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ir.homework.hw2.io.ObjectStore;

/**
 * Stores meta information Model about indices and helps facilitate interprocess communication
 * @author shabbirhussain
 */
public class MetaInfoModel implements Serializable{
	private static final long serialVersionUID = 1L;
	private MetaInfoIntModels models;
	
	/**
	 * Used to store index specific information
	 * @author shabbirhussain
	 */
	class MetaInfoIntModel implements Serializable{
		static final long serialVersionUID = 1L;

		Map<String, Integer> docInternalIDLookup;
		Map<Integer, String> docBusinessIDLookup;
		List<Integer>        usableIndexIds;
		
		Integer lastIndexID ;
		
		/**
		 * Default constructor
		 */
		public MetaInfoIntModel(){
			this.docInternalIDLookup = new HashMap<String, Integer>();
			this.docBusinessIDLookup = new HashMap<Integer, String>();
			this.usableIndexIds      = new LinkedList<Integer>();
			this.lastIndexID = 0;
		}
	}
	
	/**
	 * Stores all index related information
	 * @author shabbirhussain
	 */
	private class MetaInfoIntModels extends HashMap<String, MetaInfoIntModel> implements Serializable{
		static final long serialVersionUID = 1L;
	}
	
	// ----------------------------------------------------------------
	
	/**
	 * Default constructor
	 */
	public MetaInfoModel(){
		this.models = (MetaInfoIntModels) ObjectStore.getOrDefault(new MetaInfoIntModels());
	}

	// ----------------------------------------------------------------
	
	/**
	 * Gets the next index file version ID
	 * @param indexID is the unique identifier of index
	 * @return New index file id
	 */
	public synchronized Integer getNextIndexID(String indexID){
		return this.getModel(indexID).lastIndexID++;
	}

	/**
	 * Returns the list of usable indices. A usable index is a index which has been successfully written to disk completely and is ready to use.
	 * @param indexID is the unique identifier of index 
	 * @return List of usable index
	 */
	public synchronized List<Integer> getUsableIndices(String indexID){
		return this.getModel(indexID).usableIndexIds;
	}

	/** 
	 * Creates or loads a unique ID for each document
	 * @param indexID is the unique identifier of index 
	 * @param key is the business key for a document
	 * @return Internal key for unique document ID
	 */
	public synchronized Integer translateDocID(String indexID, String key){
		Map<String, Integer> docLookup = this.getModel(indexID).docInternalIDLookup;
		Integer id = docLookup.get(key);
		if(id != null) return id;
		
		
		id = docLookup.size();
		this.getModel(indexID).docInternalIDLookup.put(key, id);
		this.getModel(indexID).docBusinessIDLookup.put(id, key);
		
		return id;
	}

	/** 
	 * Performs reverse lookup translating internal doc id to business key
	 * @param indexID is the unique identifier of index 
	 * @param key is the internal key for the document
	 * @return Business key for unique document ID
	 */
	public synchronized String translateDocID(String indexID, Integer key){
		return this.getModel(indexID).docBusinessIDLookup.get(key);
	}
	
	/**
	 * Sets an index version unusable
	 * @param indexID is the unique identifier of index 
	 * @param version is the internal version number of index
	 * @return MetaInfoModel
	 */
	public synchronized MetaInfoModel setUnUsable(String indexID, Integer version){
		this.getModel(indexID).usableIndexIds.remove(version);
		return this;
	}
	
	/**
	 * Sets an index version usable
	 * @param indexID is the unique identifier of index 
	 * @param version is the internal version number of index
	 * @return MetaInfoModel
	 */
	public synchronized MetaInfoModel setUsable(String indexID, Integer version){
		this.getModel(indexID).usableIndexIds.add(version);
		return this;
	}
	
	// ----------------------------------------------------------------

	 /**
	  * Returns the model for given index id
	  * @param indexID is the unique identifier of index 
	  * @return MetaInfoIntModel
	  */
	private final MetaInfoIntModel getModel(String indexID){
		MetaInfoIntModel model = this.models.get(indexID);
		if(model != null) return model;
		
		this.models.put(indexID, new MetaInfoIntModel());
		return this.getModel(indexID);
	}
	
	/**
	 * Saves the model to the hard drive
	 */
	public void save(){
		System.out.println("Saving object...");
		ObjectStore.saveObject(this.models);
	}
	
	@Override
	public void finalize(){
		save();
	}
}
