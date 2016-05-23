/**
 * 
 */
package com.ir.homework.hw1.util;


import static com.ir.homework.hw1.Constants.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author shabbirhussain
 * Loads data into elasticsearch index
 */
public class VocabCounter {
	private String dataFilePath;
	private String dataFilePrefix;
	
	public VocabCounter(String dataFilePath, String dataFilePrefix, Boolean enableBulkProcessing, String indexName, String indexType){
		this.dataFilePath   = dataFilePath;
		this.dataFilePrefix = dataFilePrefix;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VocabCounter dl = new VocabCounter(DATA_PATH, DATA_FILE_PREFIX, ENABLE_BULK_INSERT, INDEX_NAME, INDEX_TYPE);
		
		long start = System.nanoTime(); 
		
		try {
			dl.indexDocuments();
			
		} catch (Exception e1) {e1.printStackTrace();}
		
		// Segment monitor
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}
	
	/**
	 * Parses the document into XML doc
	 * @return number of records sent for insertion
	 * @throws ParserConfigurationException
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private Long indexDocuments() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		File folder = new File(dataFilePath);
		File[] listOfFiles = folder.listFiles();
		
		long cnt = 0;
		
		Set<String> uniqueWords = new HashSet<String>();
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(dataFilePrefix)) {
				System.out.println(file.getName());
				Document doc = dBuilder.parse(file);
				
				org.w3c.dom.NodeList docs = doc.getElementsByTagName("doc");
				for(int i=0; i<docs.getLength(); i++){
					String words[] = ((Element) docs.item(i)).getElementsByTagName("text").item(0).getTextContent().split("[^a-z]");
					uniqueWords.addAll(Arrays.asList(words));
				}
			}
		}
		Integer result = uniqueWords.size();
		
		// Create Buffered Readers and Writers for copying		
		BufferedReader brd = new BufferedReader(new FileReader("/Users/shabbirhussain/Data/IRData/AP_DATA/stoplist.txt"));
		String line = null;
		while((line=brd.readLine())!=null){
			if(uniqueWords.contains(line.trim()))
				result--;
		}
		brd.close();
		
		System.out.println("Unique words="+ uniqueWords.size() + "\t -stop=" + result);
		return cnt;
	}
	
	
}
