package com.ir.homework.hw6;

import static com.ir.homework.hw6.Constants.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.ir.homework.hw6.io.FeatLoader;
import com.ir.homework.hw6.io.ObjectStore;
import com.ir.homework.hw6.models.FeatMatrix;
/**
 * @author shabbirhussain
 *
 */
public final class DataGenerator {
	private static FeatMatrix fMat;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start = System.nanoTime(); 
		
		// Load existing feature matrix
		System.out.println("Loading object...");
		fMat = new FeatMatrix();
		try{
			fMat = (FeatMatrix) ObjectStore.get(fMat.getClass(), OBJECTSTORE_PATH);
		}catch(Exception e){}
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		
		FeatLoader fl = new FeatLoader(fMat);
		System.out.println("Loading labels...");
		fl.loadLabels(QREL_PATH);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		System.out.println("Loading features...");
		fl.loadFeatures(OUTPUT_FOLDR_PATH, DATA_FILE_PREFIX);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		System.out.println("Saving features to file...");
		PrintStream out   = new PrintStream(new File(QRES_PATH));
		PrintStream train = new PrintStream(new File(QTRN_PATH));
		PrintStream test  = new PrintStream(new File(QTST_PATH));
		PrintStream eval  = new PrintStream(new File(QEVL_PATH));
		
		fMat.printFeatMatrix(out, train, test, eval);
		
		out.close(); train.close(); test.close(); eval.close();
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		// Save feature matrix
		System.out.println("Saving object...");
		ObjectStore.saveObject(fMat, OBJECTSTORE_PATH);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
}
