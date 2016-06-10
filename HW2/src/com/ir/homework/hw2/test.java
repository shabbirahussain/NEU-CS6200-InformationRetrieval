package com.ir.homework.hw2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ir.homework.hw2.metainfo.MetaInfoController;

class Model{
	public Integer i;
	public List<Integer> l;
	public Model(){
		this.i=0;
		this.l = new LinkedList<Integer>();
	}
	public synchronized Integer getNext(){
		return this.i++;
	}
	
	public synchronized List<Integer> addItem(Integer i){
		this.l.add(i);
		return l;
	}
}
public class test {
	
	public class myThread extends Thread{
		private MetaInfoController s;
		private Integer id;
		
		public myThread(Integer id, MetaInfoController s){
			this.id = id;
			this.s = s;
		}
		
		@Override
		public void run(){
			try{
				while(true){
					Integer i = this.s.getNextIndexID();
					this.s.setUsable(i);
					System.out.println(this.id + " => " + i + "\t" + this.s.getUsableIndices());
					Thread.sleep(2000);
				}
			}catch(Exception e){}
		}
	}
	
	public static void main(String[] args) {
		(new test()).testq();
	}
	
	private void testq(){
		MetaInfoController s = new MetaInfoController("test");
		(new myThread(1, s)).start();
		(new myThread(2, s)).start();

	}
	

}
