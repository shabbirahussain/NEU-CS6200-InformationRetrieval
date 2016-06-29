package com.ir.homework.hw3;

import static com.ir.homework.hw3.Constants.DEFAULT_QUEUE_FIELDS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;

public class test {
	private HashMap<String, Object> enqueueBuffer;
	
	 public static void main(String args[]) throws Exception {
		 (new test()).test2();
	 }
	 public void test2() throws IOException{
		 //Jsoup.connect("https://en.wikipedia.org/wiki/Constantin_Br%C3%A2ncu%C8%99i");
		 //Jsoup.connect("https://en.wikipedia.org/wiki/Constantin_Br%C3%83%C2%A2ncu%C3%88%C2%99i
		 
		 Jsoup.connect("http://en.wikipedia.org/wiki/Constantin_Brâncuși")
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
					.followRedirects(true)
					.execute();
	 }
	 
	 public void test() throws MalformedURLException{
		enqueueBuffer = new HashMap<String, Object>();
		URL url = new URL("file:///Users/shabbirhussain/Apps/elasticsearch-2.3.2/plugins/calaca/_site/index.html");
		//myclass url = new myclass("file:///Users/shabbirhussain/Apps/elasticsearch-2.3.2/plugins/calaca/_site/index.html");
		for(Integer i=0;i<50000;i++)
			enqueueBuffer.put(i.toString(), i);
		
		Long time = System.currentTimeMillis();
		//System.out.println(System.currentTimeMillis());
		for(int i=0;i<200;i++)
			enqueueBuffer.getOrDefault(url.toString(), new HashMap<String, Object>(DEFAULT_QUEUE_FIELDS));
		System.out.println(System.currentTimeMillis()- time);
		
//		time = System.currentTimeMillis();
//		for(int i=0;i<500000;i++)
//			enqueueBuffer.getOrDefault(url, new HashMap<String, Object>(DEFAULT_QUEUE_FIELDS));
//		System.out.println(System.currentTimeMillis()-time);
	}

}
