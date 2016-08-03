/**
 * 
 */
package com.ir.homework.hw7.dataloader.parsers;

import javax.mail.Session;
import javax.mail.internet.*;

import org.apache.commons.mail.util.MimeMessageParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shabbirhussain
 * Parses given file into Map of fields and data
 */
public final class MimeFileParser extends AbstractParser{
	private final String HOST = "localhost";
	private Session session;
	private HTMLParser htmlParser;
	private File tmpFile;
	
	/**
	 * Default Constructor
	 * @param qrel is the model of the qrel
	 * @throws IOException 
	 */
	public MimeFileParser() throws IOException{
		java.util.Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", HOST);
		session = Session.getDefaultInstance(properties);
		
		tmpFile    = File.createTempFile("MIME", ".tmp");
	}
	
	/**
	 * Extracts the content of message from parser
	 * @param prsr is the parsed message
	 * @return Content of document
	 * @throws Exception
	 */
	private String getContent(MimeMessageParser prsr) throws Exception{
		String content = "";

		// Add subject
		try{
			content += prsr.getSubject() + " ";
		}catch(Exception e){}
		
		// Add HTML as text
		try{
			if(prsr.hasHtmlContent()) 
				content += htmlParser.parse(prsr.getHtmlContent()).get("Text") + " ";
		}catch(Exception e){}
		
		// Add Plain text content
		try{
			if(prsr.hasPlainContent())
				content += prsr.getPlainContent() + " ";
		}catch(Exception e){}
		content = cleanContent(content);
		
		return content;
	}

	@Override
	public Map<String, Object> parse(String data) throws Exception {
		// Create temp File
		PrintStream out = new PrintStream(tmpFile);
		out.println(data); out.close();
		FileInputStream fis = new FileInputStream(tmpFile);
		
		// Parse MIME Messages
		MimeMessageParser prsr = new MimeMessageParser(new MimeMessage(session, fis));
		try{prsr.parse();}catch(Exception e){}
		
		// Load properties into resultset
		Map<String, Object> result = new HashMap<String, Object>();
		try{result.put("From"    , prsr.getFrom());   }catch(Exception e){}
		try{result.put("To"      , prsr.getTo());     }catch(Exception e){}
		try{result.put("Cc"      , prsr.getCc());     }catch(Exception e){}
		try{result.put("Bcc"     , prsr.getBcc());    }catch(Exception e){}
		try{result.put("ReplyTo" , prsr.getReplyTo());}catch(Exception e){}
		try{result.put("Subject" , prsr.getSubject());}catch(Exception e){}
		try{result.put("Content" , getContent(prsr)); }catch(Exception e){}
		
		////////////////////////////////////////////////
		fis.close();
		return result;
	}
}
