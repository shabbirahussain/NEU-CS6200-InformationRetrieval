/**
 * 
 */
package com.ir.homework.hw2.services;

import static com.ir.homework.hw2.Constants.*;

import java.util.LinkedList;
import java.util.List;

import com.ir.homework.hw2.indexers.IndexManager;
import com.ir.homework.hw2.metainfo.MetaInfoController;
import com.ir.homework.hw2.metainfo.MetaInfoControllers;

/**
 * This is responsible for updating indexes to create simpler index
 * @author shabbirhussain
 *
 */
public class IndexUpdator implements Runnable {
	private static final Long REFRESH_INTERVAL = 1L * 1000L;
	private static MetaInfoController metaInfoController;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		(new IndexUpdator()).run();
	}

	@Override
	public void run() {
		try {
			while(true){
				System.out.println("Batch merge session started...");
				
				metaInfoController = MetaInfoControllers.getMetaInfoController(INDEX_ID);
				mergeIndices();
				MetaInfoControllers.saveMetaInfo();
			
				System.out.println("Batch merge service sleeping...");
				Thread.sleep(REFRESH_INTERVAL);
			}
		} catch (Throwable e) {e.printStackTrace();}
	}
	

	/**
	 * Performs index merge with master file
	 * @throws Throwable
	 */
	private static void mergeIndices() throws Throwable{
		List<Integer> oldUsableIndices = MetaInfoControllers.getUsableIndices(INDEX_ID);
		List<Integer> newUsableIndices = new LinkedList<Integer>();
		
		IndexManager masterIM = getIndexManager(oldUsableIndices.remove(0));
		for(Integer idxId : oldUsableIndices){
			System.out.println("Merging : " + idxId + " onto " + masterIM.getInstanceID());

			masterIM = masterIM.mergeIndices(idxId, BATCH_SIZE, ENABLE_AUTO_CLEAN);
		}
		masterIM.finalize(false);
		
		newUsableIndices.add(masterIM.getInstanceID());
		MetaInfoControllers.setUsableIndices(INDEX_ID, newUsableIndices);
	}
	
	/**
	 * Gets the index manager for the given index version
	 * @param idxVer is the version of index
	 * @return IndexManager
	 */
	private static IndexManager getIndexManager(Integer idxVer){
		return (new IndexManager(INDEX_ID, idxVer));
	}
}
