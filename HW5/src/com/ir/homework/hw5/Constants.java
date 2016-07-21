package com.ir.homework.hw5;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	//private static final String DATASET_NAME = "ap_dataset"; 
	private static final String DATASET_NAME = "1512_great_mordenist_artist"; 
	private static final String WORKING_FOLDER_PATH = BASE_PATH + "/" + DATASET_NAME;

	private static final String OUTPUT_FOLDR_PATH    = WORKING_FOLDER_PATH + "/results";

	public static final String QREL_PATH = OUTPUT_FOLDR_PATH + "/qrels.txt";
	//public static final String QRES_PATH = OUTPUT_FOLDR_PATH + "/Trec-Text-HW5.txt";
	public static final String QRES_PATH = OUTPUT_FOLDR_PATH +"/output1000_OkapiBM25Controller.txt";
	public static final String QPNG_PATH = QRES_PATH + ".png";
	
	public static final String TRECK_EVAL_PATH      = OUTPUT_FOLDR_PATH + "/trec_eval";
	public static final String TRECK_EVAL_PARAMS[]  = {QREL_PATH};
	
	public static final Boolean EVALUATE_INDIVIDUAL_Q = true;

}
