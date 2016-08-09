package com.ir.homework.hw7.featureextraction.filters;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.client.transport.TransportClient;

/**
 * Provides functionality to filter features based on feature keys. Features can be whitelisted or blacklisted. 
 * @author shabbirhussain
 *
 */
public class ListFeatureFilter extends AbstractFeatureFilter {
	private Set<String> whiteList, blackList;

	/**
	 * Default constructor
	 * @param client is the transport client
	 * @param indices name of index to query
	 * @param types name of types to query
	 * @throws UnknownHostException 
	 */
	public ListFeatureFilter(TransportClient client, String indices, String types) throws UnknownHostException {
		super(client, indices, types);
		blackList = null;
		whiteList = null;
	}

	/**
	 * Adds features to the blacklist
	 * @param list is the list of features to block
	 * @param analyzer is the analyzer to process list
	 */
	public ListFeatureFilter addBlackList(String[] list, String analyzer){
		if(blackList == null) blackList = new HashSet<String>();
		for(String q: list){
			blackList.addAll(super.analyzeQuery(q, analyzer));
		}
		return this;
	}
	
	/**
	 * Adds features to the whitelist
	 * @param list is the list of features to block
	 * @param analyzer is the analyzer to process list
	 */
	public ListFeatureFilter addWhiteList(String[] list, String analyzer){
		if(whiteList == null )whiteList = new HashSet<String>();
		for(String q: list){
			whiteList.addAll(super.analyzeQuery(q, analyzer));
		}
		return this;
	}

	@Override
	public Map<String, Double> applyFilters(Map<String, Double> featureMap) {
		Map<String, Double> result = new HashMap<String, Double>();
		
		// Add whitelisted elements
		if(whiteList == null){
			result.putAll(featureMap);
		}else{
			for(String key: whiteList){
				if(featureMap.containsKey(key))
					result.put(key, featureMap.get(key));
			}
			
		}
		
		// Remove blacklisted elements
		if(blackList != null)
			for(String key: blackList)
				result.remove(key);
		
		return result;
	}

}
