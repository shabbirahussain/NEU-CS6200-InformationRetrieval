package com.ir.homework.hw4.preprocessing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ir.homework.hw4.elasticclient.ElasticClient;
import static com.ir.homework.hw4.Constants.*;

public class FileLoader {
	private static final String SPLITTER = " ";
	private static ElasticClient elasticClient;
	
	public static void main(String[] args) throws IOException {
		elasticClient = new ElasticClient();
		(new FileLoader()).loadFile(LINKS_PATH);
	}
	
	/**
	 * Loads links file to elastic search
	 * @param filePath
	 * @throws IOException 
	 */
	private void loadFile(String filePath) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		String line=null;
		while((line=br.readLine())!=null){
			String[] items = line.split(SPLITTER);
			if(items.length == 1)
				elasticClient.loadLinks(null, items[0]);
			
			for(int i=1;i<items.length; i++){
				Integer numActions = elasticClient.loadLinks(items[i], items[0]);
				if(numActions > MAX_BUFFER_SIZE){
					System.out.println("Flushing...");
					elasticClient.flush();
				}
			}
			elasticClient.flush();
		}
		br.close();
		
	}
	
}
