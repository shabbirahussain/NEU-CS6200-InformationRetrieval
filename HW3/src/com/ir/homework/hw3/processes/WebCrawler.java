package com.ir.homework.hw3.processes;

import static com.ir.homework.hw3.Constants.COOL_DOWN_INTERVAL;
import static com.ir.homework.hw3.Constants.DEQUEUE_SIZE;
import static com.ir.homework.hw3.Constants.FIELD_DISCOVERY_TIME;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.elasticsearch.search.SearchHit;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;

import com.ir.homework.hw3.elasticclient.ElasticClient;
import com.ir.homework.hw3.tools.DefaultTokenizer;
import com.ir.homework.hw3.tools.WebPageParser;
import com.ir.homework.hw3.tools.WebPageParser.ParsedWebPage;

public class WebCrawler extends Thread {
	private static WebPageParser     _webPageParser;
	private static Map<String, Long> _domainAccessTime;

	private Collection<String> queryTerms;
	private ElasticClient      elasticClient;
	private DefaultTokenizer   tokenizer;
	
	static{
		_domainAccessTime = new ConcurrentHashMap<String, Long>();
		_webPageParser    =  new WebPageParser();
	}
	
	/**
	 * Default constructor which initialized the thread
	 * @param elasticClient is the elastic client to use
	 * @param tokenizer is the tokenizer to use for scoring
	 * @param queryTerms are the collection of query terms for scoring
	 */
	public WebCrawler(ElasticClient elasticClient, DefaultTokenizer tokenizer, Collection<String> queryTerms) {
		this.elasticClient  = elasticClient;
		this.tokenizer      = tokenizer;
		this.queryTerms     = queryTerms;
	}

	@Override
	public void run(){
		while(true){
			try {
				this.log("Dequeing...");
				SearchHit[] queue = elasticClient.dequeue(DEQUEUE_SIZE);
				
				if(queue.length == 0) {
					this.log("Flusing buffer...");
					elasticClient.flush();
					Thread.sleep(2000);
				}
				
				for(SearchHit hit : queue){
					crawl(hit);
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	

	/**
	 * Executes iteration
	 * @throws Exception
	 */
	private void crawl(SearchHit hit) throws Exception{
		String url  = hit.getId();
		Integer discoveryTime = hit.getFields()
				.get(FIELD_DISCOVERY_TIME)
				.getValue();
		

		String host = new URL(url).getHost();
		Long timeElapsed = (System.currentTimeMillis() - _domainAccessTime.getOrDefault(host, 0L));
		while(timeElapsed < COOL_DOWN_INTERVAL){
			Thread.sleep(COOL_DOWN_INTERVAL - timeElapsed);
			timeElapsed = (System.currentTimeMillis() - _domainAccessTime.get(host));
		}
		_domainAccessTime.put(host, System.currentTimeMillis());
		this.log("Fetching [" + url + "]");
		
		try{
			Response response = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2")
					.followRedirects(true)
					.execute();
			
			ParsedWebPage parsedWebPage = _webPageParser.parseResponse(response);
			
			elasticClient.loadData(url, parsedWebPage);
		
			for(URL link : parsedWebPage.outLinks){
				elasticClient.enqueue(getScore(parsedWebPage.text), link, ++discoveryTime);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Given list of terms gives a document score
	 * @param document is the text of document to score
	 * @return
	 */
	private Float getScore(String document) {
		Map<String, Float> tfMap = new HashMap<String, Float>();
		
		for(String term : this.tokenizer.tokenize(document)){
			tfMap.put(term, tfMap.getOrDefault(term, 0F) + 1);
		}
		
		Float okapi_tf = 0F;
		for(String term: queryTerms){
			Float tf_w_d    = tfMap.getOrDefault(term, 0F);
			Float len_d     = ((Integer) tfMap.size()).floatValue();
			Float avg_len_d = 10000F; //_elasticClient.getAvgDocLen();
			
			okapi_tf += (float) (tf_w_d / (tf_w_d + 0.5 + 1.5*(len_d/avg_len_d)));
		}
		
		return okapi_tf;
	}
	
	/**
	 * Logs messages to default stream
	 * @param message is the string message to log
	 */
	private void log(String message){
		System.out.println("["+(new Date())+"]"
				+ "[" + this.getId() + "] " 
				+ message );
	}

}
