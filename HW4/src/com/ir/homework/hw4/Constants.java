package com.ir.homework.hw4;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	public static final String  LINKS_PATH = BASE_PATH + "/wt2g_inlinks.txt";

	public static final Integer MAX_BUFFER_SIZE = 10000;
	public static final String  HOST = "elastichost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final String LINK_MAP_NAME = "links";
	public static final String LINK_MAP_TYPE = "map";
	
	public static final String DAT_IDX_NAME = "1512_great_mordenist_artist";
	public static final String DAT_IDX_TYPE = "document";

	public static final String QUERY_TERMS = "modern artists modernist";
	
	public static final String FIELD_SRC_LINK       = "SRC_LINK";
	public static final String FIELD_DST_LINK       = "DST_LINK";
	public static final String FIELD_TEXT           = "TEXT";
	
	public static final Integer NUM_OF_ITERATIONS = 5000;
	public static final Integer PEEK_INTERVAL     = 50;
	
	public static final Integer ROOT_SET_SIZE     = 150;
	public static final Integer MAX_STAGES        = 2;
	
	public static final String OBJECTSTORE_PATH = BASE_PATH+"/ranking/"+LINK_MAP_NAME+"/"+LINK_MAP_TYPE+"/";
	

}
