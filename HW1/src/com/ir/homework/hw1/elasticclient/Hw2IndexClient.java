/**
 * 
 */
package com.ir.homework.hw1.elasticclient;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.ir.homework.hw2.indexers.CatalogManager.DocInfo;

/**
 * @author shabbirhussain
 *
 */
public class Hw2IndexClient extends BaseElasticClient implements Serializable, ElasticClient {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor
	 * @param indices
	 * @param types
	 * @param enableBulkProcessing
	 * @param limit
	 * @param field
	 */
	public Hw2IndexClient(String indices, String types, Boolean enableBulkProcessing, Integer limit, String field) {
		super(indices, types, enableBulkProcessing, limit, field);
	}


	/* (non-Javadoc)
	 * @see com.ir.homework.hw1.elasticclient.ElasticClient#getDocFrequency(java.lang.String)
	 */
	@Override
	public Map<String, Float> getDocFrequency(String term) throws IOException {
		Map<String, Float> result = new HashMap<String, Float>();
		try {
			for(Entry<String, DocInfo> e : _queryProcessor.fetchData(term).docsInfo.entrySet()){
				result.put(e.getKey(), ((Integer) e.getValue().docPos.size()).floatValue());
			}
		} catch (Exception e) {e.printStackTrace();}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.ir.homework.hw1.elasticclient.ElasticClient#getDocCount(java.lang.String)
	 */
	@Override
	public Long getDocCount(String term) throws IOException {
		Long result = null;
		try{
			result = ((Integer) _queryProcessor.fetchData(term).docsInfo.size()).longValue();
		}catch(Exception e) {e.printStackTrace();}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.ir.homework.hw1.elasticclient.ElasticClient#getTotalTermCount(java.lang.String)
	 */
	@Override
	public Long getTotalTermCount(String term) {
		Integer result = 0;
		try{
			for(Entry<String, DocInfo> e : _queryProcessor.fetchData(term).docsInfo.entrySet()){
				result += e.getValue().docPos.size();
			}
		}catch(Exception e) {e.printStackTrace();}
		
		return result.longValue();
	}
	
	@Override
	public Long getVocabSize(){
		Long result = null;
		try {
			result = ((Integer)_queryProcessor.getVocab().size()).longValue();
		} catch (IOException e) {e.printStackTrace();}
		
		return result;
	}
	
	@Override
	public Map<String, List<Long>> getPositionVector(String term){
		Map<String, List<Long>> result = null;
		try {
			result = _queryProcessor.getPositionVector(term);
		} catch (Exception e) {e.printStackTrace();}
		return result;
	}

}
