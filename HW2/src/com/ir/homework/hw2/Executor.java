/**
 * 
 */
package com.ir.homework.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ir.homework.hw2.cache.CacheManager;
import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.tokenizers.DefaultTokenizer;
import com.ir.homework.hw2.tokenizers.Tokenizer;

import opennlp.tools.stemmer.PorterStemmer;

import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor{
	private static Set<String>  stopWords = new HashSet<String>();
	private static Tokenizer    tokenizer;
	private static CacheManager translator;
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		long start = System.nanoTime(); 
		
		if(ENABLE_STOPWORD_FILTER) stopWords   = getStopWords();
		tokenizer = (new DefaultTokenizer("(\\w+(\\.?\\w+)*)"))
				.setStemming(ENABLE_STEMMING)
				.setStopWordsFilter(stopWords);
		translator = loadOrCreateCache(OBJECT_STORE_PATH);
		
		
		File folder = new File(DATA_PATH);
		File[] listOfFiles = folder.listFiles();

		
		Integer cnt    = 0;
		IndexManager idxManager = getIndexManager(translator.getNextIndexID());
		
		Integer limit = 0;
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(DATA_FILE_PREFIX)) {
				System.out.println("[Info]: Loading file [" + file.getName() + "]");
				
				cnt += Executor.loadFile(idxManager, file);
				if((cnt / BATCH_SIZE)>1){
					System.out.println("\n\n \t\t ********** New Batch *************\n\n");;
					limit++;
					cnt = 0;

					//if(limit>3) break;
					// Create new instance of index manager
					idxManager.flush();
					idxManager.finalize(false);
					idxManager = getIndexManager(translator.getNextIndexID());
				}
			}
		}
		idxManager.flush();
		idxManager.finalize(false);
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		mergeIndices();
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		saveObject(translator, OBJECT_STORE_PATH);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
				
	}
	/**
	 * Performs index merge with master file
	 * @throws Throwable
	 */
	public static void mergeIndices() throws Throwable{
		Integer masterIdxId = translator.getLastStableIndexID();
		Integer indexStop   = translator.getCurrIndexID();
		
		IndexManager masterIM = getIndexManager(masterIdxId);
		for(int i=(masterIdxId+1); i<=indexStop; i++){
			System.out.println("Merging : " + i + " onto " + translator.getLastStableIndexID());
			
			masterIM = masterIM.mergeIndices(i, BATCH_SIZE, ENABLE_AUTO_CLEAN);
			
			//System.out.println("Merged index => " + translator.getLastStableIndexID() + "\n");
		}
		masterIM.finalize(false);
	}
	
	/**
	 * Gets teh index manager for the given index version
	 * @param idxVer is the version of index
	 * @return IndexManager
	 */
	private static IndexManager getIndexManager(Integer idxVer){
		return (new IndexManager(INDEX_ID, idxVer))
				.setTokenizer(tokenizer)
				.setCacheManager(translator);
	}
	
	
	
	/**
	 * Reads and returns list of stop words
	 * @return Set of stop words
	 * @throws IOException 
	 */
	public static Set<String> getStopWords() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(STOP_WORDS_FILE_PATH)));
		Set<String> result = new HashSet<String>();
		
		String line;
		while((line=br.readLine())!= null){
			line = line.trim();
			result.add(line);
		}
		br.close();
		return result;
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
	public static Integer loadFile(IndexManager client, File file) throws ParserConfigurationException, SAXException, IOException{
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
			//dataMap.put("HEAD", head.toString());
			
			client.putData(DOCNO, dataMap);
		}
		return i;
	}
	
	/**
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param storePath full path of directory where objects are stored
	 */
	private static void saveObject(Object object, String storePath){
		if(!ENABLE_PERSISTENT_CACHE) return;
		System.out.println("\n\nSaving cache for future use...");
		
		String fullFilePath = storePath + object.getClass().getName() + ".ser";
		
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fullFilePath));
			oos.writeObject(object);
			oos.close();
		} catch (IOException e) {e.printStackTrace();}
		return;
	}
	
	/**
	 * 
	 * @param c class of object to be loaded
	 * @param storePath full path of directory where objects are stored
	 * @return Uncasted object of given class fetched from store
	 */
	private static Object loadObject(Class c, String storePath){
		String fullFilePath = storePath + c.getName() + ".ser";
		Object result;
		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullFilePath));
			result = ois.readObject();
			ois.close();
			
			return result;
		}catch(ClassNotFoundException | IOException e){}
		return null;
	}
	
	/**
	 * Loads or creates a elasticClient
	 * @param c class of object to be loaded
	 * @param storePath full path of directory where objects are stored
	 * @return Uncasted object of given class fetched from store
	 */
	private static CacheManager loadOrCreateCache(String storePath){
		CacheManager result = null;
		if(ENABLE_PERSISTENT_CACHE){
			System.out.println("Loading cache from: " + storePath);
			result = (CacheManager) loadObject(CacheManager.class, storePath);	
		}
		if(result != null) return result;
		
		result = new CacheManager();
		return result;
	}
}
