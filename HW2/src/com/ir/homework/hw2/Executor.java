/**
 * 
 */
package com.ir.homework.hw2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.metainfo.MetaInfoController;
import com.ir.homework.hw2.metainfo.MetaInfoControllers;
import com.ir.homework.hw2.tokenizers.DefaultTokenizer;
import com.ir.homework.hw2.tokenizers.Tokenizer;


import static com.ir.homework.hw2.Constants.*;

/**
 * @author shabbirhussain
 *
 */
public final class Executor{
	private static Set<String>  stopWords = new HashSet<String>();
	private static Tokenizer    tokenizer;
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		long start = System.nanoTime(); 
		
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
		
		MetaInfoControllers.saveMetaInfo();
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
				
	}
	
	/**
	 * Gets the index manager for the given index version
	 * @param idxVer is the version of index
	 * @return IndexManager
	 */
	private static IndexManager getIndexManager(Integer idxVer){
		return (new IndexManager(INDEX_ID, idxVer))
				.setTokenizer(tokenizer);
	}
}
