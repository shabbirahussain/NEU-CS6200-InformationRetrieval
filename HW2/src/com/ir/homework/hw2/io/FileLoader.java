/**
 * 
 */
package com.ir.homework.hw2.io;

import static com.ir.homework.hw2.Constants.BATCH_SIZE;
import static com.ir.homework.hw2.Constants.DATA_FILE_PREFIX;
import static com.ir.homework.hw2.Constants.DATA_PATH;
import static com.ir.homework.hw2.Constants.DONE_FILE_SUFFIX;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.tokenizers.Tokenizer;

/**
 * Loads the files into custom inverted indices
 * @author shabbirhussain
 *
 */
public class FileLoader {
	private Tokenizer tokenizer;
	
	public FileLoader(Tokenizer tokenizer){
		this.tokenizer = tokenizer;
	}
	
	/**
	 * @return True is new files are available for loading
	 */
	public Boolean newFilesAvailable(){
		return !getFilesForLoading().isEmpty();
	}
	
	/**
	 * Loads files to client provided
	 * @param idxManager is the index manager client to which data has to be loaded
	 * @throws Throwable 
	 */
	public Integer loadFiles(IndexManager idxManager) throws Throwable {
		System.out.println("\n\n \t\t ********** New Batch *************\n\n");;
	
		List<File> listOfFiles = getFilesForLoading();
		
		Integer cnt    = 0;
		for (File file : listOfFiles) {
			System.out.println("[Info]: Loading file [" + file.getName() + "]");
				
			cnt += loadFile(idxManager, file);
			file.renameTo(new File(file.getAbsolutePath() + DONE_FILE_SUFFIX));
			
			if((cnt / BATCH_SIZE)>1) break;
		}
		idxManager.flush();
		idxManager.finalize(false);
		return cnt;
	}
	/**
	 * Parses the document into XML doc
	 * @param client is the IndexManager client used to load data
	 * @param file is the file to load
	 * @return Number of documents in a file
	 * @throws ParserConfigurationException
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private Integer loadFile(IndexManager client, File file) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		Document doc = dBuilder.parse(file);
		org.w3c.dom.NodeList docs = doc.getElementsByTagName("doc");
		
		Integer i;
		for(i=0; i<docs.getLength(); i++){
			Map<String, String> dataMap = new HashMap<String, String>();
			
			Element elem = (Element) docs.item(i);
			String DOCNO = elem.getElementsByTagName("docno")
					.item(0)
					.getTextContent()
					.trim()
					.toUpperCase();
			
			org.w3c.dom.NodeList texts = elem.getElementsByTagName("text");
			StringBuilder text = new StringBuilder();
			for(int j=0; j<texts.getLength(); j++){
				text = text.append(" "); 
				text = text.append(elem.getElementsByTagName("text" )
						.item(j)
						.getTextContent()
						.trim());
			}
			dataMap.put("TEXT", text.toString());
			
			org.w3c.dom.NodeList heads = elem.getElementsByTagName("head");
			StringBuilder head = new StringBuilder();
			for(int j=0; j<heads.getLength(); j++){
				head = head.append(" "); 
				head = head.append(elem.getElementsByTagName("head" )
						.item(j)
						.getTextContent()
						.trim());
			}
			dataMap.put("HEAD", head.toString());
			
			client.putData(DOCNO, dataMap, tokenizer);
		}
		return i;
	}
	
	/**
	 * @return List of files ready to be loaded
	 */
	private List<File> getFilesForLoading(){
		List<File> result = new LinkedList<File>();
		File folder = new File(DATA_PATH);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()
					&& file.getName().startsWith(DATA_FILE_PREFIX)
					&& !file.getName().endsWith(DONE_FILE_SUFFIX)) 
				result.add(file);
		}
		return result;
	}
	
	/**
	 * For debugging only
	 */
	public static void resetLoadedFiles(){
		File folder = new File(DATA_PATH);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()
					&& file.getName().startsWith(DATA_FILE_PREFIX)
					&& file.getName().endsWith(DONE_FILE_SUFFIX)) {
				String newFileName = file.getAbsolutePath()
						.subSequence(0, 
								file.getAbsolutePath().length() - DONE_FILE_SUFFIX.length())
						.toString();
				
				
				file.renameTo(new File(newFileName));
			}
		}
	}
}
