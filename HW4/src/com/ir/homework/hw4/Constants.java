package com.ir.homework.hw4;

public final class Constants {
	private static final String BASE_PATH = "/Users/shabbirhussain/Data/IRData";
	
	public static final String  LINKS_PATH = BASE_PATH + "/wt2g_inlinks.txt";

	public static final Integer MAX_BUFFER_SIZE = 10000;
	public static final String  HOST = "elastichost";
	public static final Integer PORT = 9300;
	public static final String  CLUSTER_NAME = "dead-pool";
	
	public static final String LINK_MAP_NAME = "links";
	public static final String LINK_MAP_TYPE = "map1";
	
	public static final String FIELD_SRC_LINK       = "SRC_LINK";
	public static final String FIELD_DST_LINK       = "DST_LINK";
	
	public static final String OBJECTSTORE_PATH = BASE_PATH+"/ranking/"+LINK_MAP_NAME+"/"+LINK_MAP_TYPE+"/";
	

}
