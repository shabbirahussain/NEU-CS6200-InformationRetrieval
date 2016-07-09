package com.ir.homework.hw4.models;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class LinkInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public Collection<String> M; // In Links
	public Long L;               // Out links count
	/**
	 * Default constructor
	 */
	public LinkInfo(){
		M = new LinkedList<String>();
		L = 0L;
	}
}