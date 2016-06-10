/**
 * 
 */
package com.ir.homework.hw2.services;

import static com.ir.homework.hw2.Constants.ENABLE_STEMMING;
import static com.ir.homework.hw2.Constants.ENABLE_STOPWORD_FILTER;
import static com.ir.homework.hw2.Constants.INDEX_ID;
import static com.ir.homework.hw2.Constants.STOP_WORDS_FILE_PATH;
import static com.ir.homework.hw2.Constants.TOKENIZER_REGEXP;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.io.FileLoader;
import com.ir.homework.hw2.io.StopWordReader;
import com.ir.homework.hw2.metainfo.MetaInfoController;
import com.ir.homework.hw2.tokenizers.DefaultTokenizer;
import com.ir.homework.hw2.tokenizers.Tokenizer;

/**
 * Batch loading processor for indexes
 * @author shabbirhussain
 */
public class IndexBatchLoader implements Runnable {
	private static final Long   REFRESH_INTERVAL = 1L * 1000L;
	private MetaInfoController  metaSynchronizer;
	private Tokenizer           tokenizer;
	
	/**
	 * Default constructor
	 * @param metaSynchronizer is the synchronizer object used for interprocess communication
	 */
	public IndexBatchLoader(MetaInfoController metaSynchronizer){
		this.metaSynchronizer = metaSynchronizer;
		
		StopWordReader sr = new StopWordReader(STOP_WORDS_FILE_PATH);
		Set<String> stopWords = new HashSet<String>();;
		if(ENABLE_STOPWORD_FILTER)
			try {
				stopWords   = sr.getStopWords();
			} catch (IOException e) {}
		

		tokenizer = (new DefaultTokenizer(TOKENIZER_REGEXP))
				.setStemming(ENABLE_STEMMING)
				.setStopWordsFilter(stopWords);
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new IndexBatchLoader(new MetaInfoController(INDEX_ID))).run();
	}
	
	@Override
	public void run() {
		FileLoader fileLader = new FileLoader(tokenizer);
		try {
			while(true){
				System.out.println("Starting new batch load...");
				
				Integer idxVer = 0;
				IndexManager indexManager = null;
				if(fileLader.newFilesAvailable()){
					idxVer = metaSynchronizer.getNextIndexID();
					indexManager = metaSynchronizer.getIndexManager(idxVer);
					
					fileLader.loadFiles(indexManager);

					metaSynchronizer.setUsable(idxVer);
				}
				
				System.out.println("Batch load sleeping...");
				Thread.sleep(REFRESH_INTERVAL);
			}
		} catch (Throwable e) {e.printStackTrace();}
	}
}
