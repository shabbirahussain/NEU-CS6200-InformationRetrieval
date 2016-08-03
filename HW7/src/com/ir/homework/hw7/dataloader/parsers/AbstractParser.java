package com.ir.homework.hw7.dataloader.parsers;

public abstract class AbstractParser implements Parser{
	
	@Override
	public String cleanContent(String text){
		String result = text;
		result = result.replaceAll("\n", " ");
		result = result.replaceAll("\t", " ");
		result = result.replaceAll("(\\s)+", " ");
		
		return result;
	}

}
