package com.ir.homework.hw2.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ir.homework.hw2.indexers.CatalogManager.CatInfo;
import com.ir.homework.hw2.queryprocessing.QueryProcessor;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.stemmer.Stemmer;

import static com.ir.homework.hw2.Constants.*;

public class Evaluator {

	public static void main(String[] args) throws Exception {
		QueryProcessor qp = new QueryProcessor(INDEX_ID, "TEXT");
		Stemmer stemmer   = new PorterStemmer();

		BufferedWriter br = new BufferedWriter(new FileWriter(EVAL_OUTPUT_FILE));
		for(String term: getTerms()){
			String sTerm = stemmer.stem(term).toString();
			System.out.println("Processing.." + sTerm);
			
			
			Map<String, List<Long>> res = qp.getPositionVector(sTerm);
			Integer df = res.size();
			Long ttf = 0L;
			for(Entry<String, List<Long>> e : res.entrySet()){
				for(Long tf : e.getValue()){
					ttf += tf;
				}
			}
			
			//CatInfo catInfo = qp.getTermStatistics(sTerm);
			//Integer df = catInfo.df;
			//Long ttf   = catInfo.ttf;
			
			br.write(term + " " + df.toString() + " " + ttf.toString());
			br.newLine();
		}
	}
	
	private static List<String> getTerms() throws IOException{
		List<String> result = new LinkedList<String>();
 		BufferedReader br = new BufferedReader(new FileReader(EVAL_INPUT_FILE));
		String line=null;
		while((line=br.readLine())!=null){
			result.add(line.trim());
		}
		return result;
	}
}
