package com.ir.homework.hw6;


public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";

	private static final String DATASET_NAME = "ap_dataset";
	
	public static final String  INDEX_NAME   = DATASET_NAME; //"ap_dataset";
	public static final String  INDEX_TYPE   = "document";

	private static final String WORKING_FOLDER_PATH = BASE_PATH + "/" + DATASET_NAME;
	
	public static final String QUERY_FILE_PATH  = WORKING_FOLDER_PATH + "/queries/query_desc.txt"; //query_desc.51-100.short.txt
	public static final String OBJECTSTORE_PATH = WORKING_FOLDER_PATH + "/model/" + INDEX_NAME + "/" + INDEX_TYPE + "/";
	
	public static final String STOP_WORDS_FILE_PATH = WORKING_FOLDER_PATH + "/stoplist.txt";
	public static final String OUTPUT_FOLDR_PATH    = WORKING_FOLDER_PATH + "/results";
	public static final String OUTPUT_FILE_PATH     = OUTPUT_FOLDR_PATH + "/";
	
	public static final String QREL_PATH            = OUTPUT_FOLDR_PATH + "/qrels.txt";
	public static final String QRES_PATH			= OUTPUT_FOLDR_PATH + "/featuresFile.csv";

	// Word assosiations
	
	public static final String DATA_FILE_PREFIX = "output";
	public static final String SEPARATOR = ",";
}
