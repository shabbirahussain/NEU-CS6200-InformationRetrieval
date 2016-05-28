/**
 * 
 */
package com.ir.homework.preprocessing;

import org.elasticsearch.common.xcontent.XContentBuilder;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.ir.homework.hw1.elasticclient.ElasticClient;

/**
 * @author shabbirhussain
 * Loads data into elasticsearch index
 */
public class FileLoader {
	private String dataFilePath;
	private String dataFilePrefix;
	
	private ElasticClient elasticClient;
	
	/**
	 * Default constructor
	 * @param elasticClient client to use for loading
	 * @param dataFilePath full path of the data file to load
	 * @param dataFilePrefix prefix to use for filtering data files
	 */
	public FileLoader(ElasticClient elasticClient, String dataFilePath, String dataFilePrefix){
		this.elasticClient  = elasticClient;
		this.dataFilePath   = dataFilePath;
		this.dataFilePrefix = dataFilePrefix;
	}
	
	/**
	 * Parses the document into XML doc
	 * @return number of records sent for insertion
	 * @throws ParserConfigurationException
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Long startLoad() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		File folder = new File(dataFilePath);
		File[] listOfFiles = folder.listFiles();
		
		long cnt = 0;
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(dataFilePrefix)) {
				System.out.println("[Info]: Loading file [" + file.getName() + "]");
				Document doc = dBuilder.parse(file);
				
				org.w3c.dom.NodeList docs = doc.getElementsByTagName("doc");
				
				for(int i=0; i<docs.getLength(); i++, cnt++){
					Element elem = (Element) docs.item(i);
					String DOCNO = elem.getElementsByTagName("docno")
							.item(0)
							.getTextContent()
							.trim()
							.toUpperCase();
					
					org.w3c.dom.NodeList texts = elem.getElementsByTagName("text");
					StringBuilder TEXT = new StringBuilder();
					for(int j=0; j<texts.getLength(); j++){
						TEXT = TEXT.append(" "); 
						TEXT = TEXT.append(elem.getElementsByTagName("text" )
								.item(j)
								.getTextContent()
								.trim());
					}
					
					XContentBuilder builder = jsonBuilder()
							.startObject()
						        .field("DOCNO", DOCNO)
						        .field("TEXT" , TEXT.toString())
						    .endObject();
					
					elasticClient.loadData(DOCNO, builder);
				
				}
			}
		}
		elasticClient.commit();
		return cnt;
	}
}
