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

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

import com.ir.homework.hw5.evaluators.AbstractEvaluator.Result;
import com.ir.homework.hw5.models.ModelQres;
import static com.ir.homework.hw5.Constants.*;


public class PRGraphEvaluator extends AbstractEvaluator {
	private static final Integer[] K_VAL = {5,10,15,20,30,100,200,500,1000};
	private static final Double[] RECALL_STD = new Double[]{0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
	private static Set<Double> recallSet;
	private static final DecimalFormat f = new DecimalFormat("###0");
	
	static{
		recallSet = new HashSet<Double>();
		for(Double d: RECALL_STD){
			recallSet.add(d);
		}
	}
	
	
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
			precn.add(totPrcn/cntPrcn);
			recll.add(totRcll/cntPrcn);
		}
		showGraph(recll, precn, title);
		
		// Interpolate the Graph
		List<Double> nPrecn = new LinkedList<Double>();
		List<Double> nRecll = new LinkedList<Double>();
		Double minPrecn = precn.get(0);
		Double p, r;
		
		for(int i=1; i<precn.size();i++){
			p = precn.get(i);
			r = recll.get(i);
			//if(recallSet.contains(r))
			if(minPrecn >= p){
				nPrecn.add(p);
				nRecll.add(r);
			}else{
				nPrecn.add(minPrecn);
				nRecll.add(r);
			}	
			minPrecn = Math.min(minPrecn, p);
		}
//		nPrecn.add(0.0);
//		nRecll.add(0.0);
		
		showGraph(nRecll, nPrecn, title + " (Interpolated)");
		

		// Interpolate data
//		LinearInterpolator li = new LinearInterpolator();
//		PolynomialSplineFunction fun = li.interpolate(xData, yData);
//		xData = fun.getKnots();
//		yData = fun.getPolynomials();
		
		
		
	}
	
	/**
	 * Plots graph given two lists
	 * @param xData
	 * @param yData
	 * @param title
	 */
	private void showGraph(List<Double> xData, List<Double> yData, String title){
//		double[] xData1 = Stream.of(yData.toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
//		double[] yData1 = Stream.of(xData.toArray(new Double[0])).mapToDouble(Double::doubleValue).toArray();
		
		title = "PRGraph: " + title;
		// Create Chart
		Chart chart = QuickChart.getChart(title, "Recall", "Precision", "y(x)", xData, yData);
		new SwingWrapper(chart).displayChart();
		
		// Save it
		try {
			BitmapEncoder.saveBitmap(chart, QPNG_PATH, BitmapFormat.PNG);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
