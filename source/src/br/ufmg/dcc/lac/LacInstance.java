package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

public class LacInstance implements Serializable{

	private static final long serialVersionUID = 8253190357624368044L;

	public int tid;
	public String label;
	private int indexedClass;
	public int trueClass;
	private final String[] features;
	private Set<Integer> featuresIndexed;
	private int nRules;
		
	public LacInstance(int tid, String[] features, String label){
		this.tid = tid;
		
		Arrays.sort(features);
		this.features = features;
		
		this.label = label;
	}

	protected String getLabel() {
		return label;
	}

	public int getIndexedClass() {
		return indexedClass;
	}

	public String[] getFeatures() {
		return features;
	}

	public Set<Integer> getFeaturesIndexed() {
		return featuresIndexed;
	}
	
	protected void setFeaturesIndexed(Set<Integer> indexedFeatures){
		this.featuresIndexed = indexedFeatures;
	}
	
	public void setIndexedClass(int indexedClass){
		this.indexedClass = indexedClass;
	}
	
	protected int getTimestamp(){
		return this.tid;
	}
		
	public void setNRules(int nRules){
		this.nRules = nRules;
	}
	
	public int getNRules(){
		return this.nRules;
	}
}
