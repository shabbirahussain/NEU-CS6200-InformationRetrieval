/**
 * 
 */
package com.ir.homework.hw3;

import static com.ir.homework.hw3.Constants.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.ir.homework.hw3.elasticclient.ElasticClient;
import com.ir.homework.hw3.elasticclient.ElasticClientBuilder;
import com.ir.homework.hw3.io.ObjectStore;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static ElasticClient elasticClient;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start = System.nanoTime(); 
		//elasticClient = (ElasticClient) ObjectStore.getOrDefault(elasticClient, OBJECT_STORE_PATH);
		Document doc = Jsoup.connect("http://en.wikipedia.org/robots.txt").get();
		Elements newsHeadlines = doc.select("#mp-itn b a");
		
		System.out.println(doc);
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		//ObjectStore.saveObject(elasticClient, OBJECT_STORE_PATH);
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
}
