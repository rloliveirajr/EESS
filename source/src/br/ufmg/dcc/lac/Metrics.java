package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Metrics implements Serializable{
	
	private static final long serialVersionUID = 4313559842157606354L;
	
	double omega;
	double antecedentSupport;
	double classSupport;
	double ruleSupport;
	
	List<Integer> classesSupport;
	
	double anb,nanb,nab;
	
	Metrics(List<Integer> classesSupport){
		this.classesSupport = classesSupport;
		
		omega = 0;
		
		for(int i = 0; i < classesSupport.size(); i++){
			omega += classesSupport.get(i);
		}
	}
	
	void setAntecedentSupport(double pa){
		this.antecedentSupport = pa + Util.EPS;
	}
	
	void setRuleSupport(double pab, int classID){
		this.ruleSupport = pab;
		this.classSupport = this.classesSupport.size() < classID+1 ? 0 : this.classesSupport.get(classID);
		
		this.anb = this.antecedentSupport - this.ruleSupport;
		this.nanb = this.omega - this.classSupport - anb;
	}
	
	double confidence(){
		return this.ruleSupport/this.antecedentSupport;
	}

	double support(){
		 return this.ruleSupport/this.omega;
	}

	double addedValue() {
		return (confidence() - (this.classSupport/omega));
	}

	double certainty(){
		return (this.ruleSupport/this.antecedentSupport - this.classSupport/omega)/(Util.EPS + 1. - this.classSupport/omega);
	}

	double yulesQ(){
		return  (this.ruleSupport*nanb - anb*nab)/(Util.EPS + this.ruleSupport*nanb + anb*nab);
	}

	double yulesY(){
		return (Math.sqrt(this.ruleSupport*nanb) - Math.sqrt(anb*nab))/(Util.EPS + Math.sqrt(this.ruleSupport*nanb) + Math.sqrt(anb*nab));
	}

	double strengthScore(){
		double nb = omega - this.classSupport;
		return this.ruleSupport/(this.classSupport + Util.EPS) * confidence() / (Util.EPS + anb/nb);
	}

	double weightedRelativeConfidence() {
		return (confidence() - this.classSupport/omega)*this.antecedentSupport/omega;
	}
	
	double[] getMetrics(){
		double[] metrics = new double[2];
		metrics[0] = confidence();
		metrics[1] = support();
//		metrics[2] = addedValue();
//		metrics[3] = certainty();
////		metrics[4] = yulesQ();
////		metrics[5] = yulesY();
//		metrics[4] = strengthScore();
//		metrics[5] = weightedRelativeConfidence();
		
		return metrics;
	}
	
	public String toString(){
		return Arrays.toString(getMetrics());
	}
}
