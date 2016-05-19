package com.ir.homework.hw1.controllers;

import java.util.Map.Entry;

import com.ir.homework.io.OutputWriter;

public abstract class SearchController {
	protected String index;
	protected String type;
	
	public SearchController(String index, String type){
		this.index = index;
		this.type=type;
	}
	
	public abstract OutputWriter.OutputRecord executeQuery(Entry<String, String[]> q);
}
