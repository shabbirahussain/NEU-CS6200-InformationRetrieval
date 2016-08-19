package com.ir.homework.hw1;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";

	//private static final String DATASET_NAME = "1512_great_mordenist_artist";
	private static final String DATASET_NAME = "ap_dataset";
	
	public static final String  INDEX_NAME   = "ap_dataset1";//DATASET_NAME; //"ap_dataset";
	public static final String  INDEX_TYPE   = "document";
	
	public static final String PRE_PROCESS_SRC_PATH = BASE_PATH + "/AP_DATA_RO/ap89_collection";
	public static final String PRE_PROCESS_DST_PATH = BASE_PATH + "/ap_dataset/ap89_collection";

	private static final String WORKING_FOLDER_PATH = BASE_PATH + "/" + DATASET_NAME;
	
	public static final String QUERY_FILE_PATH  = WORKING_FOLDER_PATH + "/queries/query_desc.txt"; //query_desc.51-100.short.txt
	public static final String OBJECTSTORE_PATH = WORKING_FOLDER_PATH + "/cache/" + INDEX_NAME + "/" + INDEX_TYPE + "/";
	
	public static final String STOP_WORDS_FILE_PATH = WORKING_FOLDER_PATH + "/stoplist.txt";
	public static final String OUTPUT_FOLDR_PATH    = WORKING_FOLDER_PATH + "/results";
	public static final String OUTPUT_FILE_PATH     = OUTPUT_FOLDR_PATH + "/output1000_";
	
	public static final String QREL_PATH            = OUTPUT_FOLDR_PATH + "/qrels.txt";
	public static final String TRECK_EVAL_PATH      = OUTPUT_FOLDR_PATH + "/trec_eval";
	public static final String TRECK_EVAL_PARAMS[]  = {QREL_PATH};
	
	
	// Word assosiations
	
	public static final String DATA_FILE_PREFIX = "ap";
	public static final String QUERY_NUMBER = ""; // Good docs = [98, 93, 58, 59, 100] 
	
	public static final String  HOST = "elastichost";//"192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";

	
	public static final Boolean ENABLE_BULK_INSERT = false;
	public static final Boolean ENABLE_SILENT_MODE = false;
	public static final Boolean ENABLE_STEMMING = true;
	public static final Boolean ENABLE_PSEUDO_FEEDBACK = false;
	public static final Boolean ENABLE_ADD_NORMALIZATION = true;
	public static final Boolean ENABLE_FULL_TREC_OUTPUT = true;
	
	public static final Boolean ENABLE_HW2_CLIENT = false;
	
	public static final Boolean EVALUATE_INDIVIDUAL_Q = false;
	
	public static final Integer MAX_RESULTS = 40000; 
	public static final Integer MAX_RESULTS_OUTPUT = 40000;
	
	public static final String TEXT_FIELD_NAME = "TEXT";
	

}
