/**
 * 
 */
package com.ir.homework.hw1.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param objStorePath is the absolute path of object store including final separator
	 * @param storePath full path of directory where objects are stored
	 */
	public static void saveObject(Object object, String objStorePath){
		String fullFilePath = objStorePath + object.getClass().getName() + ".ser";
		
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
	public static Object getOrDefault(Object obj, String objStorePath){
		Object result = obj;
		String fullFilePath = objStorePath + obj.getClass().getName() + ".ser";
		try{
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullFilePath));
			result = ois.readObject();
			ois.close();
		}catch(ClassNotFoundException | IOException e){}
		
		return result;
	}
}
