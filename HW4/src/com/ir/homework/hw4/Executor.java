package com.ir.homework.hw4;

import static com.ir.homework.hw4.Constants.*;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map.Entry;

import com.ir.homework.hw4.elasticclient.ElasticClient;
import com.ir.homework.hw4.io.ObjectStore;
import com.ir.homework.hw4.rankers.PageRanker;

public class Executor {
	private static PageRanker pr;
	
	public static void main(String[] args) throws UnknownHostException {
		PageRanker._elasticClient = new ElasticClient();
		System.out.println("Loading object...");
		pr = loadOrDefaultPR();
		
		for(int j=0;j<10;j++){
			System.out.println("Ranking pages...");
			for(int i=0;i<50;i++)
				pr.rankPages();
			
			System.out.println("Printing results...");
			List<Entry<String, Float>> topPages = pr.getTopPages();
			for(int i=0;i<5;i++)
				System.out.println(topPages.get(i));
			
			System.out.println("Saving object...");
			savePR();
		}
	}
	
	/**
	 * Tries to load last saved page rank
	 * @return PageRanker object
	 */
	private static PageRanker loadOrDefaultPR(){
		PageRanker result;
		try{
			result = (PageRanker) ObjectStore.get(PageRanker.class, OBJECTSTORE_PATH);
		}catch(Exception e){
			result = new PageRanker();
		}
		return result;
	}
	
	/**
	 * Saves the object 
	 * @return PageRanker object
	 */
	private static void savePR(){
		ObjectStore.saveObject(pr, OBJECTSTORE_PATH);
	}
}
