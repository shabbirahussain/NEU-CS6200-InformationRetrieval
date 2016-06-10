/**
 * 
 */
package com.ir.homework.hw2.services;

import java.util.List;

import com.ir.homework.hw2.io.Saveable;

/**
 * @author shabbirhussain
 *
 */
public final class ShutdownHandler implements Runnable {
	private List<Saveable> savableList;
	
	public ShutdownHandler(List<Saveable> savableList){
		this.savableList = savableList;
	}
	
	@Override
	public void run() {
		for(Saveable object : savableList){
			object.save();
		}
	}
	
}
