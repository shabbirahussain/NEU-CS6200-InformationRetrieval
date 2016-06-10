/**
 * 
 */
package com.ir.homework.hw2.indexers;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw2.indexers.CatalogManager.DocInfo;
import com.ir.homework.hw2.indexers.CatalogManager.TermInfo;
import com.ir.homework.hw2.metainfo.MetaInfoController;
import com.ir.homework.hw2.tokenizers.Tokenizer;
import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public class IndexManager implements Serializable, Flushable{
	// Serialization version ID
	private static final long serialVersionUID = 1L;

	private MetaInfoController metaSynchronizer;
	private String    indexID;
	private Integer   version;
	private String    datFilePath;
	private String    indexPath;
	
	public Set<String> fieldSet;
	private Map<String, CatalogManager>  catalogMap;
	
	/**
	 * Default constructor
	 * @param indexID is the unique ID of index
	 * @param instanceID is the unique identifier of instance or thread
	 * @param tokenizer tokenizer used for current index
	 */
	public IndexManager(String indexID, Integer version, MetaInfoController metaSynchronizer) {
		this.indexID   = indexID;
		this.version   = version;
		this.metaSynchronizer = metaSynchronizer;
		
		this.indexPath   = INDEX_PATH + "/" + indexID;
		this.datFilePath = indexPath + "/" + version;
		
		// Create directory structure
		(new File(indexPath)).mkdirs();
		
		this.catalogMap = new HashMap<String, CatalogManager>();
		
		this.fieldSet    = this.readFields();
		
	}

	/**
	 * Merges two indices using api
	 * @param index2 Second index version number to merge
	 * @param batchSize is the batch size to set flush interval
	 * @throws Throwable 
	 */
	public IndexManager mergeIndices(Integer index2, Integer batchSize) throws Throwable{
		Integer newIdxID = metaSynchronizer.getNextIndexID();
		
		IndexManager idxM = new IndexManager(this.indexID, newIdxID, metaSynchronizer);
		IndexManager idx2 = new IndexManager(this.indexID, index2  , metaSynchronizer);
		
		this.fieldSet.addAll(this.fieldSet);
		this.fieldSet.addAll(idx2.fieldSet);
		
		Integer i = 0;
		for(String field: this.fieldSet){
			CatalogManager cm = idxM.getCatalogManager(field);
			
			Set<String> terms = new HashSet<String>();
			terms.addAll(this.getTerms(field));
			terms.addAll(idx2.getTerms(field));
			
			for(String term: terms){
				Map<String, DocInfo> docsInfo1 = this.getTermPositionVector(field, term);
				Map<String, DocInfo> docsInfo2 = idx2.getTermPositionVector(field, term);
				
				docsInfo1.putAll(docsInfo2);
				idxM.setTermPositionVector(field, term, docsInfo1);
				
				// flush data periodically
				if(++i >= batchSize){
					i = 0;
					cm.flush();
				}
			}
			cm.flush();
		}

		idxM.fieldSet = this.fieldSet;
		
		metaSynchronizer.setUnUsable(this.version);
		metaSynchronizer.setUnUsable(idx2.version);
		metaSynchronizer.setUsable(idxM.version);
		
		this.deleteIndex(true);
		idx2.deleteIndex(true);

		return idxM;
	}
	
	
	/**
	 * Returns terms present in index
	 * @param field is the field to search for
	 * @return Set of terms present in the index
	 * @throws IOException 
	 */
	public Set<String> getTerms(String field) throws IOException{
		return this.getCatalogManager(field).getTerms();
	}

	/**
	 * Gets the current instance ID
	 * @return Unique instance identifier
	 */
	public Integer getInstanceID(){
		return this.version;
	}
	
	/**
	 * Reads the term info 
	 * @param field is the field to search for
	 * @param term is the term to search for
	 * @return Doc info wrapper object
	 * @throws Exception 
	 */
	public TermInfo getTermInfo(String field, String term) throws Exception{
		return this.getCatalogManager(field)
				.readEntry(term);
	}
	
	
	// -------------------- Setters -----------------------------------
	
	/**
	 * Sets the term position vector
	 * @param field is the field to search for
	 * @param term is the term to search for
	 * @param docsInfo vector to set
	 * @throws Exception
	 */
	private void setTermPositionVector(String field, String term, Map<String, DocInfo> docsInfo) throws Exception{
		this.getCatalogManager(field).putData(term, docsInfo);
	}
	
	/**
	 * Writes index to the internal buffer
	 * @param docID is the document id to be indexed
	 * @param data is the data to be indexed in terms of fields and values
	 * @param tokenizer is the tokenizer to be used to parse the data
	 * @throws IOException
	 */
	public final void putData(String docID, Map<String, String> data, Tokenizer tokenizer) throws IOException{
		for(Entry<String, String> fieldDat : data.entrySet()){
			this.fieldSet.add(fieldDat.getKey());
			
			CatalogManager cm = this.getCatalogManager(fieldDat.getKey());
			cm.putData(docID, fieldDat.getValue(), tokenizer);
		}
	}
	
	/**
	 * Cleans the index file
	 * @param confirmDelete is the delete physical file confirmation
	 * @throws Throwable 
	 */
	public final void deleteIndex(Boolean confirmDelete) throws Throwable{
		for(String field : this.fieldSet){
			this.getCatalogManager(field).deleteIndex(ENABLE_AUTO_CLEAN); // TODO
		}
	}
	
	@Override
	public final void flush() throws IOException{
		for(String field: this.fieldSet){
			this.getCatalogManager(field).flush();
		}
	}
	
	
	// ----------------------------------------------------------------
	/**
	 * Term frequency vector is retrieved from catalog and data file of this instance
	 * @param field is the field to search for
	 * @param term is the term to search for
	 * @return Doc info wrapper object
	 * @throws Exception 
	 */
	private Map<String, DocInfo> getTermPositionVector(String field, String term) throws Exception{
		return this.getCatalogManager(field)
				.readEntry(term)
				.docsInfo;
	}
	
	/**
	 * Gets list of fields indexed
	 * @return List of field names
	 */
	private final Set<String> readFields(){
		File datFolder = new File(this.indexPath);
		File files[] = datFolder.listFiles();
		
		Set<String> result = new HashSet<String>();
		for(File file: files){
			String nameSplit[] = file.getName().split("\\.");
			if(nameSplit.length>1 && nameSplit[0].equals(this.version.toString())){
				result.add(nameSplit[1]);
			}
		}
		return result;
	}
	
	
	/**
	 * Fetches a handler for a cataloged index
	 * @param field is the field name to query
	 * @return Object of catalog manager
	 * @throws IOException 
	 */
	private final CatalogManager getCatalogManager(String field) throws IOException{
		CatalogManager result = this.catalogMap.get(field);
		if(result != null) return result;
		
		result = new CatalogManager(field, 
				this.datFilePath,
				metaSynchronizer);
		
		this.catalogMap.put(field, result);
		return result;
	}
	
	/**
	 * Finalizeses the object forcefully deleting any files associated with it
	 * @param deleteFiles specifies to delete index files from hard disk
	 */
	public final void finalize(Boolean deleteFiles) throws IOException, Throwable{
		for(String field : this.fieldSet){
			this.getCatalogManager(field).finalize(deleteFiles);
		}
	}
	

	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map is the map to sort
	 * @return List of map entries in sorted form
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Entry<String, Integer>> sortByValue(Map map) {
	     List<Entry<String, Integer>> list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return -((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
}
