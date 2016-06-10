package com.ir.homework.hw2.queryprocessing;

import static com.ir.homework.hw2.Constants.INDEX_ID;

import java.util.List;

import com.ir.homework.hw2.indexers.IndexManager;
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
		QueryProcessor qp = new QueryProcessor(INDEX_ID, "TEXT");
		
		System.out.println(qp.fetchData("dog").docsInfo.size());
		
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
	
}
