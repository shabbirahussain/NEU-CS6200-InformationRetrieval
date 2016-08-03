/**
 * 
 */
package com.ir.homework.hw7.dataloader;

import static com.ir.homework.hw7.dataloader.Constants.*;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.ir.homework.hw7.dataloader.io.MInputDataDAO;
import com.ir.homework.hw7.dataloader.io.MQrelDAO;
import com.ir.homework.hw7.dataloader.models.MInputData;
import com.ir.homework.hw7.dataloader.models.MQrel;
import com.ir.homework.hw7.dataloader.parsers.MimeFileParser;
import com.ir.homework.hw7.dataloader.parsers.Parser;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static long start;
	private static Parser     _parser;
	private static MInputData _mInputData;
	private static MQrel      _mQrel;
	
	/**
	 * @param args 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
		start = System.nanoTime(); 
		
		// Initialize

		_parser = new MimeFileParser();

		_mQrel      = MQrelDAO.getModel(QREL_PATH).get("0");
		_mInputData = MInputDataDAO.getModel(_parser,
				CLUSTER_NAME, 
				HOST, 
				PORT, 
				INDEX_NAME, 
				INDEX_TYPE, 
				MAX_RESULTS);

		// Start load
		startLoad();
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	

	/**
	 * Parses the document and loads it into the elasticsearch
	 * @return number of records sent for insertion
	 * @throws Exception
	 */
	public static Long startLoad() throws Exception{
		File folder = new File(DATA_PATH);
		File[] listOfFiles = folder.listFiles();
		
		Long cnt = 0L;
		Long errCnt = 0L;
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().startsWith(DATA_FILE_PREFIX)) {
				String id = file.getName();
				System.out.println("[Info]: Loading file [" + id + "]");
				String message = FileUtils.readFileToString(file);
		
				// Parse the data
				Map<String, Object> content;
				try{
					content = _parser.parse(message);
					content.put("Label", _mQrel.get(id));
					
					// Load the data
					_mInputData.storeData(file.getName(), content);
				}catch(Exception e){
					errCnt++;
					e.printStackTrace();
				}
			}
		}
		System.out.println("Total Errors = " + errCnt);
		_mInputData.flush();
		return cnt;
	}
}
