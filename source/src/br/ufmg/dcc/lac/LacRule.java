package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LacRule implements Comparable<LacRule>, Serializable{

	private static final long serialVersionUID = -339109421235405187L;

	private final List<Integer> featuresIds;
	private final int indexedClass;
	private final Metrics metrics;
	
	LacRule(List<Integer> featuresIds, int indexedClass, Metrics metrics){
		this.featuresIds = featuresIds;
		this.indexedClass = indexedClass;
		this.metrics = metrics;
	}

	public List<Integer> getFeaturesIds() {
		return featuresIds;
	}

	public int getIndexedClass() {
		return indexedClass;
	}

	public double[] getMetrics() {
		return metrics.getMetrics();
	}
	
	public String toString(){
		String r = featuresIds.toString() + " -> " + indexedClass + "(" + this.metrics.confidence() + ")";
		
		return r;
	}

	@Override
	public int compareTo(LacRule o) {
		if(featuresIds.size() == o.featuresIds.size()){
			if(this.indexedClass == o.indexedClass){
				Set<Integer> intersect = new HashSet<Integer>();
				intersect.addAll(featuresIds);
				intersect.removeAll(o.featuresIds);
				
				if(intersect.size() == 0){
					return 0;
				}else{
					return 1;
				}
				
			}else{
				return (this.indexedClass - o.indexedClass);
			}			
		}		
		
		return (featuresIds.size() - o.featuresIds.size());
	}
}
