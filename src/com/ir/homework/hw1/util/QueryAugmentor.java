package com.ir.homework.hw1.util;

import com.ir.homework.hw1.elasticclient.ElasticClient;

public class QueryAugmentor {
	ElasticClient searchClient;
	/**
	 * Default constructor
	 * @param searchClient elastic search client to use
	 */
	public QueryAugmentor(ElasticClient searchClient){
		this.searchClient = searchClient;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
