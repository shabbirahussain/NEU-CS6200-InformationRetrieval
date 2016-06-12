package com.ir.homework.hw3;

public abstract class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	public static final String OBJECT_STORE_PATH = "/Users/shabbirhussain/Data/IRData/ObjectStore/hw3/";
	
	public static final String  HOST = "localhost";//"192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final String INDEX_NAME = "web_dataset";
	public static final String INDEX_TYPE = "document";
	
	public static final Integer COOL_DOWN_INTERVAL = 1000; // is the amount of time between multiple requests to a domain in ms.
	
}
