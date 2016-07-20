package com.ir.homework.hw5;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	//private static final String OUTPUT_FOLDR_PATH    = BASE_PATH + "/AP_DATA/results";
	private static final String OUTPUT_FOLDR_PATH    = BASE_PATH + "/1512_great_mordenist_artist/results";

	public static final String QREL_PATH = OUTPUT_FOLDR_PATH + "/qrels.txt";
	public static final String QRES_PATH = OUTPUT_FOLDR_PATH + "/output1000_UnigramLM_LaplaceSmoothing.txt";
	public static final String QPNG_PATH = QRES_PATH + ".png";
	
	public static final Boolean ENABLE_INDIVIDUAL_OUTPUT = false;
	public static final Double MAX_GRADE = 1.0;

}
