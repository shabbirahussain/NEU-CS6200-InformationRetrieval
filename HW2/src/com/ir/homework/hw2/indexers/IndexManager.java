/**
 * 
 */
package com.ir.homework.hw2.indexers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw2.cache.Translator;
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
	
	private Set<String> fieldSet;
	private Map<String, CatalogManager>  catalogMap;
	
	/**
	 * Default constructor
	 * @param indexID is the unique ID of index
	 * @param instanceID is the unique identifier of instance or thread
	 * @param tokenizer tokenizer used for current index
	 */
	public IndexManager(String indexID, Integer instanceID, Tokenizer tokenizer, Translator translator) {
		super(tokenizer, translator);
		
		this.instanceID = instanceID;
		this.indexPath  = INDEX_PATH + "/" + indexID;
		this.datFilePath = indexPath + "/" + instanceID;
		
		// Create directory structure
		(new File(indexPath)).mkdirs();
		
		this.fieldSet    = this.readFields();
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
			if(nameSplit.length>1 && nameSplit[0].equals(this.instanceID.toString())){
				result.add(nameSplit[1]);
			}
		}
		return result;
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
			
			for(String term: terms){
				Map<String, Integer> tfMap = new HashMap<String, Integer>();
				
				System.out.println("term="+term);
				tfMap.putAll(idx1.getTF(field, term));
				tfMap.putAll(idx2.getTF(field, term));
				System.out.println("tfmap="+tfMap);
				
				StringBuilder buffer = new StringBuilder();
				buffer.append(term + ":");
				
				catFile.write(term + ":");
				catFile.write(((Long)datFile.getFilePointer()).toString() + ":");
				
				// For each document for that term sorted by TD desc
				for(Entry<String, Integer> docTF : this.sortByValue(tfMap)){
					buffer.append(docTF.getKey() + ":" + docTF.getValue() + ":");
				}
				buffer.append("\n");
				
				datFile.writeChars(buffer.toString());
				catFile.write("\n");
			}
			datFile.close();
			catFile.close();
		}
	}
	
	public void write(){
		
	}
	
	
	
	/**
	 * Writes index to the disk any existing indices will be overwritten
	 * @throws IOException
	 */
	public void writeIndex() throws IOException{
		this.fieldSet.addAll(super.fieldTermDocMap.keySet());
		
		for(String field : this.fieldSet){
			String datFileName = this.getDatFileName(field);
			String catFileName = this.getCatFileName(field);
			
			RandomAccessFile datFile = new RandomAccessFile(datFileName, "rw");
			BufferedWriter   catFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(catFileName)));
			
			
			// For each term 
			for(Entry<String, Map<String, Integer>> term: fields.getValue().entrySet()){
				StringBuilder buffer = new StringBuilder();
				buffer.append(term.getKey() + ":");
				
				catFile.write(term.getKey() + ":");
				catFile.write(((Long)datFile.getFilePointer()).toString() + ":");
				
				// For each document for that term sorted by TD desc
				for(Entry<String, Integer> docTF : this.sortByValue(term.getValue())){
					buffer.append(docTF.getKey() + ":" + docTF.getValue() + ":");
				}
				buffer.append("\n");
				datFile.writeChars(buffer.toString().replaceAll("[^\\d:]", ""));
				
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
			System.out.println(tf);
			if(!tf.equals("")) result.put(docTF[i], Integer.parseInt(tf));
			
		}
		datFile.close();

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

	/**
	 * Cleans the index file
	 * @throws IOException 
	 */
	public final void deleteIndex() throws IOException{
		Set<String> fields = this.fieldCatMap.keySet();
		for(String field : fields){
			Files.deleteIfExists(Paths.get(getDatFileName(field)));
			Files.deleteIfExists(Paths.get(getCatFileName(field)));
		}
	}
	//-----------------------------------------------------------------
	private getCatalogManager(String term){
		this.catalogMap.getOrDefault(term, new CatalogManager(term, term));
	}
	
}
