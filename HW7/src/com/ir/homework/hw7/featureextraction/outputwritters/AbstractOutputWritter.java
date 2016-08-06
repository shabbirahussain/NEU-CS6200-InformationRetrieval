package com.ir.homework.hw7.featureextraction.outputwritters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AbstractOutputWritter implements OutputWritter {
	protected File outFile;
	
	/**
	 * Creates a out file with given name
	 * @param outFileName is the fully qualified name of the out file
	 * @throws IOException 
	 */
	public AbstractOutputWritter(String outFileName, String extension) throws IOException {
		this.outFile = new File(outFileName + extension);
		Files.createDirectories(Paths.get(outFile.getParent())); 
	}
}
