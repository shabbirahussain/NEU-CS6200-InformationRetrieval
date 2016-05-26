package com.ir.homework.hw1;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	public static final String DATA_PATH = BASE_PATH + "/AP_DATA/ap89_collection";
	public static final String PRE_PROCESS_SRC_PATH = BASE_PATH + "/AP_DATA_RO/ap89_collection";
	public static final String PRE_PROCESS_DST_PATH = BASE_PATH + "/AP_DATA/ap89_collection";

	public static final String OBJECT_STORE_PATH = BASE_PATH + "/AP_DATA/cache/";
	//public static final String QUERY_FILE_PATH   = BASE_PATH + "/AP_DATA/query_desc.51-100.short.txt";
	public static final String QUERY_FILE_PATH   = BASE_PATH + "/AP_DATA/query_desc.51-100.short copy.txt";
	
	public static final String STOP_WORDS_FILE_PATH = BASE_PATH + "/AP_DATA/stoplist.txt";
	public static final String OUTPUT_FOLDR_PATH    = BASE_PATH + "/AP_DATA/results";
	public static final String OUTPUT_FILE_PATH     = OUTPUT_FOLDR_PATH + "/output1000_";
	public static final String TRECK_EVAL_PATH      = OUTPUT_FOLDR_PATH + "/trec_eval";
	public static final String TRECK_EVAL_PARAMS[]  = {OUTPUT_FOLDR_PATH + "/qrels.adhoc.51-100.AP89.txt"};
	
	
	
	// Word assosiations
	
	public static final String DATA_FILE_PREFIX = "ap";
	public static final String QUERY_NUMBER = ""; // Good docs = [98, 93, 58, 59, 100] 
	
	public static final String  HOST = "localhost";//"192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String CLUSTER_NAME = "dead-pool";
	
	public static final String INDEX_NAME = "ap_dataset";
	public static final String INDEX_TYPE = "document";
	
	public static final Boolean ENABLE_BULK_INSERT = true;
	public static final Boolean ENABLE_PERSISTENT_CACHE = true;
	public static final Boolean ENABLE_SILENT_MODE = false;
	public static final Boolean ENABLE_STEMMING = true;
	public static final Boolean ENABLE_PSEUDO_FEEDBACK = true;
	public static final Boolean ENABLE_ADD_NORMALIZATION = false;
	
	public static final Boolean EVALUATE_INDIVIDUAL_Q = false;
	
	public static final Integer MAX_RESULTS = 10000; 
	
	public static final String TEXT_FIELD_NAME = "TEXT";
	

}
