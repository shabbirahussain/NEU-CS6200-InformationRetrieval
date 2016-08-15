package cc.mallet.pipe;

import java.io.Serializable;

import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;

public class ARFF2FeatureVector extends Pipe implements Serializable  {
	private static final long serialVersionUID = 1L;

	public ARFF2FeatureVector() {
		// TODO Auto-generated constructor stub
	}

	
	public Instance pipe (Instance carrier) {

		String[] fields = carrier.getData().toString().split("\\s+");

		int numFields = fields.length;
		
		Object[] featureNames = new Object[numFields];
		double[] featureValues = new double[numFields];

		for (int i = 0; i < numFields; i++) {
			if (fields[i].contains("=")) {
				String[] subFields = fields[i].split("=");
				featureNames[i] = subFields[0];
				featureValues[i] = Double.parseDouble(subFields[1]);
			}
			else {
				featureNames[i] = fields[i];
				featureValues[i] = 1.0;
			}
		}

		carrier.setData(new FeatureVector(getDataAlphabet(), featureNames, featureValues));
		
		return carrier;
	}
	
}
