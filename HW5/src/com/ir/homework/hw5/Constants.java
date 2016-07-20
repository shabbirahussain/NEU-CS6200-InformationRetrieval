package com.ir.homework.hw5;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	private static final String OUTPUT_FOLDR_PATH    = BASE_PATH + "/AP_DATA/results";

	public static final String QREL_PATH = OUTPUT_FOLDR_PATH + "/qrels.adhoc.51-100.AP89.txt";
	public static final String QRES_PATH = OUTPUT_FOLDR_PATH + "/output1000_OkapiTFController.txt";
	public static final String QPNG_PATH = QRES_PATH + ".png";
	
	public static final Boolean ENABLE_INDIVIDUAL_OUTPUT = true;
	public static final Double MAX_GRADE = 1.0;

}
