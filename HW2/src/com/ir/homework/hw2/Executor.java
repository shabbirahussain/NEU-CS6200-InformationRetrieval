/**
 * 
 */
package com.ir.homework.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.tokenizers.DefaultTokenizer;
import com.ir.homework.hw2.tokenizers.Tokenizer;

import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor{
	private static Set<String> stopWords = new HashSet<String>();
	private static Tokenizer tokenizer;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime(); 
		
		if(ENABLE_STOPWORD_FILTER) stopWords   = getStopWords();
		tokenizer = (new DefaultTokenizer("(\\w+(\\.?\\w+)*)"))
				.setStemming(ENABLE_STEMMING)
				.setStopWordsFilter(stopWords);
		
		
		File folder = new File(DATA_PATH);
		File[] listOfFiles = folder.listFiles();
		
		Integer cnt    = 0;
		Integer idxVer = 1;
		IndexManager idxManager = new IndexManager(INDEX_ID, idxVer, tokenizer);
		
		mergeIndices(1, 357);

		if (true) return;
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(DATA_FILE_PREFIX)) {
				System.out.println("[Info]: Loading file [" + file.getName() + "]");
				
				cnt += Executor.loadFile(idxManager, file);
				if((cnt / BATCH_SIZE)>1){
					idxManager.writeIndex();
					// Create new instance of index manager
					idxManager = new IndexManager(INDEX_ID, ++idxVer, tokenizer);
				}
			}
		}

		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		mergeIndices(1, idxVer);

		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
	public static void mergeIndices(Integer idxVerStart, Integer idxVerStop) throws Exception{
		System.out.println("[Info]: Merging files:" +  idxVerStart + "-" + idxVerStop);
		if ((idxVerStop - idxVerStart) <1) return; // do nothing
		
		Integer idxVer = idxVerStop;
		for(int i=idxVerStart; i<(idxVerStop-1); i+=2){
			System.out.println(i);
			IndexManager idxManager = new IndexManager(INDEX_ID, ++idxVer, tokenizer);
			IndexManager idx1 = new IndexManager(INDEX_ID, i  , tokenizer);
			IndexManager idx2 = new IndexManager(INDEX_ID, i+1, tokenizer);
			
			idxManager.mergeIndices(idx1, idx2);
			
			idx1.deleteIndex();
			idx2.deleteIndex();
		}
		mergeIndices(idxVerStop + 1, idxVer);
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
			dataMap.put("HEAD", head.toString());
			
			client.loadData(DOCNO, dataMap);
		}
		return i;
	}
}
