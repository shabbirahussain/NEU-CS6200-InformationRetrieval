/**
 * 
 */
package com.ir.homework.hw2.metainfo;

import java.util.HashMap;
import java.util.List;
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
	 * @param instanceID is the unique index identifier
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
	
	
	
	/**
	 * Sets usable indices for future use
	 * @param instanceID is the unique index identifier
	 * @param indexIDList is the list of index id to be set as usable
	 */
	public static void setUsableIndices(String instanceID, List<Integer> indexIDList){
		MetaInfoController metaController = getMetaInfoController(instanceID);
		metaController.setUsableIndices(indexIDList);
		
		MetaInfoControllers.saveMetaInfo();
	}
	

	/**
	 * Adds new index to usable list. With data from higher index id considered more reliable.
	 * @param instanceID is the unique index identifier 
	 * @param indexID is the unique index number/ version number
	 */
	public static void addUsableIndex(String instanceID, Integer indexID){
		MetaInfoController metaController = getMetaInfoController(instanceID);
		metaController.addUsableIndex(indexID);
		
		MetaInfoControllers.saveMetaInfo();
	}
	
	/**
	 * Gets the next index file version ID
	 * @param instanceID is the unique index identifier 
	 * @return New index ID
	 */
	public static Integer getNextIndexID(String instanceID){
		MetaInfoController metaController = getMetaInfoController(instanceID);
		Integer result = metaController.getNextIndexID();
		
		MetaInfoControllers.saveMetaInfo();
		return result;
	}
	
	/**
	 * Returns the list of usable indices. A usable index is a index which has been successfully written to disk completely and is ready to use.
	 * @param instanceID is the unique index identifier 
	 * @return List of usable index
	 */
	public static List<Integer> getUsableIndices(String instanceID){
		MetaInfoController metaController = getMetaInfoController(instanceID);
		List<Integer> result = metaController.getUsableIndices();
		
		MetaInfoControllers.saveMetaInfo();
		return result;
	}
}
