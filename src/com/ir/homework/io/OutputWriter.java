package com.ir.homework.io;

import static com.ir.homework.hw1.Constants.*;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public final class OutputWriter {
	private String oPath;
	private BufferedWriter bw;
	
	public static class OutputRecord{
		public String  queryNo;
		public String  docNo;
		public Long    rank;
		public Float  score;
		
		
		public OutputRecord(String queryNo, String docNo, Long i, Float float1){
			this.queryNo = queryNo;
			this.docNo   = docNo;
			this.rank    = i;
			this.score   = float1;
		}
	};
	
	/**
	 * Creates instance of OutputWriter to write to specified path
	 * @param oPath
	 */
	public OutputWriter(String oPath){
		this.oPath = oPath;
	}
	
	/**
	 * Opens the writer handler
	 * @throws FileNotFoundException 
	 */
	public void open() throws FileNotFoundException{
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oPath)));
	}
	
	/**
	 * Closes the output stream and flush the data to file
	 * @throws IOException 
	 */
	public void close() throws IOException{
		bw.close();
	}
	
	/**
	 * Writes record to the file
	 * @param record
	 * @throws IOException
	 */
	public void writeOutput(OutputRecord record) throws IOException{
		bw.write(record.queryNo + " ");
		bw.write("Q0 ");
		bw.write(record.docNo + " ");
		bw.write(record.rank + " ");
		bw.write(record.score + " ");
		bw.write("Exp");
		bw.newLine();
	}
	
	/**
	 * Main function for testing only
	 * @param args
	 */
	public static void main(String[] args) {
		OutputWriter or=new OutputWriter(OUTPUT_FILE_PATH);
		try {
			or.open();
			
			or.writeOutput(new OutputRecord("13", "test-doc001", 1L, (float) 0.245));
			
			or.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
