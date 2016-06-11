package com.ir.homework.hw2;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	public static final String DATA_PATH  = BASE_PATH + "/AP_DATA/ap89_collection";
	public static final String INPUT_PATH = BASE_PATH + "/AP_DATA_RO/ap89_collection";
	public static final String INDEX_PATH = BASE_PATH + "/AP_DATA/indexes";
	//public static final String EVAL_OUTPUT_FILE = BASE_PATH + "/AP_DATA/results/out.0.no.stop.stem.txt";
	public static final String EVAL_OUTPUT_FILE = BASE_PATH + "/AP_DATA/results/out.0.stop.stem.txt";
	public static final String EVAL_INPUT_FILE = BASE_PATH + "/AP_DATA/queries/in.0.50.txt";
	
	public static final String INDEX_ID = "ap_dataset/document";
	//public static final String INDEX_ID = "ap_dataset_nostem_nostop/document";
	
	public static final String STOP_WORDS_FILE_PATH = BASE_PATH + "/AP_DATA/stoplist.txt";
	

	public static final Integer BATCH_SIZE = 3000;
	public static final Integer MAX_ACTIVE_INDICES = 1;
	public static final Boolean ENABLE_STEMMING = false;
	public static final Boolean ENABLE_STOPWORD_FILTER = false;
	public static final Boolean ENABLE_FULL_DOC_ID = false;
	public static final Boolean ENABLE_AUTO_CLEAN = true;
	
	public static final String DATA_FILE_PREFIX = "ap";
	public static final String DONE_FILE_SUFFIX = ".done";
	public static final String TOKENIZER_REGEXP = "(\\w+(\\.?\\w+)*)";
	
	public static final String FIELDS_TO_LOAD[] = {"text", "head"};
	// Word assosiations
	
	
	public static final String  HOST = "localhost";//"192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final Integer MAX_RESULTS = 40000; 
	public static final Integer MAX_RESULTS_OUTPUT = 1000;
	
}
