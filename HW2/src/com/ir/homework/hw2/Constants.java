package com.ir.homework.hw2;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	public static final String DATA_PATH  = BASE_PATH + "/AP_DATA/ap89_collection";
	public static final String INPUT_PATH = BASE_PATH + "/AP_DATA_RO/ap89_collection";
	public static final String INDEX_PATH = BASE_PATH + "/AP_DATA/indexes";

	public static final String INDEX_ID = "ap_dataset/document";
	
	public static final String STOP_WORDS_FILE_PATH = BASE_PATH + "/AP_DATA/stoplist.txt";
	

	public static final Integer BATCH_SIZE = 1000;
	public static final Boolean ENABLE_STEMMING = true;
	public static final Boolean ENABLE_STOPWORD_FILTER = true;
	// Word assosiations
	
	public static final String DATA_FILE_PREFIX = "ap";
	
	public static final String  HOST = "localhost";//"192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final String INDEX_NAME = "ap_dataset";
	public static final String INDEX_TYPE = "document";
	
	public static final Boolean ENABLE_BULK_INSERT = false;
	public static final Boolean ENABLE_PERSISTENT_CACHE = true;
	public static final Boolean ENABLE_SILENT_MODE = false;
	public static final Boolean ENABLE_PSEUDO_FEEDBACK = true;
	public static final Boolean ENABLE_ADD_NORMALIZATION = true;
	public static final Boolean ENABLE_FULL_TREC_OUTPUT = false;
	
	public static final Boolean EVALUATE_INDIVIDUAL_Q = false;
	
	public static final Integer MAX_RESULTS = 40000; 
	public static final Integer MAX_RESULTS_OUTPUT = 1000;
	
	public static final String TEXT_FIELD_NAME = "TEXT";
	

}
