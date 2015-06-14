package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;


public class RulesResult implements Serializable{
	
	private static final long serialVersionUID = 1803918281169246909L;
	
	public int nclasses;
	public double[] classesConfidence;
	public int[] classesNRules;
	public List<LacRule> rules;
	
	public RulesResult(int nclasses){
		this.nclasses = nclasses;
		this.classesConfidence = new double[this.nclasses];
		Arrays.fill(this.classesConfidence, 0.0f);
		
		this.classesNRules = new int[this.nclasses];
		Arrays.fill(this.classesNRules, 0);
		
		this.rules = new Vector<LacRule>();
	}
	
	public RulesResult assign(RulesResult rhs){
		this.nclasses = rhs.nclasses;
		this.classesConfidence = rhs.classesConfidence;
		this.classesNRules = rhs.classesNRules;
		
		return this;
	}
	
	public RulesResult increment(RulesResult rhs){
		for(int i = 0; i < this.nclasses; i++){
			this.classesConfidence[i] = rhs.classesConfidence[i];
			this.classesNRules[i] = rhs.classesNRules[i];
		}
		
		return this;
	}
	
	public void addRule(LacRule r){
		this.rules.add(r);
	}
	
	public double score(int classId){
		final double score = this.classesNRules[classId] == 0 ? 0.0f : 
			(double)classesConfidence[classId]/(double)(this.rules.size());
		
		return score;
	}
}