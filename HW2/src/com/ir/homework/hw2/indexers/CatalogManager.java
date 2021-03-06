package com.ir.homework.hw2.indexers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import java.util.Map.Entry;

import com.ir.homework.hw2.metainfo.MetaInfoController;
import com.ir.homework.hw2.tokenizers.Tokenizer;
import static com.ir.homework.hw2.Constants.*;

import java.util.Set;

public class CatalogManager implements Serializable, Flushable{
	private static final long serialVersionUID = 1L;
	private static final Integer MAX_DOCS_PER_TERM = Integer.MAX_VALUE;

	private MetaInfoController metaSynchronizer;
	private String       datFilePath;
	private String       fieldName;
	private FileChannel  datFileRW;
	private FileChannel  datFileWr;
	
	private Map<String, CatInfo> catalogMap;
	private Map<String, TermInfo> termInfoMap;
	
	public class TermInfo implements Serializable{
		private static final long serialVersionUID = 1L;
		public Map<String, DocInfo> docsInfo;
		
		/**
		 * Default constructor
		 */
		public TermInfo(){
			docsInfo = new HashMap<String, DocInfo>();
		}
		
		/**
		 * Merges information from another term info object
		 * @param i2 is the other term info object to be merged
		 * @return Merged information object
		 */
		public TermInfo merge(TermInfo i2){
			this.docsInfo.putAll(i2.docsInfo);
			return this;
		}
		
		/**
		 * Merges information from another term info object
		 * @param info is the collection of other term info object to be merged
		 * @return Merged information object
		 */
		public TermInfo mergeAll(Collection<TermInfo> info){
			for(TermInfo d : info){
				this.merge(d);
			}
			return this;
		}
		
		@Override
		public String toString(){
			return this.docsInfo.toString();
		}
	}
	
	/**
	 * Stores document information for every term in the reverse index
	 * @author shabbirhussain
	 */
	public class DocInfo implements Serializable, Comparable<DocInfo> {
		private static final long serialVersionUID = 1L;
		public List<Long> docPos;
		
		public DocInfo(){
			this.docPos = new LinkedList<Long>();
		}

		@Override
		public int compareTo(DocInfo o) {
			Integer thisSize = this.docPos.size();
			Integer othrSize = o.docPos.size();
			
			return thisSize.compareTo(othrSize);
		}
		
		@Override
		public String toString(){
			return this.docPos.toString();
		}
	}

	/**
	 * Stores catalog info for a term
	 * @author shabbirhussain
	 */
	public class CatInfo implements Serializable{
		private static final long serialVersionUID = 1L;
		Long    offset;
		Integer length;
		public Long df;
		public Long ttf;
		
		public CatInfo(){
			this.df  = 0L;
			this.ttf = 0L;
		}

		/**
		 * Merges information from another term info object
		 * @param i2 is the other term info object to be merged
		 * @return Merged information object
		 */
		public CatInfo merge(CatInfo i2){
			this.df  += i2.df;
			this.ttf += i2.ttf;
			return this;
		}
	}
	
	/**
	 * Creates a field object with input and output streams connected to data files and catalog. It uses the myName parameter to automatically decide which index file it will be bound to.
	 * @param myName is the name of field or instance 
	 * @param datFilePath is the base path of the index files
	 * @param tokenizer 
	 * @param translator 
	 * @throws IOException 
	 */
	public CatalogManager(String myName, String  datFilePath, MetaInfoController metaSynchronizer) throws IOException{
		this.datFilePath = datFilePath;
		this.fieldName   = myName;
		this.metaSynchronizer  = metaSynchronizer;
		
		this.catalogMap  = new HashMap<String, CatInfo>();
		this.termInfoMap = new HashMap<String, TermInfo>();

		this.loadCatMap();
		this.openIndexStreams();
	}
	
