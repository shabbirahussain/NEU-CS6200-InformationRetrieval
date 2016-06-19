/**
 * 
 */
package com.ir.homework.hw3.frontier;

import com.ir.homework.hw3.elasticclient.ElasticClient;

/**
 * @author shabbirhussain
 *
 */
public final class FrontierTruncator extends Thread{
	private ElasticClient ec;
	private Long trunctionInterval;
	
	/**
	 * Default constructor
	 * @param ec is the elastic client to use
	 * @param trunctionInterval is the truncation interval
	 */
	public FrontierTruncator(ElasticClient ec, Long trunctionInterval){
		this.ec = ec;
		this.trunctionInterval = trunctionInterval;
	}
	
	@Override
	public void run() {
		try{
			System.out.println("Truncating additional queue...");
			this.ec.truncateQueue();
			Thread.sleep(trunctionInterval);
		}catch(Exception e){}
	}
	

}
