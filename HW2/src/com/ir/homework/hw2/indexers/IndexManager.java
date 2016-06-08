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

import com.ir.homework.hw2.cache.CacheManager;
import com.ir.homework.hw2.indexers.CatalogManager.DocInfo;
import com.ir.homework.hw2.tokenizers.Tokenizer;
import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public class IndexManager implements Serializable, Flushable{
	// Serialization version ID
	private static final long serialVersionUID = 1L;
	
	private Tokenizer    tokenizer;
	private CacheManager translator;
	private Integer instanceID;
	private String  datFilePath;
	private String  indexPath;
	
	public Set<String> fieldSet;
	private Map<String, CatalogManager>  catalogMap;
	
	/**
	 * Default constructor
	 * @param indexID is the unique ID of index
	 * @param instanceID is the unique identifier of instance or thread
	 * @param tokenizer tokenizer used for current index
	 */
	public IndexManager(String indexID, Integer instanceID, Tokenizer tokenizer, CacheManager translator) {
		this.tokenizer  = tokenizer;
		this.translator = translator;
		
		this.instanceID = instanceID;
		this.indexPath  = INDEX_PATH + "/" + indexID;
		this.datFilePath = indexPath + "/" + instanceID;
		
		// Create directory structure
		(new File(indexPath)).mkdirs();
		
		this.catalogMap = new HashMap<String, CatalogManager>();
		
		this.fieldSet    = this.readFields();
		
	}
	

	/**
	 * Merges two indices using api
	 * @param idx1 First index to merge
	 * @param idx2 Second index to merge
	 * @throws Exception 
	 */
	public void mergeIndices(IndexManager idx1, IndexManager idx2) throws Exception{
		this.fieldSet.addAll(idx1.fieldSet);
		this.fieldSet.addAll(idx2.fieldSet);
		
		for(String field: this.fieldSet){
			CatalogManager cm = this.getCatalogManager(field);
			
			Set<String> terms = new HashSet<String>();
			terms.addAll(idx1.getTerms(field));
			terms.addAll(idx2.getTerms(field));
			
			for(String term: terms){
				Map<String, DocInfo> docsInfo1 = idx1.getTermPositionVector(field, term);
				Map<String, DocInfo> docsInfo2 = idx2.getTermPositionVector(field, term);
				
				docsInfo1.putAll(docsInfo2);
				
				cm.putData(term, docsInfo1);
			}
		}
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
	 * Term frequency vector is retrieved from catalog and data file of this instance
	 * @param field is the field to search for
	 * @param term is the term to search for
	 * @return Doc info wrapper object
	 * @throws Exception 
	 */
	public Map<String, DocInfo> getTermPositionVector(String field, String term) throws Exception{
		return this.getCatalogManager(field).readEntry(term);
	}
	
	/**
	 * Writes index to the internal buffer
	 * @param docID is the document id to be indexed
	 * @param data is the data to be indexed in terms of fields and values
	 * @throws IOException
	 */
	public final void putData(String docID, Map<String, String> data) throws IOException{
		for(Entry<String, String> fieldDat : data.entrySet()){
			this.fieldSet.add(fieldDat.getKey());
			
			CatalogManager cm = this.getCatalogManager(fieldDat.getKey());
			cm.putData(docID, fieldDat.getValue());
		}
	}
	
	/**
	 * Cleans the index file
	 * @param confirmDelete is the delete physical file confirmation
	 * @throws Throwable 
	 */
	public final void deleteIndex(Boolean confirmDelete) throws Throwable{
		for(String field : this.fieldSet){
			this.getCatalogManager(field).deleteIndex(confirmDelete);
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
	 * Gets list of fields indexed
	 * @return List of field names
	 */
	private final Set<String> readFields(){
		File datFolder = new File(this.indexPath);
		File files[] = datFolder.listFiles();
		
		Set<String> result = new HashSet<String>();
		for(File file: files){
			String nameSplit[] = file.getName().split("\\.");
			if(nameSplit.length>1 && nameSplit[0].equals(this.instanceID.toString())){
				result.add(nameSplit[1]);
			}
		}
		return result;
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
	
	//-----------------------------------------------------------------
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
				this.tokenizer, 
				this.translator);
		
		this.catalogMap.put(field, result);
		return result;
	}
}
