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
import com.ir.homework.hw2.metainfo.MetaInfoControllers;
import com.ir.homework.hw2.tokenizers.DefaultTokenizer;
import com.ir.homework.hw2.tokenizers.Tokenizer;

/**
 * Batch loading processor for indexes
 * @author shabbirhussain
 */
public class IndexBatchLoader implements Runnable {
	private static Tokenizer  tokenizer;
	private static final Long REFRESH_INTERVAL = 1L * 1000L;

	static{
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
		(new IndexBatchLoader()).run();
	}
	
	@Override
	public void run() {
		FileLoader fileLader = new FileLoader();
		try {
			while(true){
				System.out.println("Starting new batch load...");
				
				Integer idxVer = MetaInfoControllers.getNextIndexID(INDEX_ID);
				fileLader.loadFiles(getIndexManager(idxVer));
				MetaInfoControllers.addUsableIndex(INDEX_ID, idxVer);
				
				System.out.println("Batch load sleeping...");
				Thread.sleep(REFRESH_INTERVAL);
			}
		} catch (Throwable e) {e.printStackTrace();}
	}
	

	/**
	 * Gets the index manager for the given index version
	 * @param idxVer is the version of index
	 * @return IndexManager
	 */
	private static IndexManager getIndexManager(Integer idxVer){
		return (new IndexManager(INDEX_ID, idxVer))
				.setTokenizer(tokenizer);
	}
}
