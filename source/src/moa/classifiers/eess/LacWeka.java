package moa.classifiers.eess;

import java.util.List;
import java.util.Vector;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Utils;
import br.ufmg.dcc.lac.SymbolTable;
import br.ufmg.dcc.lac.Training;

public abstract class LacWeka {

	public static EessInstance populateInstance(int timestamp, Instance wekaInstance, boolean considerFeaturesPositions) {
		String label = "";
		List<String> featuresList = new Vector<String>();

		int numAtts = wekaInstance.numAttributes();
		int nvalidAtts = 0;

		for (int i = 0; i < numAtts; i++) {
			if (i != wekaInstance.classIndex() && !wekaInstance.isMissing(i)) {
				String feature = wekaInstance.toString(i);
				
				if (!considerFeaturesPositions) {
					feature = String.format("w[%d]=%s", i, feature);
				}else{
					feature = String.format("w=%s", feature);
				}
				featuresList.add(feature);

				nvalidAtts++;
			} else {
				label = wekaInstance.classAttribute().value((int)wekaInstance.classValue());
			}
		}

		String[] features = featuresList.toArray(new String[nvalidAtts]);

		EessInstance instance = new EessInstance(timestamp, features, label);

		return instance;
	}
	
	public static void calcProbability(Training window, Attribute classAttribute, double[] scores, double[] probs){
		SymbolTable classesTable = window.getClassesTable();
		
		double normalizationFactor = scores[0];
		
		for(int classId = 0; classId < window.getNumberOfClasses(); classId++){
			String label = classesTable.getName(classId);

			int wekaClassId = classAttribute.indexOfValue(label);
			
			probs[wekaClassId] = scores[classId];
			if(Double.compare(normalizationFactor, scores[classId]) > 0){
				normalizationFactor = scores[classId];
			}
		}

		if(Double.compare(normalizationFactor, 0.0) == 0){
			int defaultClass = window.getMostFrequentClass();
			
			String label = classesTable.getName(defaultClass);
			
			int wekaClassId = classAttribute.indexOfValue(label);
			
			probs[wekaClassId] = window.getClassProb(defaultClass);	
			
			scores[defaultClass] = probs[wekaClassId];
		}
		
		Utils.normalize(probs);
	}
}