	/**
	 * Reads the document information from index for the term
	 * @param term is the given term to search for
	 * @return Map of document and information stored in index
	 * @throws Exception is thrown for IOException or ArrayIndexOutOfBounds when file underlying is corrupted
	 */
	public TermInfo readEntry(String term) throws Exception{
		CatInfo catInfo = this.catalogMap.get(term);
		if(catInfo == null) return (new TermInfo());
		
		Map<String, DocInfo> docInfoMap = new HashMap<String, DocInfo>();
		
		// Read from file 
		Long    position = catInfo.offset;
		Integer capacity = catInfo.length;
				
		ByteBuffer dst = ByteBuffer.allocate(capacity);
		datFileRW.read(dst, position);
		
		
		String buffer   = new String(dst.array());
		String tokens[] = buffer.split(":");
		for(int i=1; i<tokens.length-1; i+=2){
			String  key   = tokens[i]; // Document id
			try{
				key = metaSynchronizer.translateDocID(Integer.parseInt(key));
			}catch(Exception e){}
			
			
			DocInfo value = new DocInfo(); 
			
			String tokens1[] = tokens[i+1].split(";");
			Long prevPos = 0L;
			for(int j=0; j<tokens1.length; j++){
				Long delta = Long.parseLong(tokens1[j]);
				Long currPos = prevPos + delta;
				
				value.docPos.add(currPos);
				prevPos = currPos;
			}
			docInfoMap.put(key, value);
		}
		
		TermInfo result = new TermInfo();
		result.docsInfo = docInfoMap;
		
		return result;
	}
		
	/**
	 * Caution!!! Cleans the index file permanently from disk.
	 * @param confirm is the confirm delete flag which removes file only when true
	 * @throws Throwable 
	 */
	public final void deleteIndex(Boolean confirm) throws Throwable{
		if(!confirm) return;
		
		Files.deleteIfExists(Paths.get(getDatFileName()));
		Files.deleteIfExists(Paths.get(getCatFileName()));
		
		this.finalize();
	}
	
	/**
	 * Gets the list of terms available in index
	 * @return Set of terms 
	 */
	public final Set<String> getTerms(){
		return this.catalogMap.keySet();
	}
	
	/**
	 * Gets the term statistics for given term from the catalog 
	 * @param term is the given term to search for
	 * @return CatInfo
	 */
	public final CatInfo getTermStats(String term){
		return this.catalogMap.getOrDefault(term, new CatInfo());
	}
	
	/**
	 * Loads data provided in map into the index
	 * @param docID unique identifier of document
	 * @param data text data to be indexed
	 * @param tokenizer is the tokenizer to be used for data parsing
	 */
	public void putData(String docID, String data, Tokenizer tokenizer){
		docID = this.metaSynchronizer.translateDocID(docID).toString();
		Map<String, DocInfo> docsInfo;
		DocInfo docInfo;
		
		List<String> terms = tokenizer.tokenize(data);
		// for each term in data
		for(Integer i=0; i<terms.size(); i++){
			String term = terms.get(i);
			
			TermInfo termInfo = this.termInfoMap.getOrDefault(term, (new TermInfo()));
			docsInfo = termInfo.docsInfo;
			docInfo  = docsInfo.getOrDefault(docID, (new DocInfo()));
			
			// add current position to map
			docInfo.docPos.add(i.longValue());
				
			// Store data back
			docsInfo.put(docID, docInfo);
			termInfo.docsInfo = docsInfo;
			this.termInfoMap.put(term, termInfo);
		}
	}
	
	/**
	 * Loads processed term data into map
	 * @param term is the given term to load
	 * @param docsInfo is the statistics to be loaded into index
	 * @return TermInfo
	 */
	public TermInfo putEntry(String term, TermInfo termInfo){
		return this.termInfoMap.put(term, termInfo);
	}
	
