package com.ir.homework.hw7.featureextraction.models;

import java.util.HashMap;
import java.util.Map;

public class MFeature extends HashMap<String, Double> {
	private static final long serialVersionUID = 1L;

	@Override
	public Double put(String key, Double value){
		return super.put(keyEncoder(key), value);
	}

	@Override
	public Double get(Object key){
		return super.get(keyEncoder(key.toString()));
	}
	
	@Override
	public Double getOrDefault(Object key, Double value){
		Double result = this.get(key);
		if(result == null) result = value;
		return result;
	}
	
	@Override
	public void putAll(Map<? extends String, ? extends Double> m){
		for(Entry<? extends String, ? extends Double> e: m.entrySet())
			this.put(e.getKey(), e.getValue());
	}
	
	/**
	 * Cleans the key to keep only alphanumeric values
	 * @param key is the given key to cleanse
	 * @return Key value cleaned of non alphanumeric characters
	 */
	private String keyEncoder(String key){
		return key.replaceAll("\\W", "_");
	}
}
