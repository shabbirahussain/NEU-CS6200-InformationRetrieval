package com.ir.homework.hw7.dataloader.io;

import static com.ir.homework.hw7.dataloader.Constants.DATA_FILE_PREFIX;
import static com.ir.homework.hw7.dataloader.Constants.DATA_PATH;

import java.io.File;
import java.util.Map;

import com.ir.homework.hw7.dataloader.parsers.Parser;
import com.ir.homework.hw7.elasticclient.ElasticClient;

public final class FileLoader {
	Parser parser;
	ElasticClient elasticClient;
	
	public FileLoader(Parser parser, ElasticClient elasticClient) {
		this.parser = parser;
		this.elasticClient = elasticClient;
	}
	/**
	 * Parses the document and loads it into the elasticsearch
	 * @param qID is the query id for which documents has to be loaded
	 * @return number of records sent for insertion
	 * @throws Exception
	 */
	public Long startLoad(String qID) throws Exception{
		File folder = new File(DATA_PATH);
		File[] listOfFiles = folder.listFiles();
		
		Long cnt = 0L;
		Long errCnt = 0L;
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(DATA_FILE_PREFIX)) {
				System.out.println("[Info]: Loading file [" + file.getName() + "]");
		
				// Parse the data
				try{
					Map<String, Object> content = parser.parseFile(qID, file.getAbsolutePath());
					
					// Load the data
					elasticClient.loadData(file.getName(), content);
				}catch(Exception e){
					errCnt++;
					e.printStackTrace();
				}
			}
		}
		System.out.println("Total Errors = " + errCnt);
		elasticClient.flush();
		return cnt;
	}
}
