package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Util implements Serializable{

	private static final long serialVersionUID = 3224393217454876603L;
	
	public static final double EPS = 1e-18;
	
	public static void calcIntersection(List<Integer> a, List<Integer> b){
		int aBegin = 0;
		
		int bBegin = 0;

		while(aBegin < a.size()){
			
			while(bBegin < b.size() && b.get(bBegin).intValue() < a.get(aBegin).intValue()){
				bBegin++;
			}
			
			if(bBegin < b.size() && a.get(aBegin).intValue() == b.get(bBegin).intValue()){
				aBegin++;
			}else{
				a.remove(aBegin);
			}
		}
	}
	
	public static int[] ruleCoverage(List<LacInstance> instances, List<LacRule> rules){
		int[] coverage = new int[instances.size()];
		Arrays.fill(coverage, 0);
		
		for(int i = 0; i < instances.size(); i++){
			LacInstance instance = instances.get(i);
			List<Integer> features = new Vector<Integer>(instance.getFeaturesIndexed());
			List<Integer> f = new Vector<Integer>(instance.getFeaturesIndexed());
			int indexedClass = instance.getIndexedClass();			
			
			for(LacRule r : rules){
				f.clear();
				f.addAll(features);
				
				List<Integer> ruleFeatures = r.getFeaturesIds();
				
				int indexedRuleClass = r.getIndexedClass();
				Util.calcIntersection(f, ruleFeatures);
				
				if(indexedClass == indexedRuleClass && f.size() == ruleFeatures.size()){
					coverage[i] += 1;
				}
			}
		}
	
		return coverage;
	}
	
	public static double[] coverageRank(List<LacInstance> instances, List<LacRule> rules){
		double[] coverage = new double[instances.size()];
		Arrays.fill(coverage, 0);
		
		for(int i = 0; i < instances.size(); i++){
			LacInstance instance = instances.get(i);
			List<Integer> features = new Vector<Integer>(instance.getFeaturesIndexed());
			List<Integer> f = new Vector<Integer>(instance.getFeaturesIndexed());
			int indexedClass = instance.getIndexedClass();			
			
			for(LacRule r : rules){
				f.clear();
				f.addAll(features);
				
				List<Integer> ruleFeatures = r.getFeaturesIds();
				
				int indexedRuleClass = r.getIndexedClass();
				Util.calcIntersection(f, ruleFeatures);
				
				if(indexedClass == indexedRuleClass && f.size() == ruleFeatures.size()){
					coverage[i] += 1;
				}
			}
		}
		
		for(int i = 0; i < instances.size(); i++){
			coverage[i] /= (double)rules.size();
		}
		
		return coverage;
	}
}
