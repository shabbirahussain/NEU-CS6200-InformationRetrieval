/**
 * 
 */
package com.ir.homework.hw7.dataloader.parsers;

import javax.mail.Session;
import javax.mail.internet.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.mail.util.MimeMessageParser;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ir.homework.hw7.dataloader.models.ModelQrel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shabbirhussain
 * Parses given file into Map of fields and data
 */
public final class MimeFileParser implements Parser{
	private final String HOST = "localhost";
	private Map<String, ModelQrel> qrel;
	
	private Session session;
	
	/**
	 * Default Constructor
	 * @param qrel is the model of the qrel
	 */
	public MimeFileParser(Map<String, ModelQrel> qrel){
		java.util.Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", HOST);
		session = Session.getDefaultInstance(properties);
		
		this.qrel = qrel;
	}
	
	public Map<String, Object> parseFile(String qID, String filePath) throws Exception{
		// Read the file & Create parser out for the file
		File f = new File(filePath);
		FileInputStream fis = new FileInputStream(f);
		MimeMessageParser prsr = new MimeMessageParser(new MimeMessage(session, fis));
		try{prsr.parse();}catch(Exception e){}
		
		// Load properties into resultset
		Map<String, Object> result = new HashMap<String, Object>();
		try{result.put("From", prsr.getFrom());}catch(Exception e){}
		try{result.put("To"  , prsr.getTo());}catch(Exception e){}
		try{result.put("Cc"  , prsr.getCc());}catch(Exception e){}
		try{result.put("Bcc" , prsr.getBcc());}catch(Exception e){}
		try{result.put("ReplyTo" , prsr.getReplyTo());}catch(Exception e){}
		
		//////// Content extraction //////////////////////
		String content = "";
		try{
			content = prsr.getHtmlContent();
			if(content != null) content = getPlainTextFromHTML(content) + " ";
		}catch(Exception e){}
		
		content = content + prsr.getPlainContent();
		content = cleanContent(content);
		
		result.put("Content" , content);
		
		////////////////////////////////////////////////
		
		// Add label
		Double label = qrel.get(qID).get(f.getName());
		result.put("Label", label);
		
		fis.close();
		return result;
	}
	
	/**
	 * Tries to parse given html content
	 * @param html is the given html content to parse
	 * @return String equivalent if content can be successfully parsed
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private String getPlainTextFromHTML(String html) throws ParserConfigurationException, SAXException, IOException{
		
		org.jsoup.nodes.Document doc = Jsoup.parse(html);//dBuilder.parse(html);
		
		String text = doc.text().trim();
		return text;
	}
	
	/**
	 * Cleans the given content as per internal rules
	 * @param content is the given content to parse
	 * @return String of cleaned content
	 */
	private String cleanContent(String content){
		String result = content;
		result = result.replace("\n", " ");
		result = result.replace("\t", " ");
				
				
		return result;
	}
}
