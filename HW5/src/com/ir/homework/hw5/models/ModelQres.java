package com.ir.homework.hw5.models;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class ModelQres extends LinkedList<Entry<String, Double>> implements Serializable{
	public ModelQres(Set<Entry<String, Double>> entrySet) {
		super(entrySet);
	}

	private static final long serialVersionUID = 1L;
	
}
