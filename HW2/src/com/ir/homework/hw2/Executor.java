/**
 * 
 */
package com.ir.homework.hw2;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.ir.homework.hw2.io.FileLoader;
import com.ir.homework.hw2.io.Saveable;
import com.ir.homework.hw2.metainfo.MetaInfoController;
import com.ir.homework.hw2.services.IndexBatchLoader;
import com.ir.homework.hw2.services.IndexBatchMerger;
import com.ir.homework.hw2.services.ShutdownHandler;

import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor{
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		MetaInfoController metaSynchronizer = new MetaInfoController(INDEX_ID);
		List<Saveable> saveableObjects = new LinkedList<Saveable>();
		saveableObjects.add(metaSynchronizer);
		
		Runtime.getRuntime()
			.addShutdownHook(new Thread(new ShutdownHandler(saveableObjects)));
		
		Thread loader = new Thread(new IndexBatchLoader(metaSynchronizer));
		Thread merger = new Thread(new IndexBatchMerger(metaSynchronizer));
		
		loader.start();
		merger.start();	
	}
	
	private static void resetIndices(){
		FileLoader.resetLoadedFiles();
		
		
		String indicesPath = INDEX_PATH + File.separator + INDEX_ID;
		File folder = new File(indicesPath);
		File[] files = folder.listFiles();
		for(File file : files){
			file.delete();
		}
	}
}
