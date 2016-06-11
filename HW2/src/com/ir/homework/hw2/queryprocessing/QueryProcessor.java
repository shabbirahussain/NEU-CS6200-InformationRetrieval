package com.ir.homework.hw2.queryprocessing;

import static com.ir.homework.hw2.Constants.INDEX_ID;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.indexers.CatalogManager.CatInfo;
import com.ir.homework.hw2.indexers.CatalogManager.DocInfo;
import com.ir.homework.hw2.indexers.CatalogManager.TermInfo;
import com.ir.homework.hw2.metainfo.MetaInfoController;

public class QueryProcessor {
	private MetaInfoController metaSynchronizer;
	private String fieldName;
	
	/**
	 * Default constructor
	 */
	public QueryProcessor(String indexID, String fieldName){
		this.metaSynchronizer = new MetaInfoController(INDEX_ID);
		this.fieldName = fieldName;
	}
	
	/**
	 * Only for debugging
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
		QueryProcessor qp = new QueryProcessor(INDEX_ID, "HEAD");
		System.out.println(qp.getTermStatistics("cancel").ttf);
		//System.out.println(qp.metaSynchronizer.getUsableIndices());
		//System.out.println(qp.fetchData("dog"));
		//System.out.println(qp.fetchData("dog").docsInfo.size());
		
	}
	
	/**
	 * Processes query on all index version available
	 * @param term is the term to search for
	 * @return Term information wrapped in an object
	 * @throws Exception 
	 */
	public TermInfo fetchData(String term) throws Exception{
		List<Integer> indices = metaSynchronizer.getUsableIndices();
		if(indices.isEmpty()) return null;
		
		TermInfo termInfo = metaSynchronizer.getIndexManager(indices.get(0))
				.getTermInfo(this.fieldName, term);
		
		for(int i=1; i<indices.size(); i++){
			IndexManager im = metaSynchronizer.getIndexManager(indices.get(i));
			termInfo.merge(im.getTermInfo(this.fieldName, term));
		}
		return termInfo;
	}
	
	/**
	 * Fetches term statistics
	 * @param term is the term to search for
	 * @return Statistics wrapped in an object
	 * @throws IOException 
	 */
	public CatInfo getTermStatistics(String term) throws IOException{
		List<Integer> indices = metaSynchronizer.getUsableIndices();
		if(indices.isEmpty()) return null;
		
		CatInfo catInfo = metaSynchronizer.getIndexManager(indices.get(0))
				.getTermStats(this.fieldName, term);
		
		for(int i=1; i<indices.size(); i++){
			IndexManager im = metaSynchronizer.getIndexManager(indices.get(i));
			catInfo.merge(im.getTermStats(this.fieldName, term));
		}
		return catInfo;
	}
	
	/**
	 * Gets the vocabulary in the index 
	 * @return Set containing vocab of the index
	 * @throws IOException 
	 */
	public Set<String> getVocab() throws IOException{
		List<Integer> indices = metaSynchronizer.getUsableIndices();
		if(indices.isEmpty()) return null;
		
		Set<String> result = new HashSet<String>();
		
		for(int i=0; i<indices.size(); i++){
			IndexManager im = metaSynchronizer.getIndexManager(indices.get(i));
			result.addAll(im.getTerms(this.fieldName));
		}
		return result;
	}
	
	/**
	 * Fetches the position vector of a term
	 * @param term is the term to search for
	 * @return Term information wrapped in an object
	 * @throws Exception 
	 */
	public Map<String, List<Long>> getPositionVector(String term) throws Exception{
		Map<String, List<Long>> result = new HashMap<String, List<Long>>();
		
		for(Entry<String, DocInfo> e : this.fetchData(term).docsInfo.entrySet()){
			result.put(e.getKey(), e.getValue().docPos);
		};
		
		return result;
	}
	
}
