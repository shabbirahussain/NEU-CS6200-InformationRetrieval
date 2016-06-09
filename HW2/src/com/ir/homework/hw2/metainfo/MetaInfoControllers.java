/**
 * 
 */
package com.ir.homework.hw2.metainfo;

import java.util.HashMap;
import java.util.Map;

import com.ir.homework.hw2.io.ObjectStore;

/**
 * Gets or retries meta information. A builder controller for meta info
 * @author shabbirhussain
 *
 */
public abstract class MetaInfoControllers {
	private static Map<String, MetaInfoController> metaControllers = new HashMap<String, MetaInfoController>();
	
	/**
	 * Gets the controller of meta information
	 * @return MetaInfoController
	 */
	public static MetaInfoController getMetaInfoController(String instanceID){
		metaControllers = (Map<String, MetaInfoController>) ObjectStore.getOrDefault(metaControllers);
		
		MetaInfoController metaController = new MetaInfoController();
		metaController = metaControllers.getOrDefault(instanceID, metaController);
		metaControllers.put(instanceID, metaController);
		
		return metaController;
	}
	
	/**
	 * Stores meta information back to disk
	 */
	public static void saveMetaInfo(){
		ObjectStore.saveObject(metaControllers);
	}

}
