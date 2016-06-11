package com.ir.homework.hw2.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
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
		System.out.println("Processing..");
		
		BufferedWriter br = new BufferedWriter(new FileWriter(EVAL_OUTPUT_FILE));
		Integer i = 0, j = 0;
		Long time = System.nanoTime();
		Collection<String> terms = getTerms();
		for(String term: terms){
			String sTerm = stemmer.stem(term).toString();
			//System.out.println("Processing.." + sTerm);
			
			
			/*Map<String, List<Long>> res = qp.getPositionVector(sTerm);
			Integer df = res.size();
			Long ttf = 0L;
			for(Entry<String, List<Long>> e : res.entrySet()){
				for(Long tf : e.getValue()){
					ttf += tf;
				}
			}*/
			
			CatInfo catInfo = qp.getTermStatistics(sTerm);
			Integer df = catInfo.df.intValue();
			Long ttf   = catInfo.ttf;
			
			br.write(term + " " + df.toString() + " " + ttf.toString());
			br.newLine();
			if(i==200) {
				i=0;
				System.out.println((j*100.0/terms.size()) 
						+ "% done [" + j + "/" + terms.size() + "]"
						+ (1.0e-9 *(System.nanoTime()-time)) + "s elapsed");
			}i++;j++;
		}
		br.close();//4598649655
		System.out.println("time =" + (1.0e-9 *(System.nanoTime()-time)));
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
