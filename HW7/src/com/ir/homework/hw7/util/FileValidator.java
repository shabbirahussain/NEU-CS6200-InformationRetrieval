/**
 * 
 */
package com.ir.homework.hw7.util;

import static com.ir.homework.hw7.Constants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author shabbirhussain
 * Pre processes the file to make them normalized and usable in XML processing
 */
public final class FileValidator {
	/**
	 * Validates files files to make them XML compatible
	 * @return error count while validating converted documents
	 * @throws IOException
	 */
	public static Integer validateFiles(String dataSrcFilePath) throws IOException{
		
		File   folder      = new File(dataSrcFilePath);
		File[] listOfFiles = folder.listFiles();
		
		int errCnt=0;
		for (File file : listOfFiles) {
			// If valid file type pre process it
			if (file.isFile() && file.getName().startsWith("ap")){
				System.out.println("[Info]: Validatig file [" + file.getName() + "]");
				
				try{
					validateFile(file);
				}catch(Exception e){e.printStackTrace();}
			}
		}
		//System.out.println("[Info]: Number of file processed: " + fileCnt);
		return errCnt;
	}
	
	/**
	 * Makes files compatible for XML reading
	 * @return Updated file post processing
	 * @throws IOException 
	 */
	private static void validateFile(File file) throws IOException {
		// Create Buffered Readers and Writers for copying		
		BufferedReader brd = new BufferedReader(new FileReader(file));
				
		// Copy actual data to new file. Cleaning them as well
		String line;
		Boolean flag = false;
	    while ((line = brd.readLine()) != null) {
	    	
	    	if(line.contains("</TEXT>") ){
	    		flag = true;
	    	}
	    	if(line.contains("</DOC>")){
	    		flag = false;
	    	}
	    	if(flag && !line.contains("<TEXT>")){
	    		System.out.println(line);
	    		System.out.println("");
	    	}
	    }
		brd.close();
	}

}
