/**
 * 
 */
package com.ir.homework.hw4.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Stores or retrieves previously stored object
 * @author shabbirhussain
 */
public abstract class ObjectStore {
	private static String objStorePath;
	
	static{
		try{
			//create a temp file
			File temp = File.createTempFile("temp-file-name", ".tmp"); 

			//Get tempropary file path
			String absolutePath = temp.getAbsolutePath();
			String tempFilePath = absolutePath.
					substring(0,absolutePath.lastIndexOf(File.separator));

			objStorePath = tempFilePath + File.separator;
		}catch(IOException e){e.printStackTrace();}
	}
	
	/**
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param storePath full path of directory where objects are stored
	 */
	public static void saveObject(Object object){
		saveObject(object, objStorePath);
	}
	
	/**
	 * Loads or gets default value of an object
	 * @param object default value of object if not found in store
	 * @return Uncasted object of given class fetched from store
	 */
	public static Object getOrDefault(Object object){
		return getOrDefault(object, objStorePath);
	}
	/**
	 * Clears the object from the store
	 * @param object is the object to be cleaned
	 * @return True if deletion is successful
	 */
	public static Boolean cleanObject(Object object){
		return cleanObject(object, objStorePath);
	}
	
	/**
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param objStorePath is the absolute path of object store including final separator
	 * @param storePath full path of directory where objects are stored
	 */
	public static void saveObject(Object object, String objStorePath){
		//System.out.println("Saving to " + objStorePath);
		
		String fullFilePath = getObjectFileName(object, objStorePath);
		
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fullFilePath));
			oos.writeObject(object);
			oos.close();
		} catch (IOException e) {e.printStackTrace();}
		return;
	}
	
	/**
	 * Loads or gets default value of an object
	 * @param object default value of object if not found in store
	 * @param objStorePath is the absolute path of object store including final separator
	 * @return Uncasted object of given class fetched from store
	 */
	public static Object getOrDefault(Object object, String objStorePath){
		Object result = object;
		String fullFilePath = getObjectFileName(object, objStorePath);
		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullFilePath));
			result = ois.readObject();
			ois.close();
		}catch(ClassNotFoundException | IOException e){}
		
		return result;
	}
	
	/**
	 * Clears the object from the store
	 * @param object is the object to be cleaned
	 * @param objStorePath is the absolute path of object store including final separator
	 * @return True if deletion is successful
	 */
	public static Boolean cleanObject(Object object, String objStorePath){
		String fullFilePath = getObjectFileName(object, objStorePath);
		try {
			Files.deleteIfExists(Paths.get(fullFilePath));
			return true;
		} catch (IOException e) {}
		return false;
	}
	
	/**
	 * Gets fully qualified file name for storage of object. Only one instance of object can be stored by class name. 
	 * @param object is the object to be stored
	 * @param objStorePath is the absolute path of object store including final separator
	 * @return Full file name for the object
	 */
	private static String getObjectFileName(Object object, String objStorePath){
		String result = objStorePath + object.getClass().getName() + ".ser";
		return result;
	}
}
