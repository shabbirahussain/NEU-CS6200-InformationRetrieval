package com.ir.homework.hw4.rankers;

import static com.ir.homework.hw4.Constants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.ir.homework.hw4.elasticclient.ElasticClient;

public class PageRank {
	private static ElasticClient  elasticClient;
	private Map<String, LinkInfo> linksMap;
	
	private class LinkInfo{
		public float score;
		public Collection<String> links;
		
		public LinkInfo(){
			score = -1F;
			links = new LinkedList<String>();
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		elasticClient = new ElasticClient();
		(new PageRank()).rankPages();
	}

	public void rankPages(){
		
	}
}
