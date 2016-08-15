package com.ir.homework.hw8;

import java.io.File;

import cc.mallet.examples.TopicModel;

public final class Executor {
	
	
	public static void main(String[] args) throws Exception {
		System.out.println(new File("stoplists/en.txt").getAbsoluteFile());
		TopicModel.main(new String[]{"/Users/shabbirhussain/Downloads/ap.txt"});
        
	}
}
