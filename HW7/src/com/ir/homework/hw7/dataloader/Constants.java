package com.ir.homework.hw7.dataloader;

public final class Constants{
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	private static final String DATASET_NAME = "trec07_spam";
	
	public static final String  INDEX_NAME   = DATASET_NAME; //"ap_dataset";
	public static final String  INDEX_TYPE   = "document";
	

	private static final String WORKING_FOLDER_PATH = BASE_PATH + "/" + DATASET_NAME;
	public  static final String DATA_PATH = WORKING_FOLDER_PATH + "/data";
	public  static final String DATA_FILE_PREFIX = "inmail";

	public  static final String QREL_PATH = WORKING_FOLDER_PATH + "/results/qrel.txt"; 
	
	public static final String  HOST = "elastichost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";

	
	public static final Integer MAX_RESULTS = 40000; 
	
	public static final String TEXT_FIELD_NAME = "TEXT";
	

}
