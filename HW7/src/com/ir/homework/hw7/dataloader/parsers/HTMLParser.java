package com.ir.homework.hw7.dataloader.parsers;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;

public class HTMLParser extends AbstractParser{

	@Override
	public Map<String, Object> parse(String html) throws Exception{
		html = html.replaceAll("<", "\n<");
		
		org.jsoup.nodes.Document doc = Jsoup.parse(html);//dBuilder.parse(html);
		String value = "";
		try{value = doc.text().trim();}catch(Exception e){}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("Text", value);
		
		return result;
	}
}
