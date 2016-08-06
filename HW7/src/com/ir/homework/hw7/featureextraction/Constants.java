package com.ir.homework.hw7.featureextraction;

public final class Constants{
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	private static final String DATASET_NAME = "trec07_spam";
	
	public static final String  INDEX_NAME   = DATASET_NAME; //"ap_dataset";
	public static final String  INDEX_TYPE   = "document";
	

	private static final String WORKING_FOLDER_PATH = BASE_PATH + "/" + DATASET_NAME;
	public  static final String FEAT_FILE_PATH = WORKING_FOLDER_PATH + "/results/features";
	
	public static final String  HOST = "elastichost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";

	
	
	public static final String[] MANUAL_FEAT_LIST = {"Label", "free", "spam", "click", "buy", "clearance", "shopper", "order", "earn", "cash", "extra", "money", "double", "collect", "credit", "check", "affordable", "fast", "price", "loans", "profit", "refinance", "hidden", "freedom", "chance", "miracle", "lose", "home", "remove", "success", "virus", "malware", "ad", "subscribe", "sales", "performance", "viagra", "valium", "medicine", "diagnostics", "million", "join", "deal", "unsolicited", "trial", "prize", "now", "legal", "bonus", "limited", "instant", "luxury", "legal", "celebrity", "only", "compare", "win", "viagra", "$$$", "$discount", "click here", "meet singles", "incredible deal", "lose weight", "act now", "100% free", "fast cash", "million dollars", "lower interest rate", "visit our website", "no credit check"};
// 	public static final String[] MANUAL_FEAT_LIST = {};
	

}
