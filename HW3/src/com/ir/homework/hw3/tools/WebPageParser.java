package com.ir.homework.hw3.tools;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebPageParser {
	
	/**
	 * Stores the parsed webpages
	 * @author shabbirhussain
	 */
	public class ParsedWebPage{
		public String headers ;
		public String html;
		public String title;
		public String text;
		public Collection<URL> outLinks;
	}

	/**
	 * Parses the web page to generate a ParsedWebPage out of it
	 * @param response is the Jsoup response given to parse
	 * @return An object containing all important fields from that response
	 * @throws IOException 
	 */
	public ParsedWebPage parseResponse(Response response) throws IOException{
		ParsedWebPage result = new ParsedWebPage();
		
		Document doc;
		doc = response.parse();
		result.headers  = response.headers().toString();
		result.html     = doc.outerHtml();
		result.title    = doc.select("title").text();
		result.text     = doc.body().text(); //getPlainTextContent(doc);
		result.outLinks = getLinksFromPage(doc);

		return result;
	}
	
	/**
	 * Gets a list of valid urls from page to load
	 * @param doc is the document page
	 * @return List of urls
	 */
	private Collection<URL> getLinksFromPage(Document doc){
		Elements elems = doc.select("a");
		Collection<URL> result = new LinkedList<URL>();
		
		for(org.jsoup.nodes.Element elem : elems){
			try{
				URL url = URLCanonizer.getCanninizedURL(elem.attr("href"));
				if(url != null) result.add(url);
			}catch(Exception e){}
		}
		return result;
	}
}
