package com.ir.homework.hw7.featureextraction.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MFeature implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String label;
	public Map<String, Double> map;
	
	public MFeature() {
		map = new HashMap<String, Double>();
	}
	
	@Override
	public String toString(){
		String result = "";
		
		result += label + ": ";
		result += map.toString();
		
		return result;
	}
}
