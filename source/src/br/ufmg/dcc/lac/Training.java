package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.List;


public class Training implements Serializable{
	
	private static final long serialVersionUID = 6846815681748470029L;

	private List<Integer> classesSupport;
	private List<Integer> tidClassMap;
	private Map<Integer, Itemset> itemsets;
	private int tidCount;
	
	private SymbolTable featuresTable;
	private SymbolTable classesTable;
	
	private int cacheSize;
	
	public Training(){
		this.tidCount = 0;
		this.itemsets = new TreeMap<Integer, Itemset>();
		this.tidClassMap = new Vector<Integer>();
		this.classesSupport = new Vector<Integer>();
		this.featuresTable = SymbolTable.featuresTable();
		this.classesTable = SymbolTable.classesTable();
		this.cacheSize = 0;
	}
	
	public Training(int cacheSize){
		this.tidCount = 0;
		this.itemsets = new TreeMap<Integer, Itemset>();
		this.tidClassMap = new Vector<Integer>();
		this.classesSupport = new Vector<Integer>();
		this.featuresTable = SymbolTable.featuresTable();
		this.classesTable = SymbolTable.classesTable();
		this.cacheSize = cacheSize;
	}
	
	public void addTransaction(LacInstance instance){
		
		int classId = this.classesTable.addName(instance.getLabel());
		
		Set<Integer> featuresIds = new TreeSet<Integer>();
		for(String feature : instance.getFeatures()){
			int featureId = this.featuresTable.addName(feature);
			featuresIds.add(featureId);
		}
		
		instance.setFeaturesIndexed(featuresIds);
		instance.setIndexedClass(classId);
		instance.trueClass = classId;
		
		while(this.classesSupport.size() <= classId){
			this.classesSupport.add(0);
		}	

		int classSupport = this.classesSupport.get(classId);
		classSupport++;
		this.classesSupport.set(classId, classSupport);
		
		this.tidClassMap.add(classId);
			
		for(Integer fid : featuresIds){
			Itemset itemset = itemsets.get(fid);
			if(itemset == null){
				itemset = new Itemset();
				itemset.featureIds.add(fid);
				itemset.addTid(tidCount, classId);
				itemsets.put(fid, itemset);
			}else{
				itemset.addTid(tidCount, classId);
			}
		}
		
		tidCount++;
	}
	
	public void addTransactions(List<LacInstance> transactions){
		for(LacInstance i : transactions){
			this.addTransaction(i);
		}
	}
	
	Projection getProjection(Set<Integer> featureIds){
		Projection projection = new Projection(this.cacheSize);
		
		for(Integer fid : featureIds){
			Itemset itemset = this.itemsets.get(fid);
			if(itemset != null){
				projection.itemsets.add(itemset);
			}
		}
		
		Collections.sort(projection.itemsets);
		
		return projection;
	}
	
	public int getNumberOfClasses(){
		return this.classesSupport.size();
	}
	
	public int getMostFrequentClass(){
		Integer mostValue = Collections.max(this.classesSupport);
		
		for(int mostFrequentClass = 0; mostFrequentClass < this.classesSupport.size(); mostFrequentClass++){
			if(this.classesSupport.get(mostFrequentClass) == mostValue){
				return mostFrequentClass;
			}
		}
		
		return -1;
	}
	
	public double getClassProb(int i){
		double probs = (double)this.classesSupport.get(i)/(double)tidCount;
		
		return probs;
	}
	
	public int getClassSupport(int i){
		return this.classesSupport.get(i);
	}
	
	public List<Integer> getTidClassMap(){
		return this.tidClassMap;
	}
	
	List<Integer> getClassesSupport(){
		return this.classesSupport;
	}
	
	public SymbolTable getFeaturesTable(){
		return this.featuresTable;
	}
	
	public SymbolTable getClassesTable(){
		return this.classesTable;
	}
	
	String ruleToString(LacRule r){
		String out = "";
		
		for(int f : r.getFeaturesIds()){
			String label = "";
			label = this.featuresTable.getName(f);
			
			out += label + ",";
		}

		out += this.classesTable.getName(r.getIndexedClass()) + " (" + Arrays.toString(r.getMetrics()) + ")";

		
		return out;
	}
	
	String instanceToString(LacInstance inst){
		String instance = "%s CLASS=%s %s";
		String features = Arrays.toString(inst.getFeatures());
		features = features.replace("[", "");
		features = features.replace(", ", " ");
		features = features.replace("]", "");
		
		String tid = "" + inst.tid;
		
		instance = String.format(instance, tid, this.classesTable.getName(inst.getIndexedClass()), features);
		
		return instance;
	}
	
	public void clear(){
		this.tidCount = 0;
		this.itemsets.clear();
		this.tidClassMap.clear();
		this.classesSupport.clear();
		this.featuresTable.clear();
		this.classesTable.clear();
	}
}
