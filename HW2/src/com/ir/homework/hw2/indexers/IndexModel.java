/**
 * 
 */
package com.ir.homework.hw2.indexers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw2.cache.Translator;
import com.ir.homework.hw2.tokenizers.Tokenizer;

/**
 * Creates loads index
 * @author shabbirhussain
 */
abstract class IndexModel implements Serializable{
	// Serialization version id
	private static final long serialVersionUID = 1L;
	private Tokenizer   tokenizer;
	private Translator translator;
	 
	//            field       term        docID   freq
	protected Map<String, Map<String, Map<String, Integer>>> fieldTermDocMap;
	
	/**
	 * Default constructor
	 * @param indexID is the unique identifier of the index
	 */
	public IndexModel(Tokenizer tokenizer, Translator translator){
		this.tokenizer  = tokenizer;
		this.translator = translator;
		this.fieldTermDocMap = new HashMap<String, Map<String, Map<String, Integer>>>();
	}
	
	/**
	 * Loads data provided in map into the index
	 * @param docID unique identifier of document
	 * @param data text data to be indexed in form of field value tuples
	 */
	public void loadData(String docID, Map<String, String> data){
		docID = this.translator.translateDocID(docID).toString();
		
		// for each field in data
		for(Entry<String, String> e: data.entrySet()){
			String field = e.getKey();
			Map<String, Map<String, Integer>> termDocMap = fieldTermDocMap.getOrDefault(field , (new HashMap<String, Map<String, Integer>>()));
			
			List<String> terms = tokenizer.tokenize(e.getValue());
			// for each term in data
			for(String term : terms){
				Map<String, Integer> docMap = termDocMap.getOrDefault(term, (new HashMap<String, Integer>()));
				Integer docTF               = docMap.getOrDefault(docID, 0) + 1;
				
				// Store data back
				docMap.put(docID, docTF);
				termDocMap.put(term, docMap);
			}
			this.fieldTermDocMap.put(field, termDocMap);
			//System.out.println(fieldTermDocMap);
		}
	}
}
