package com.ir.homework.hw5.evaluators;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

import com.ir.homework.hw5.evaluators.AbstractEvaluator.Result;
import com.ir.homework.hw5.models.ModelQres;
import static com.ir.homework.hw5.Constants.*;


public class PRGraphEvaluator extends AbstractEvaluator {
	public static final Integer[] K_VAL = {5,10,15,20,30,100,200,500,1000};
	private static final DecimalFormat f = new DecimalFormat("###0");
	
	
	public void execute(PrintStream out) {
		List<Double> precn = new LinkedList<Double>();
		List<Double> recll = new LinkedList<Double>();
		
		Integer k = 0;
		Set<String> doneQ = new HashSet<String>();
		String title = null;
		while(doneQ.size() != qres.keySet().size()){
			Double totPrcn = 0.0;
			Double totRcll = 0.0;
			Double cntPrcn = 0.0;
			
			for(Entry<String, ModelQres> query: qres.entrySet()){
				if(title==null) 
					title = query.getKey();
				else if(title!=query.getKey())  
					title = "Summary";
				
				List<Result> qResult = super.resultMap.get(query.getKey());
				if(k<qResult.size()){
					totPrcn += qResult.get(k).prcisn;
					totRcll += qResult.get(k).recall;
					cntPrcn++;
				}
				else doneQ.add(query.getKey());
				
				k++; // go to next index in sparse matrix
			}
			//cntPrcn = Math.max(1.0, cntPrcn);
			precn.add(totPrcn/cntPrcn);
			recll.add(totRcll/cntPrcn);
		}
		System.out.println("precn:"+precn);
		System.out.println("recall:"+recll);
		
		double[] xData = Stream.of(recll.toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
		double[] yData = Stream.of(precn.toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
		
		title = "PRGraph: " + title;
		
		// Create Chart
		Chart chart = QuickChart.getChart(title, "Recall", "Precision", "y(x)", xData, yData);
		
		// Show it
		new SwingWrapper(chart).displayChart();
		
		// Save it
		try {
			BitmapEncoder.saveBitmap(chart, QPNG_PATH, BitmapFormat.PNG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
