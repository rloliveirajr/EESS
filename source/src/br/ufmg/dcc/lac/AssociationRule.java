package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.List;


public abstract class AssociationRule implements Serializable{

	private static final long serialVersionUID = -6350467102728932961L;

	protected SymbolTable featuresTable;
	protected SymbolTable classesTable;
	
	protected List<Integer> tidClassMap;
	protected static int maxRules = 512000;
	protected int totalRules;
		
	abstract RulesResult induceRules(Projection projection, int nclasses, List<Integer> pclassesSupport);
	
	public void setTidClassMap(List<Integer> tidClassMap){
		this.tidClassMap = tidClassMap;
	}
	
	public void setFeaturesTalbe(SymbolTable featuresTable){
		this.featuresTable = featuresTable;
	}
	
	public void setClassTable(SymbolTable classTable){
		this.classesTable = classTable;
	}
	
	protected void getKey(List<Itemset> itemsets, List<Integer> keys){
		Itemset item = itemsets.get(0);
		
		for(Integer id : item.featureIds){
			keys.add(id);
		}
		
		for(Itemset i : itemsets){
			int nfeatures = i.featureIds.size();
			Integer lastElement = i.featureIds.get(nfeatures - 1);
			
			keys.add(lastElement);
		}
	}
	
	protected void ruleToString(Itemset itemset, int classId, int support, double confidence) throws Exception{
		throw new Exception("Method not implemented");
	}
}
