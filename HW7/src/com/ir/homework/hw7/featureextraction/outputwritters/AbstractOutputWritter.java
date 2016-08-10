package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public abstract class AbstractOutputWritter implements OutputWritter {
	private static DecimalFormat format = new DecimalFormat("##.00%");
	
	protected File outFile;
	private Double rowCount, doneCount;
	private Long time;
	
	/**
	 * Creates a out file with given name
	 * @param outFileName is the fully qualified name of the out file
	 * @throws IOException 
	 */
	public AbstractOutputWritter(String outFileName, String extension) throws IOException {
		this.outFile = new File(outFileName + extension);
		Files.createDirectories(Paths.get(outFile.getParent())); 
		this.time = System.currentTimeMillis();
		this.doneCount = 0.0;
		this.rowCount  = 0.0;
	}
	
	/**
	 * Adds row in the preprocess
	 */
	protected void addRow(){
		this.rowCount++;
	}
	
	/**
	 * Shows the status if every n sec
	 * @param n is the number of milliseconds to print status
	 */
	protected void showStatus(Long n){
		this.doneCount++;
		if((System.currentTimeMillis() - this.time)<n) return;
		
		System.out.println("\t" + format.format(doneCount/rowCount));
		this.time = System.currentTimeMillis();
	}
}
