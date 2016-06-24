package com.ir.homework.hw3;

import java.util.HashMap;
import java.util.Map;

public abstract class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	public static final String OBJECT_STORE_PATH = "/Users/shabbirhussain/Data/IRData/ObjectStore/hw3/";
	public static final String STOP_WORDS_FILE_PATH = BASE_PATH + "/AP_DATA/stoplist.txt";
	
	public static final String  HOST = "elastichost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final String[] QUERY_TERMS = {"modern", "artists", "modernist"};
	
	public static final String TOKENIZER_REGEXP = "(\\w+(\\.?\\w+)*)";

	public static final Float   DEQUEUE_RND_PERCENT = 1F;
	public static final Integer DEQUEUE_SIZE  = ((Float) (60F / DEQUEUE_RND_PERCENT)).intValue();
	public static final Integer MAX_QUEUE_SIZE = 5000;
	public static final Integer COOL_DOWN_INTERVAL  = 1000; // is the amount of time between multiple requests to a domain in ms.
	public static final Long    TRUNCATION_INTERVAL = 10 * 1000L;
	public static final Integer MAX_BUFFER_SIZE = 10000;
	
	public static final Short   MAX_NO_THREADS = 20;	
	
	public static final String INDEX_NAME    = "1512_great_mordenist_artist";
	public static final String INDEX_TYPE    = "document";
	public static final String QUEUE_NAME    = "frontier";
	public static final String QUEUE_TYPE    = "queue";
	public static final String LINK_MAP_NAME = "links";
	public static final String LINK_MAP_TYPE = "map";
	
	
	public static final String FIELD_VISITED        = "VISITED";
	public static final String FIELD_IN_CNT         = "IN_LINK_CNT";
	public static final String FIELD_PARENT_SCORE   = "PARENT_SCORE";
	public static final String FIELD_DISCOVERY_TIME = "DISCOVERY_WAVE_NO";
	public static final String FIELD_DOMAIN_NAME    = "DOMAIN_NAME";
	public static final String FIELD_VIS_DOMAIN_NAME= "VISITED_DOMAIN_NAME";
	public static final String FIELD_VISITED_DATE   = "VISITED_DATE";
	public static final String FIELD_SRC_LINK       = "SRC_LINK";
	public static final String FIELD_DST_LINK       = "DST_LINK";
	

	public static final String FIELD_HTTP_HEADERS   = "HTTP_HEADERS";
	public static final String FIELD_HTML   		= "HTML";
	public static final String FIELD_TITLE			= "TITLE";
	public static final String FIELD_TEXT           = "TEXT";
	public static final String FIELD_DT_UPDATED     = "LAST_UPDATED";
	
	public static final String SCRIPT_DEQUEUE = "MAGIC_DEQUEUE";
	public static final String SCRIPT_ENQUEUE = "MAGIC_ENQUEUE";
	public static final String SCRIPT_TRUNCATE= "MAGIC_TRUNCATE";
	
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
