package cc.mallet.pipe.iterator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.regex.Pattern;

import cc.mallet.types.Instance;

public class ArffIterator implements Iterator<Instance>{
	private LineNumberReader reader;
	private String currentLine;
	
	
	public ArffIterator(Reader input) {
		this.reader = new LineNumberReader (input);
		
		try {
			currentLine = reader.readLine();
			while(!currentLine.startsWith("@DATA")){
				
			}
			
			
			
		} catch (IOException e) {
			throw new IllegalStateException ();
		}
	}

	public ArffIterator(String filename)throws java.io.FileNotFoundException{
		this (new FileReader (new File(filename)));
	}
	
	public Instance next() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasNext() { return currentLine != null; }

	public void remove () {
		throw new IllegalStateException ("This Iterator<Instance> does not support remove().");
	}
}