	@Override
	public void flush() throws IOException{
		Path datFilePath = Paths.get(this.getDatFileName());
		this.datFileWr = FileChannel.open(datFilePath, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		
		for(Entry<String, TermInfo> termInfo : this.termInfoMap.entrySet()){
			try {
				this.writeEntry(termInfo.getKey(), termInfo.getValue().docsInfo);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		this.termInfoMap = new HashMap<String, TermInfo>();
	}
	
	// ------------------------------------------------------------

	/**
	 * Writes an entry to index file and simultaneously creates a catalog of it
	 * @param term is the term to index
	 * @param is the doc information map
	 * @throws Exception is thrown in case of term requested to be written is already present in file or an IOException has occurred
	 */
	private void writeEntry(String term, Map<String, DocInfo> docInfoMap) throws Exception{
		// Raises an exception for overwriting an existing term in catalog
		if(this.catalogMap.containsKey(term)) 
			throw (new Exception("Cannot overrite an existing entry for term: " + term));
		
		
		// convert data to byte buffer
		StringBuilder buffer = new StringBuilder();
		buffer.append(term).append(":");
		
		Long df  = 0L;
		Long ttf = 0L;
		for(Entry<String, DocInfo> docInfo : this.getSortedList(docInfoMap, false)){
			String docID = docInfo.getKey();
			try{
				if(ENABLE_FULL_DOC_ID)
					docID = metaSynchronizer.translateDocID(Integer.parseInt(docID));
			}catch(NumberFormatException e){}
			
			buffer.append(docID).append(":");
			
			//List<Long> posList = docInfo.getValue().docPos;
			//Long sortedPos[] = new Long[posList.size()];
			//docInfo.getValue().docPos.toArray(sortedPos);
			//Arrays.sort(sortedPos);
			
			Long prevPos = 0L;
			for(Long currPos : docInfo.getValue().docPos){
				Long delta = (currPos - prevPos);
				buffer.append(delta.toString()).append(";");

				// Save current pos for future use
				prevPos = currPos;
				ttf++;
			}
			buffer.append(":");
			
			// break the loop if max limit of docs is reached
			if(++df > MAX_DOCS_PER_TERM) break;
			
		}
		buffer.append("\n");
		
		ByteBuffer src = ByteBuffer.wrap(buffer.toString().getBytes());
		
		// write data to index file
		Long position = datFileWr.size();
		datFileWr.position(position);
		datFileWr.write(src);

		CatInfo catInfo = new CatInfo();
		catInfo.offset  = position;
		catInfo.length  = ((Long)(datFileWr.size() - position)).intValue();
		catInfo.df      = df;
		catInfo.ttf     = ttf;
		
		this.catalogMap.put(term, catInfo);
	}
	
	/**
	 * Loads the catalog file into memory
	 * @return Map of field vs term vs position
	 * @throws IOException 
	 */
	private void loadCatMap() throws IOException{
		String catFileName = this.getCatFileName();

		// Return if no catalog file is present
		if(! Files.exists(Paths.get(catFileName))) return;
		
		BufferedReader catFile = new BufferedReader(new FileReader(catFileName));
		
		String line;
		while((line=catFile.readLine())!=null){
			String[] entries = line.split(":");
			
			CatInfo catInfo = new CatInfo();
			catInfo.offset = Long.parseLong(entries[1]);
			catInfo.length = Integer.parseInt(entries[2]);
			catInfo.df     = Long.parseLong(entries[3]);
			catInfo.ttf    = Long.parseLong(entries[4]);
			
			this.catalogMap.put(entries[0], catInfo);
		}
		catFile.close();
	}
	
	/**
	 * Writes catalog back to file from memory
	 * @throws IOException
	 */
	public void writeCatalog() throws IOException{
		String catFileName = this.getCatFileName();
		BufferedWriter catFile = new BufferedWriter(new FileWriter(catFileName, false));

		// For each term 
		for(Entry<String, CatInfo> catInfo: this.catalogMap.entrySet()){
			StringBuilder buffer = new StringBuilder();
			
			buffer.append(catInfo.getKey()).append(":");
			buffer.append(catInfo.getValue().offset).append(":");
			buffer.append(catInfo.getValue().length).append(":");
			buffer.append(catInfo.getValue().df).append(":");
			buffer.append(catInfo.getValue().ttf).append("\n");
			
			catFile.write(buffer.toString());
		}
		catFile.close();
	}
	
	/**
	 * Given a field returns the idx file name for index
	 * @return Fully qualified name of idx file
	 */
	private final String getDatFileName(){
		return this.datFilePath + "." + fieldName + ".dat.txt";
	}

	/**
	 * Given a field returns the catalog file name for index
	 * @return Fully qualified name of cat file
	 */
	private final String getCatFileName(){
		return this.getDatFileName() + ".cat.txt";
	}
	
	/**
	 * Opens a new file stream for catalog management
	 * @throws IOException 
	 */
	private void openIndexStreams() throws IOException{
		Path datFilePath = Paths.get(this.getDatFileName());
		if(!Files.exists(datFilePath))
			Files.createFile(datFilePath);
		
		this.datFileRW = FileChannel.open(datFilePath, StandardOpenOption.READ, StandardOpenOption.CREATE);
	}
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map is the map to sort
	 * @param ascOrder notifies order of sort ascending otherwise descending
	 * @return List of map entries in sorted form
	 */
	private List<Entry> getSortedList(Map<String, DocInfo> map, Boolean ascOrder){
		Map<String, DocInfo> m1 = new HashMap<String, DocInfo>();
		Map<String, DocInfo> m2 = new HashMap<String, DocInfo>();
		
		Integer i = 0;
		for(Entry<String, DocInfo> e: map.entrySet()){
			if(i<map.size()/2)
				m1.put(e.getKey(), e.getValue());
			else
				m2.put(e.getKey(), e.getValue());
		}
		return mergeSort(sortByValue(m1, ascOrder), sortByValue(m2, ascOrder));
	}
	
	/**
	 * sorts given map and returns a linked list to print results in sorted order
	 * @param map is the map to sort
	 * @param ascOrder notifies order of sort ascending otherwise descending
	 * @return List of map entries in sorted form
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Entry> sortByValue(Map map, Boolean ascOrder) {
	     List<Entry> list = new LinkedList(map.entrySet());
	     final Integer srtOrder = (ascOrder)? 1 : -1;
	     
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return srtOrder * ((Comparable) ((Map.Entry) (o1)).getValue())
	            		   .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });
	     return list;
	}
	
	/**
	 * sorts given list of entries and returns a linked list to print results in sorted order
	 * @param map is the map to sort
	 * @param ascOrder notifies order of sort ascending otherwise descending
	 * @return List of map entries in sorted form
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Entry> mergeSort(List<Entry> map1, List<Entry> map2){
		List<Entry> list = new LinkedList();
	    Integer minLen = Math.min(map1.size(), map2.size());
	    
	    while(!map1.isEmpty() && !map2.isEmpty()){
	    	Entry item1 = map1.get(0); 
	    	Entry item2 = map2.get(0);
	    	
	    	if(((Comparable)item1.getValue())
	    			.compareTo((Comparable)item2.getValue())>0){
	    		map1.remove(0);
	    		list.add(item1);
	    	}else{
	    		map2.remove(0);
	    		list.add(item2);
	    	}
	    	
	    }
	    list.addAll(map1);
	    list.addAll(map2);
		return list;
	}
	
	@Override
	public void finalize() throws Throwable{
		// Write catalog back to file
		this.datFileRW.close();
		try{
			this.datFileWr.close();
		}catch(Exception e){}
	}
	
	/**
	 * Finalizeses the object forcefully deleting any files associated with it
	 * @param deleteFiles specifies to delete index files from hard disk
	 */
	public final void finalize(Boolean deleteFiles) throws IOException, Throwable{
		//System.out.println("Finalizing catalog..." + this.datFilePath + "/" + this.fieldName);
		this.datFileRW.close();
		
		if(!deleteFiles) this.writeCatalog();
		try{
			this.datFileWr.close();
		} catch(Exception e){}

		this.deleteIndex(deleteFiles);
	}
}