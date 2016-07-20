package com.ir.homework.hw5;

public class Test {

	public static void main(String args[]){
		long t;
		t = System.nanoTime();
		if(true);
		System.out.println(System.nanoTime() - t);
		
		t = System.nanoTime();
		if(1==1);
		System.out.println(System.nanoTime() - t);
		
		t = System.nanoTime();
		if(true);
		System.out.println(System.nanoTime() - t);
	}
	
}
