/**
 * 
 */
package com.ir.homework.io;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static com.ir.homework.common.Constants.*;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * @author shabbirhussain
 * Loads data into elasticsearch index
 */
public class DataLoader {
	private String dataFilePath;
	private String dataFilePrefix;
	private Boolean enableBulkProcessing;
	private String indexName;
	private String indexType;
	
	public DataLoader(String dataFilePath, String dataFilePrefix, Boolean enableBulkProcessing, String indexName, String indexType){
		this.dataFilePath   = dataFilePath;
		this.dataFilePrefix = dataFilePrefix;
		this.enableBulkProcessing = enableBulkProcessing;
		this.indexName = indexName;
		this.indexType = indexType;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataLoader dl = new DataLoader(DATA_PATH, DATA_FILE_PREFIX, ENABLE_BULK_INSERT, INDEX_NAME, INDEX_TYPE);
		
		long start = System.nanoTime(); 
		long pushCnt =0, isrtCnt =0;
		try {
			pushCnt = dl.indexDocuments();
			isrtCnt = dl.getRecordInsertCount();
			if(pushCnt != isrtCnt)
				System.err.println("Some errors in bulk insert processing:" + pushCnt + ";" + isrtCnt);
			System.out.println("Record Count=" + pushCnt);
		} catch (Exception e1) {e1.printStackTrace();}
		
		// Segment to monitor
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
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(dataFilePrefix)) {
				System.out.println(file.getName());
				Document doc = dBuilder.parse(file);
				
				org.w3c.dom.NodeList docs = doc.getElementsByTagName("DOC");
				
				for(int i=0; i<docs.getLength(); i++, cnt++){
					IndexRequestBuilder irBuilder = getRequest(docs.item(i));
					
					//*/ Individual inserts
					if (enableBulkProcessing){
						bulkProcessor.add(irBuilder.request());
					}else {
						irBuilder.get();
					}
				}
			}
		}
		bulkProcessor.close();
		return cnt;
	}
	
	/**
	 * @return Gets count of records in the index
	 */
	private Long getRecordInsertCount(){
		Long idxCnt = client.prepareSearch(INDEX_NAME).get().getHits().getTotalHits();
		return idxCnt;
	}
	
	/**
	 * Creates XContentBuilder using given elastic search methods
	 * @param node XML node of given Document
	 * @return IndexRequestBuilder made from selected attributes
	 * @throws IOException
	 */
	private IndexRequestBuilder getRequest(org.w3c.dom.Node node) throws IOException{
		Element elem = (Element)node;
		String DOCNO = elem.getElementsByTagName("DOCNO").item(0).getTextContent().trim();
		String TEXT  = elem.getElementsByTagName("TEXT" ).item(0).getTextContent().trim();
		
		XContentBuilder builder = jsonBuilder()
			.startObject()
		        .field("DOCNO", DOCNO)
		        .field("TEXT" , TEXT)
		    .endObject();
		
		
		IndexRequestBuilder irBuilder = client.prepareIndex(indexName, indexType, DOCNO).setSource(builder);
		return irBuilder;
	}
	
}
