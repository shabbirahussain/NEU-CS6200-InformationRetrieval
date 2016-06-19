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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.search.SearchHit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;

import com.ir.homework.hw3.elasticclient.ElasticClient;
import com.ir.homework.hw3.frontier.FrontierTruncator;
import com.ir.homework.hw3.io.StopWordReader;
import com.ir.homework.hw3.tools.DefaultTokenizer;
import com.ir.homework.hw3.tools.URLCanonizer;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

/**
 * @author shabbirhussain
 *
 */
public final class Executor extends Thread{
	private static ElasticClient _elasticClient;
	private static DefaultTokenizer _tokenizer;
	private static List<String> _queryTerms;
	
	private static Map<String, Long> _domainAccessTime;
	private Short threadID;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime(); 
		
		// Initalizing
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
		
		// Create threads
		FrontierTruncator truncator = new FrontierTruncator(_elasticClient, TRUNCATION_INTERVAL);
		truncator.start();
		
		_domainAccessTime = new ConcurrentHashMap<String, Long>();
		for(Short i=0;i<MAX_NO_THREADS; i++){
			(new Executor(i)).start();
		}
	}
	
	public Executor(Short threadID){
		this.threadID = threadID;
	}
	
	/**
	 * Executes thread
	 */
	public void run(){
		while(true){
			try {
				System.out.println("["+this.threadID + "] Dequeing...");
				SearchHit[] queue = _elasticClient.dequeue(DEQUEUE_SIZE);
				if(queue.length == 0) continue;
				
				for(SearchHit hit : queue){
					executeItteration(hit);
				}
				System.out.println("["+this.threadID + "] Storing results...");
				_elasticClient.flush();
				//System.out.println("[Took: " + ((System.nanoTime() - start) * 1.0e-9) +"s] ");
				
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Executes iteration
	 * @throws Exception
	 */
	private void executeItteration(SearchHit hit) throws Exception{
		String url  = hit.getId();
		String host = new URL(url).getHost();
		// Sleep thread if last access time is less than cooldown time
		Long timeElapsed = (System.currentTimeMillis() - 
				_domainAccessTime.getOrDefault(host, 0L));
		while(timeElapsed < COOL_DOWN_INTERVAL){
			//System.out.println("["+this.threadID + "] Sleeping [" + url + "]");
			
			Thread.sleep(Math.max(0, COOL_DOWN_INTERVAL - timeElapsed));
			
			timeElapsed = (System.currentTimeMillis() - 
					_domainAccessTime.get(host));
		}
		
		Integer discoveryTime = hit.getFields()
				.get(FIELD_DISCOVERY_TIME)
				.getValue();
		
		try{
			System.out.println("["+this.threadID + "] Fetching [" + url + "]");
			
			_domainAccessTime.put(host, System.currentTimeMillis());
			Document doc = Jsoup.connect(url).get();
			
			System.out.println("["+this.threadID + "] Parsing [" + url + "]");
			
			String text = getPlainTextContent(doc);
			Collection<URL> outLinks = getLinksFromPage(doc);
			
			System.out.println("["+this.threadID + "] Buffering [" + url + "]");
			
			_elasticClient.loadData(url, doc.select("title").text(), text, outLinks);
		
			for(URL link : outLinks){
				_elasticClient.enqueue(getScore(text), link, ++discoveryTime);
			}
		}catch(Exception e){}
		
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
		Collection<URL> result = new HashSet<URL>();
		
		Elements elems = doc.select("body").select("a");
		
		for(org.jsoup.nodes.Element elem : elems){
			String href = elem.attr("href");
			URL url = null;
			try{
				url = URLCanonizer.getCanninizedURL(href);
				url = new URL("http", url.getHost(), url.getPath());
				result.add(url);
			}catch(Exception e){continue;}
		}
		return result;
	}
}
