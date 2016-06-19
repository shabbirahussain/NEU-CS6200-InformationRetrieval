/**
 * 
 */
package com.ir.homework.hw3;

import static com.ir.homework.hw3.Constants.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.elasticsearch.search.SearchHit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;

import com.ir.homework.hw3.elasticclient.ElasticClient;
import com.ir.homework.hw3.io.StopWordReader;
import com.ir.homework.hw3.tools.DefaultTokenizer;
import com.ir.homework.hw3.tools.URLCanonizer;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

/**
 * @author shabbirhussain
 *
 */
public final class Executor {
	private static ElasticClient _elasticClient;
	private static DefaultTokenizer _tokenizer;
	private static List<String> _queryTerms;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime(); 
		
		_tokenizer = new DefaultTokenizer(TOKENIZER_REGEXP)
				.setStemming(true)
				.setStopWordsFilter((new StopWordReader(STOP_WORDS_FILE_PATH))
						.getStopWords());
		
		_queryTerms = new LinkedList<String>();
		Stemmer stemmer = new PorterStemmer();
		for(String term : QUERY_TERMS){
			_queryTerms.add(stemmer.stem(term).toString());
		}
		
		_elasticClient = new ElasticClient();
		
		executeItteration();
		
	}
	
	
	private static void executeItteration() throws Exception{
		long start = System.nanoTime(); 
		
		System.out.print("Dequeing elements...");
		SearchHit[] queue = _elasticClient.dequeue(10);
		System.out.println("[Took: " + ((System.nanoTime() - start) * 1.0e-9) +"s] ");
		
		for(SearchHit hit : queue){
			String url = hit.getId();
			System.out.print("Fetching [" + url + "]");
			
			Integer discoveryTime = hit.getFields()
					.get(FIELD_DISCOVERY_TIME)
					.getValue();
			
			long lastAccessTime = System.currentTimeMillis();
			try{
				Document doc = Jsoup.connect(url).get();
				String text = getPlainTextContent(doc);
				_elasticClient.loadData(url, doc.select("title").text(), text);
			
				for(URL link : getLinksFromPage(doc)){
					_elasticClient.enqueue(getScore(text), link, ++discoveryTime);
				}
			}catch(Exception e){}
			Thread.sleep(Math.max(0, COOL_DOWN_INTERVAL - System.currentTimeMillis() + lastAccessTime));
			
			System.out.println("[Took: " + ((System.nanoTime() - start) * 1.0e-9) +"s] ");
		}
		System.out.print("Storing results...");
		_elasticClient.flush();
		System.out.println("[Took: " + ((System.nanoTime() - start) * 1.0e-9) +"s] ");
		
		System.out.println("Time Required=" + ((System.nanoTime() - start) * 1.0e-9));
	}
	
	
	/**
	 * Given list of terms gives a document score
	 * @param document is the text of document to score
	 * @return
	 */
	private static Float getScore(String document) {
		Map<String, Float> tfMap = new HashMap<String, Float>();
		
		for(String term : _tokenizer.tokenize(document)){
			tfMap.put(term, tfMap.getOrDefault(term, 0F) + 1);
		}
		
		Float okapi_tf = 0F;
		for(String term: _queryTerms){
			Float tf_w_d    = tfMap.getOrDefault(term, 0F);
			Float len_d     = ((Integer) tfMap.size()).floatValue();
			Float avg_len_d = 10000F; //_elasticClient.getAvgDocLen();
			
			okapi_tf += (float) (tf_w_d / (tf_w_d + 0.5 + 1.5*(len_d/avg_len_d)));
		}
		
		return okapi_tf;
	}
	
	/**
	 * Returns plain text content from document
	 * @param doc
	 * @return
	 */
	private static String getPlainTextContent(Document doc){
		return doc.select("body").text();
	}
	
	/**
	 * Gets a list of valid urls from page to load
	 * @param doc is the document page
	 * @return List of urls
	 */
	private static Collection<URL> getLinksFromPage(Document doc){
		Collection<URL> result = new LinkedList<URL>();
		
		Elements elems = doc.select("body").select("a[href]");
		
		for(org.jsoup.nodes.Element elem : elems){
			String href = elem.attr("href");
			URL url = null;
			try{
				url = URLCanonizer.getCanninizedURL(href);
			}catch(Exception e){continue;}
			
			result.add(url);
		}
		return result;
	}
}
