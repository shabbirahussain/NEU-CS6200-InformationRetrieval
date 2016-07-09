/**
 * 
 */
package com.ir.homework.hw4.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	/**
	 * Saves a serializable object
	 * @param object object to be stored
	 * @param fullFilePath is the absolute path of object store
	 */
	public static void saveObject(Object object, String fullFilePath){
		//System.out.println("Saving to " + objStorePath);
		(new File(fullFilePath)).mkdirs();
		fullFilePath = getObjectFileName(object.getClass().getName(), fullFilePath);
		
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(fullFilePath));
			oos.writeObject(object);
			oos.close();
		} catch (IOException e) {e.printStackTrace();}
		return;
	}
	
	/**
	 * Given absolute path loads object
	 * @param c is the class of object to load
	 * @param fullFilePath is the full path of object to load
	 * @return Object loaded from location
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static Object get(Class c, String fullFilePath) throws FileNotFoundException, IOException, ClassNotFoundException{
		fullFilePath = getObjectFileName(c.getName(), fullFilePath);
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fullFilePath));
		Object result = ois.readObject();
		ois.close();
		return result;
	}
	
	/**
	 * Gets fully qualified file name for storage of object. Only one instance of object can be stored by class name. 
	 * @param name is the name of class
	 * @param objStorePath is the absolute path of object store including final separator
	 * @return Full file name for the object
	 */
	private static String getObjectFileName(String name, String objStorePath){
		String result = objStorePath + name + ".ser";
		return result;
	}
}
