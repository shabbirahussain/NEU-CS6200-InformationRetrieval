package com.ir.homework.hw2.indexers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import java.util.Map.Entry;

public class CatalogManager implements Serializable{
	private String datFilePath;
	private String fieldName;
	
	public Map<String, CatInfo> catalogMap;
	
	public FileChannel datFileRW;
	public FileChannel catFileRW;
	
	public class DocInfo {
		public List<Long> docPos;
		
		public DocInfo(){
			this.docPos = new LinkedList<Long>();
		}
	}

	/**
	 * Stores catalog info for a term
	 * @author shabbirhussain
	 */
	private class CatInfo{
		Long    offset;
		Integer length;
	}
	
	/**
	 * Creates a field object with input and output streams connected to data files and catalog. It uses the myName parameter to automatically decide which index file it will be bound to.
	 * @param myName is the name of field or instance 
	 * @param datFilePath is the base path of the index files
	 * @throws IOException 
	 */
	public CatalogManager(String myName, String  datFilePath) throws IOException{
		this.datFilePath = datFilePath;
		this.fieldName   = myName;

		this.loadCatMap();
		this.openIndexStreams();
	}
	
	/**
	 * Reads the document information from index for the term
	 * @param term is the given term to search for
	 * @return Map of document and information stored in index
	 * @throws Exception is thrown for IOException or ArrayIndexOutOfBounds when file underlying is corrupted
	 */
	public Map<String, DocInfo> readEntry(String term) throws Exception{
		CatInfo catInfo = this.catalogMap.get(term);
		if(catInfo == null) return (new HashMap<String, DocInfo>());
		
		Map<String, DocInfo> result = new HashMap<String, DocInfo>();
		
		// Read from file 
		Long    position = catInfo.offset;
		Integer capacity = catInfo.length;
				
		ByteBuffer dst = ByteBuffer.allocate(capacity);
		datFileRW.read(dst, position);
		
		String buffer   = new String(dst.array());
		String tokens[] = buffer.split(":");
		for(int i=0; i<tokens.length; i+=2){
			String key = tokens[0];
			DocInfo value = new DocInfo(); 
			
			String tokens1[] = tokens[i+1].split(";");
			for(int j=0; j<tokens1.length; j++){
				value.docPos.add(Long.parseLong(tokens1[j]));
			}
			result.put(key, value);
		}
		return result;
	}
	
	/**
	 * Writes an entry to index file and simultaneously creates a catalog of it
	 * @param term is the term to index
	 * @param is the doc information map
	 * @throws Exception is thrown in case of term requested to be written is already present in file or an IOException has occurred
	 */
	public void writeEntry(String term, Map<String, DocInfo> docInfoMap) throws Exception{
		// Raises an exception for overwriting an existing term in catalog
		if(this.catalogMap.containsKey(term)) 
			throw (new Exception("Cannot overrite an existing entry for term: " + term));
		
		Long position = datFileRW.size();
		// convert data to byte buffer
		StringBuilder buffer = new StringBuilder();
		for(Entry<String, DocInfo> docInfo : docInfoMap.entrySet()){
			buffer.append(docInfo.getKey()).append(":[");
			for(Long pos : docInfo.getValue().docPos){
				buffer.append(pos.toString()).append(";");
			}
			buffer.append("]");
		}
		ByteBuffer src = ByteBuffer.wrap(buffer.toString().getBytes());
		
		// write data to index file
		datFileRW.write(src, position);

		CatInfo catInfo = new CatInfo();
		catInfo.offset  = position;
		catInfo.length  = ((Long)(position - datFileRW.size())).intValue();
		
		this.catalogMap.put(term, catInfo);
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
	
	// ------------------------------------------------------------

	/**
	 * Loads the catalog file into memory
	 * @return Map of field vs term vs position
	 * @throws IOException 
	 */
	private void loadCatMap() throws IOException{
		String catFileName = this.getCatFileName();
		BufferedReader catFile = new BufferedReader(new InputStreamReader(new FileInputStream(catFileName)));
		
		this.catalogMap = new HashMap<String, CatInfo>();
		
		String line;
		while((line=catFile.readLine())!=null){
			String[] entries = line.split(":");
			
			CatInfo catInfo = new CatInfo();
			catInfo.offset = Long.parseLong(entries[1]);
			catInfo.length = Integer.parseInt(entries[2]);
			
			this.catalogMap.put(entries[0], catInfo);
		}
		catFile.close();
	}
	
	/**
	 * Writes catalog back to file from memory
	 * @throws IOException
	 */
	private void writeCatalog() throws IOException{
		String catFileName = this.getCatFileName();
		BufferedWriter catFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(catFileName)));

		// For each term 
		for(Entry<String, CatInfo> catInfo: this.catalogMap.entrySet()){
			StringBuilder buffer = new StringBuilder();
			
			buffer.append(catInfo.getKey()).append(":");
			buffer.append(catInfo.getValue().offset).append(":");
			buffer.append(catInfo.getValue().length).append("\n");
			
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
		this.datFileRW = FileChannel.open(datFilePath, StandardOpenOption.APPEND, StandardOpenOption.READ);
	}
	
	@Override
	public void finalize() throws Throwable{
		this.datFileRW.close();
		
		// Write catalog back to file
		this.writeCatalog();
		
		super.finalize();
	}
}