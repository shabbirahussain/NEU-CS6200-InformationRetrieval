package com.ir.homework.hw3;

import java.util.HashMap;
import java.util.Map;

public abstract class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	public static final String OBJECT_STORE_PATH = "/Users/shabbirhussain/Data/IRData/ObjectStore/hw3/";
	public static final String STOP_WORDS_FILE_PATH = BASE_PATH + "/AP_DATA/stoplist.txt";
	
	public static final String  HOST = "localhost";//"192.168.1.105"; //"localhost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final String[] QUERY_TERMS = {"modern", "artists"};
	
	public static final String TOKENIZER_REGEXP = "(\\w+(\\.?\\w+)*)";
	
	public static final Integer COOL_DOWN_INTERVAL = 1000; // is the amount of time between multiple requests to a domain in ms.
	
	public static final String INDEX_NAME = "1512_great_mordenist_artist";
	public static final String INDEX_TYPE = "document";
	public static final String QUEUE_NAME = "frontier";
	public static final String QUEUE_TYPE = "queue";
	
	public static final String FIELD_VISITED        = "VISITED";
	public static final String FIELD_IN_CNT         = "IN_LINK_CNT";
	public static final String FIELD_PARENT_SCORE   = "PARENT_SCORE";
	public static final String FIELD_DISCOVERY_TIME = "DISCOVERY_TIME";
	public static final String FIELD_DOMAIN_NAME    = "DOMAIN_NAME";
	public static final String FIELD_VIS_DOMAIN_NAME= "VISITED_DOMAIN_NAME";
	
	public static final String FIELD_TEXT           = "TEXT";
	public static final String FIELD_TITLE			= "TITLE";
	public static final String FIELD_DT_UPDATED     = "LAST_UPDATED";
	
	public static final String SCRIPT_DEQUEUE = "MAGIC_DEQUEUE";
	public static final String SCRIPT_ENQUEUE = "MAGIC_ENQUEUE";
	
	public static Map<String, Object> DEFAULT_QUEUE_FIELDS = new HashMap<String, Object>();
	
	
	static{
		try{
			
			
			
			DEFAULT_QUEUE_FIELDS.put(FIELD_VISITED, false);
			DEFAULT_QUEUE_FIELDS.put(FIELD_IN_CNT, 0);
			DEFAULT_QUEUE_FIELDS.put(FIELD_PARENT_SCORE, 0F);
			DEFAULT_QUEUE_FIELDS.put(FIELD_DOMAIN_NAME, "");
			DEFAULT_QUEUE_FIELDS.put(FIELD_VIS_DOMAIN_NAME, "");
			DEFAULT_QUEUE_FIELDS.put(FIELD_DISCOVERY_TIME, 1);
						
		}catch(Exception e){e.printStackTrace();}
	}
														
}
