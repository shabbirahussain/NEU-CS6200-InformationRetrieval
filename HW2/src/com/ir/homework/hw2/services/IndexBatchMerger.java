/**
 * 
 */
package com.ir.homework.hw2.services;

import static com.ir.homework.hw2.Constants.*;

import java.util.LinkedList;
import java.util.List;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.metainfo.MetaInfoController;

/**
 * This is responsible for updating indexes to create simpler index
 * @author shabbirhussain
 *
 */
public class IndexBatchMerger implements Runnable {
	private static final Long REFRESH_INTERVAL = 2L * 1000L;
	private MetaInfoController metaSynchronizer;
	
	/**
	 * Default constructor
	 * @param metaSynchronizer is the synchronizer object used for interprocess communication
	 */
	public IndexBatchMerger(MetaInfoController metaSynchronizer){
		this.metaSynchronizer = metaSynchronizer;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new IndexBatchMerger(new MetaInfoController(INDEX_ID))).run();
	}

	@Override
	public void run() {
		try {
			while(true){
				System.out.println("Batch merge session started...");
				
				mergeIndices();
				metaSynchronizer.save();
				
				System.out.println("Batch merge service sleeping...");
				Thread.sleep(REFRESH_INTERVAL);
			}
		} catch (Throwable e) {e.printStackTrace();}
	}
	

	/**
	 * Performs index merge with master file
	 * @throws Throwable
	 */
	private void mergeIndices() throws Throwable{
		List<Integer> oldUsableIndices = new LinkedList<Integer>(metaSynchronizer.getUsableIndices());
		if(oldUsableIndices.size()<MAX_ACTIVE_INDICES) return;
		
		IndexManager masterIM = metaSynchronizer.getIndexManager(oldUsableIndices.remove(0));
		for(Integer idxId : oldUsableIndices){
			System.out.println("Merging : " + idxId + " onto " + masterIM.getInstanceID());

			masterIM = masterIM.mergeIndices(idxId, BATCH_SIZE);
		}
		masterIM.finalize(false);
	}
}
