package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class Itemset implements Comparable<Itemset>, Serializable{
	
	private static final long serialVersionUID = -3444651129919839603L;
	
	int support;
	final public List<Integer> featureIds;
	public List<Integer> classesSupport;
	public List<Integer> tids;
	int classId;
	String[] minHash;
	String minHashStr;
	public int size;
	
	
	double utility = -1;
	public double relativeCompression = -1;
	
	double[] metrics;
	
	boolean pattern; 
	
	public Itemset(){
		this.support = 0;
		this.featureIds = new Vector<Integer>();
		this.classesSupport = new Vector<Integer>();
		this.tids = new Vector<Integer>();
	}
	
	Itemset(Itemset rhs){
		this.support = rhs.support;
		this.featureIds = new Vector<Integer>(rhs.featureIds);
		this.classesSupport = new Vector<Integer>(rhs.classesSupport);
		this.tids = new Vector<Integer>(rhs.tids);
	}
	
	public Itemset(Set<Integer> featuresIds){
		this.support = 0;
		this.featureIds = new Vector<Integer>(featuresIds);
		this.classesSupport = new Vector<Integer>();
		this.tids = new Vector<Integer>();
	}
		
	void addTid(int tid, int classId){
		this.support++;
		this.classId = classId;
		this.tids.add(tid);
		
		while(this.classesSupport.size() <= classId){
			this.classesSupport.add(0);
		}
		
		int classSupport = this.classesSupport.get(classId);
		classSupport++;
		this.classesSupport.set(classId, classSupport);
	}
	
	public int getSupport(){
		return this.support;
	}
	
	public int getSize(){
		return this.featureIds.size();
	}
	
	public int getSupport(int classId){
		return classId < classesSupport.size() ? this.classesSupport.get(classId) : 0;
	}
	
	double[] getMetrics(){
		return new double[]{featureIds.size(), tids.size()};
	}
		
	@Override
	public int compareTo(Itemset o) {		
		if(this.getSupport() == o.getSupport()){
			return this.featureIds.toString().compareTo(o.featureIds.toString());
		}
		
		return this.getSupport() < o.getSupport() ? -1 : 1;
	}
	
	@Override
	public String toString(){
		String out = featureIds.toString();
		
		return out;
	}

}
