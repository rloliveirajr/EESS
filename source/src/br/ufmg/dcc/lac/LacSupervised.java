package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LacSupervised implements Serializable{

	private static final long serialVersionUID = 5001433886495733415L;
	
	public LacSupervised(){
	}
	
	public double[] predict(Training training, LacInstance test, AssociationRule associationRule, double[] scores, List<LacRule> rules){		
		SymbolTable featureTable = training.getFeaturesTable();
		
		String[] features = test.getFeatures();
		
		int nclasses = training.getNumberOfClasses();
		
		Set<Integer> featureIds = new HashSet<Integer>();
		for(String f: features){
			featureIds.add(featureTable.addName(f));
		}
		
		Projection projection = training.getProjection(featureIds);
		
		RulesResult result = associationRule.induceRules(projection, nclasses, 
				training.getClassesSupport());

		rules.addAll(result.rules);
		
		for(int i = 0; i < nclasses; i++){
			scores[i] = result.score(i);
		}
		
		return scores;		
	}
}
