package com.ir.homework.hw2.queryprocessing;

import static com.ir.homework.hw2.Constants.INDEX_ID;

import java.util.List;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.indexers.CatalogManager.TermInfo;
import com.ir.homework.hw2.metainfo.MetaInfoController;

public class QueryProcessor {
	private static MetaInfoController metaSynchronizer = new MetaInfoController(INDEX_ID);
	
	public static void main(String args[]) throws Exception{
		QueryProcessor qp = new QueryProcessor();
		
		System.out.println(qp.fetchData("HEAD", "cancel").docsInfo);
		
	}
	
	/**
	 * Processes query on all index version available
	 * @param query
	 * @return Term information wrapped in an object
	 * @throws Exception 
	 */
	public TermInfo fetchData(String field, String term) throws Exception{
		List<Integer> indices = metaSynchronizer.getUsableIndices();
		if(indices.isEmpty()) return null;
		
		TermInfo termInfo = metaSynchronizer.getIndexManager(indices.get(0))
				.getTermInfo(field, term);
		
		for(int i=1; i<indices.size(); i++){
			IndexManager im = metaSynchronizer.getIndexManager(indices.get(i));
			termInfo.merge(im.getTermInfo(field, term));
		}
		return termInfo;
	}
	
}
