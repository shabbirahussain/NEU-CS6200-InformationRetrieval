package com.ir.homework.hw1.elasticutil;

public abstract class ElasticClientBuilder {
	/**
	 * Creates a new elastic client
	 * @return
	 */
	public static ElasticClient createElasticClient(){
		return new ElasticClient();
	}
}
