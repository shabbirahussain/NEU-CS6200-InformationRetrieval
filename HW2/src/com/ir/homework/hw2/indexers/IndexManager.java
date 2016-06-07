/**
 * 
 */
package com.ir.homework.hw2.indexers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw2.indexers.IndexModel;
import com.ir.homework.hw2.tokenizers.Tokenizer;
import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public class IndexManager extends IndexModel {
	// Serialization version ID
	private static final long serialVersionUID = 1L;

	private Integer instanceID;
	private String  datFilePath;
	private String  indexPath;
	
	private Map<String, Map<String, Long>> fieldCatMap;
	
	/**
	 * Default constructor
	 * @param indexID is the unique ID of index
	 * @param instanceID is the unique identifier of instance or thread
	 * @param tokenizer tokenizer used for current index
	 */
	public IndexManager(String indexID, Integer instanceID, Tokenizer tokenizer) {
		super(tokenizer);
		
		this.instanceID = instanceID;
		this.indexPath  = INDEX_PATH + "/" + indexID;
		this.datFilePath = indexPath + "/" + instanceID;
		
		// Create directory structure
		(new File(indexPath)).mkdirs();
		fieldCatMap = null; 
	}
	
	/**
	 * Merges two indices using api
	 * @param idx1 First index to merge
	 * @param idx2 Second index to merge
	 * @throws Exception 
	 */
	public void mergeIndices(IndexManager idx1, IndexManager idx2) throws Exception{
		Set<String> fields = new HashSet<String>();
		fields.addAll(idx1.loadCatMap().keySet());
		fields.addAll(idx2.loadCatMap().keySet());
		
		for(String field: fields){
			
			String datFileName = this.getDatFileName(field);
			String catFileName = this.getCatFileName(field);
			
			RandomAccessFile datFile = new RandomAccessFile(datFileName, "rw");
			BufferedWriter   catFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(catFileName)));
			
			Set<String> terms = new HashSet<String>();
			terms.addAll(idx1.getTermsList(field));
			terms.addAll(idx2.getTermsList(field));
			
			Map<String, Integer> tfMap = new HashMap<String, Integer>();
			for(String term: terms){
				tfMap.putAll(idx1.getTF(field, term));
				tfMap.putAll(idx2.getTF(field, term));
				
				datFile.writeChars(term + ":");
				
				catFile.write(term + ":");
				catFile.write(((Long)datFile.getFilePointer()).toString() + ":");
				
				// For each document for that term sorted by TD desc
				for(Entry<String, Integer> docTF : this.sortByValue(tfMap)){
					datFile.writeChars(docTF.getKey() + ":" + docTF.getValue() + ":");
				}
				datFile.writeChars("\n");
				catFile.write("\n");
			}
			datFile.close();
			catFile.close();
		}
	}
	
	
	/**
	 * Gets list of fields indexed
	 * @return List of field names
	 */
	private List<String> readFields(){
		File datFolder = new File(this.indexPath);
		File files[] = datFolder.listFiles();
		
		List<String> result = new LinkedList<String>();
		for(File file: files){
			String nameSplit[] = file.getName().split("\\.");
			if(nameSplit.length>1 && nameSplit[0].equals(this.instanceID.toString())){
				result.add(nameSplit[1]);
			}
		}
		
		return result;
	}
	
	/**
	 * Writes index to the disk any existing indices will be overwritten
	 * @throws IOException
	 */
	public void writeIndex() throws IOException{
		for(Entry<String, Map<String, Map<String, Integer>>> fields : super.fieldTermDocMap.entrySet()){
			String field = fields.getKey();
			
			String datFileName = this.getDatFileName(field);
			String catFileName = this.getCatFileName(field);
			
			RandomAccessFile datFile = new RandomAccessFile(datFileName, "rw");
			BufferedWriter   catFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(catFileName)));
			
			
			// For each term 
			for(Entry<String, Map<String, Integer>> term: fields.getValue().entrySet()){
				datFile.writeChars(term.getKey() + ":");
				
				catFile.write(term.getKey() + ":");
				catFile.write(((Long)datFile.getFilePointer()).toString() + ":");
				
				// For each document for that term sorted by TD desc
				for(Entry<String, Integer> docTF : this.sortByValue(term.getValue())){
					datFile.writeChars(docTF.getKey() + ":" + docTF.getValue() + ":");
				}
				datFile.writeChars("\n");
				//catFile.write(((Long)datFile.getFilePointer()).toString() + ":");
				catFile.write("\n");
			}
			datFile.close();
			catFile.close();
		}
	}
	
	/**
	 * Term frequency vector is retrieved from catalog and data file of this instance
	 * @param field is the field to search for
	 * @param term is the term to search for
	 * @return Gets TF for given term from index
	 * @throws Exception 
	 */
	public Map<String, Integer> getTF(String field, String term) throws Exception{
		if(this.fieldCatMap == null) this.loadCatMap();
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		Long pos = this.fieldCatMap.get(field).get(term);
		if(pos==null) return result; // no entries found in index
		
		String datFileName = this.getDatFileName(field);
		RandomAccessFile datFile = new RandomAccessFile(datFileName, "r");
		
		datFile.seek(pos);
		String dat = datFile.readLine();
		String[] docTF = dat.split(":");
			
		for(int i=0; i<docTF.length-1; i+=2){
			String tf = docTF[i+1].replaceAll("[^\\d]", "");
			result.put(docTF[i], Integer.parseInt(tf));
			
		}
		datFile.close();

		return result;
	}
	
	/**
	 * Given a field returns the idx file name for index
	 * @param field is the name of the field
	 * @return Fully qualified name of idx file
	 */
	private String getDatFileName(String field){
		return this.datFilePath + "." + field + ".dat.txt";
	}

	/**
	 * Given a field returns the catalog file name for index
	 * @param field is the name of the field
	 * @return Fully qualified name of cat file
	 */
	private String getCatFileName(String field){
		return this.getDatFileName(field) + ".cat.txt";
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
	
	/**
	 * Cleans the index file
	 * @throws IOException 
	 */
	public void deleteIndex() throws IOException{
		Set<String> fields = this.fieldCatMap.keySet();
		for(String field : fields){
			Files.deleteIfExists(Paths.get(getDatFileName(field)));
			Files.deleteIfExists(Paths.get(getCatFileName(field)));
		}
	}
	

	/**
	 * Loads the catalog file into memory
	 * @return Map of field vs term vs position
	 * @throws IOException 
	 */
	private Map<String, Map<String, Long>> loadCatMap() throws IOException{
		this.fieldCatMap = new HashMap<String, Map<String, Long>>();
		
		List<String> fields = this.readFields();
		for(String field: fields){
			String catFileName = this.getCatFileName(field);
			BufferedReader   catFile = new BufferedReader(new InputStreamReader(new FileInputStream(catFileName)));
			
			Map<String, Long> catMap = new HashMap<String, Long>();
			String line;
			while((line=catFile.readLine())!=null){
				String[] entries = line.split(":");
				catMap.put(entries[0], Long.parseLong(entries[1]));
			}
			catFile.close();
			this.fieldCatMap.put(field, catMap);
		}
		
		return this.fieldCatMap;
	}

	
	/**
	 * Gets the list of terms present in the index
	 * @param field is the name of field to search for
	 * @return List of terms
	 * @throws IOException 
	 */
	private Set<String> getTermsList(String field) throws IOException{
		Map<String, Long> catMap = this.fieldCatMap.get(field);
		if(catMap != null) return catMap.keySet();
		
		loadCatMap();
		return getTermsList(field);
	}
}
