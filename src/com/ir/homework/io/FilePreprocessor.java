/**
 * 
 */
package com.ir.homework.io;

import static com.ir.homework.common.Constants.*;

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
public final class FilePreprocessor {
	private String dataSrcFilePath;
	private String dataDstFilePath;
	private String dataFilePrefix;
	
	// Constants class specific
	private static final Boolean DEBUG = false;

	private static Path tempFilePath = null;
	
	public FilePreprocessor(String dataSrcFilePath, String dataDstFilePath, String dataFilePrefix){
		this.dataSrcFilePath = dataSrcFilePath;
		this.dataDstFilePath = dataDstFilePath;
		this.dataFilePrefix  = dataFilePrefix;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start = System.nanoTime(); 
		FilePreprocessor fp = new FilePreprocessor(PRE_PROCESS_SRC_PATH, PRE_PROCESS_DST_PATH, DATA_FILE_PREFIX);
		
		System.out.println("Pre processing files...");
		System.out.println("[Info]: [Source = " + fp.dataSrcFilePath +"]");
		System.out.println("[Info]: [Destination = " + fp.dataDstFilePath +"]");
		
		Integer errCnt = fp.preProcessFiles();
		
		System.out.println("[Info]: Number of Errors: "         + errCnt);
		
		double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
		System.out.println("Time Required=" + elapsedTimeInSec);
	}
	
	public Integer preProcessFiles() throws IOException{
		tempFilePath = Files.createTempFile("com.ir.preprocessor.",".temp");
		
		File   folder      = new File(dataSrcFilePath);
		File[] listOfFiles = folder.listFiles();
		
		int errCnt=0, fileCnt=0;
		for (File file : listOfFiles) {
			// If valid file type pre process it
			if (file.isFile() && file.getName().startsWith(dataFilePrefix)){
				fileCnt++;
				//System.out.println("[Info]: Processing file [" + file.getName() + "]");
				
				try{
					File processedFile = preProcessFile(file, dataDstFilePath);
					validateFileXML(processedFile);
					
				}catch(Exception e){
					errCnt++;
					if(DEBUG) e.printStackTrace(System.err);
				}
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
	private File preProcessFile(File file, String destPath) throws IOException {
		// Move source file to backup loc
		Files.copy(file.toPath(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
		
		// Create new file in-place of destination
		File target = new File (destPath + "/" + file.getName());
				
		// Create Buffered Readers and Writers for copying		
		BufferedReader brd = new BufferedReader(new FileReader(tempFilePath.toString()));
		BufferedWriter bwr = new BufferedWriter(new FileWriter(target));
				
		// Write wrapper root node to the file		
		bwr.write("<DOCS>\n");
				
		// Copy actual data to new file. Cleaning them as well
		String line;
	    while ((line = brd.readLine()) != null) {
	    	line = cleanupLines(line);
	        bwr.write(line);
	    }
		brd.close();
	    
		// Write wrapper root node end to the file
	    bwr.write("</DOCS>\n");
	    
		bwr.close();
		Files.delete(tempFilePath);
		
		return target;
	}
	
	/**
	 * Parses the XML document to validate it
	 * @param file input file to validate
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private void validateFileXML(File file) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		dBuilder.parse(file);
	}
	
	/**
	 * Cleans the lines to make them compatible for XML by purifying HTML Entities into proper format
	 * @param line input string to be processed
	 * @return
	 */
	private String cleanupLines(String line){
		line = line.replaceAll("&amp;", "&");
		line = line.replaceAll("&", "&amp;");
		
		return line;
	}

}
