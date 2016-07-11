package com.ir.homework.hw4;

import static com.ir.homework.hw4.Constants.*;

import java.net.UnknownHostException;

import com.ir.homework.hw4.io.ObjectStore;
import com.ir.homework.hw4.rankers.HITSRanker;
import com.ir.homework.hw4.rankers.PageRanker;
import com.ir.homework.hw4.rankers.Ranker;

public class Executor {
	private static Ranker ranker;
	
	public static void main(String[] args) throws UnknownHostException{
		System.out.println("Loading object...");
		
		/////////////////////////////////////
		ranker = loadOrDefaultPR();
		//ranker = loadOrDefaultHITS();
		/////////////////////////////////////
		
		calcPageRank();
		
		ranker.printTopPages(10);
		saveObject(ranker);
	}
	
	/**
	 * Calculates the page rank
	 * @throws UnknownHostException
	 */
	public static void calcPageRank()	throws UnknownHostException {
		for(int j=1;j<=(NUM_OF_ITERATIONS/PEEK_INTERVAL);j++){
			System.out.println("Ranking pages...");
			for(int i=1;i<=PEEK_INTERVAL;i++){
				ranker.rankPages();
			}
			if(ranker.isConverged(4)){
				System.out.println("Convergence reached after " + j*PEEK_INTERVAL + " iterations.");
				return;
			}
			System.out.println("Result peek:");
			ranker.printTopPages(5);
			
			// Save intermediate results
			saveObject(ranker);
		}
	}
	
	/**
	 * Tries to load last saved page rank
	 * @return PageRanker object
	 * @throws UnknownHostException 
	 */
	private static Ranker loadOrDefaultPR() throws UnknownHostException{
		Ranker result;
		try{
			result = (PageRanker) ObjectStore.get(PageRanker.class, OBJECTSTORE_PATH);
		}catch(Exception e){
			result = new PageRanker();
		}
		return result;
	}
	
	/**
	 * Tries to load last saved page rank
	 * @return PageRanker object
	 * @throws UnknownHostException 
	 */
	private static Ranker loadOrDefaultHITS() throws UnknownHostException{
		Ranker result;
		try{
			result = (HITSRanker) ObjectStore.get(HITSRanker.class, OBJECTSTORE_PATH);
		}catch(Exception e){
			result = new HITSRanker();
		}
		return result;
	}
	
	/**
	 * Saves the object 
	 * @return PageRanker object
	 */
	private static void saveObject(Object object){
		System.out.println("Saving object...");
		ObjectStore.saveObject(object, OBJECTSTORE_PATH);
	}
}
