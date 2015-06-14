package br.ufmg.dcc.lac;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Vector;


public class Eclat extends AssociationRule{

	private static final long serialVersionUID = -7344475748563366757L;

	protected double minConfidence;
	protected double minSupport;
	protected int maxRuleSize;
	
	public Eclat(double pminConfidence, double pminSupport, int pmaxRuleSize) {
		this.minConfidence = pminConfidence;
		this.minSupport = pminSupport;
		this.maxRuleSize = pmaxRuleSize;
	}

	@Override
	RulesResult induceRules(Projection projection, int nclasses, 
			List<Integer> pclassesSupport) {
		RulesResult result = new RulesResult(nclasses);
		
		if(projection.itemsets.size() == 0){
			return result;
		}
		
		Map<Integer, Itemset> database = new TreeMap<Integer, Itemset>();
		
		minSupport = (int)(projection.getSize() * this.minSupport + 0.5f);
		
		List<Integer> keys = new Vector<Integer>();
		
		totalRules = 0;
		
		this.getKey(projection.itemsets, keys);
		
		if(maxRuleSize == 1){
			return result;
		}
		
		List<Itemset> equiClass = new Vector<Itemset>();
		
		for(Itemset itemset : projection.itemsets){
			if(itemset.getSupport() < minSupport) {
				continue;
			}
			
			database.put(itemset.featureIds.get(0), itemset);
			Itemset newItem = new Itemset(itemset);
			equiClass.add(newItem);
		}
		
		Queue<List<Itemset>> equivalenceClasses = new ArrayDeque<List<Itemset>>();
		
		equivalenceClasses.offer(equiClass);
		
		this.induceRules(equivalenceClasses, nclasses, database, pclassesSupport, result);
		
		return result;
	}
	
	private void induceRules(Queue<List<Itemset>> equivalenceClasses, int nclasses, Map<Integer, Itemset> database, List<Integer> pclassesSupport, RulesResult result){
		List<Itemset> equiClass;
		
		while(!equivalenceClasses.isEmpty()){
			equiClass = equivalenceClasses.remove();
			
			int equiClassSize = equiClass.size();
			
			for(int it = 0; it < equiClassSize; it++){
				Itemset i = equiClass.get(it);
				
				if(i.getSupport() < 1){
					continue;
				}
				
				
				for(int c = 0; c < nclasses; c++){
					Metrics metrics = new Metrics(pclassesSupport);
					metrics.setAntecedentSupport(i.getSupport());
					metrics.setRuleSupport(i.getSupport(c), c);
					double confidence = metrics.confidence();
					
					if(confidence > this.minConfidence){
						LacRule r = new LacRule(i.featureIds, c, metrics);
						
						result.addRule(r);
	
						result.classesConfidence[c] += confidence;
						result.classesNRules[c] += 1;
						totalRules++;
					}
				}
				
				if(totalRules >= maxRules - 1){
					break;
				}
				
				if(maxRuleSize == i.featureIds.size() + 1){
					continue;
				}
				
				List<Itemset> newEquiClass = new Vector<Itemset>();
				
				int jt = it;
				
				for(++jt; jt < equiClassSize; jt++){
					Itemset j = equiClass.get(jt);
					Itemset d = this.getItemset(i, j, database);
					
					if(d.getSupport() >= minSupport){
						newEquiClass.add(d);
					}else{
						d = null;
					}
				}
				
				if(newEquiClass.size() > 0){
					equivalenceClasses.add(newEquiClass);
				}else{
					newEquiClass = null;
				}
			}
			
			equiClass.clear();
			equiClass = null;
		}
	}
	
	private Itemset getItemset(Itemset i, Itemset j, Map<Integer, Itemset> database){
		Itemset ret = new Itemset();
		
		ret.featureIds.addAll(i.featureIds);
		
		int jFeaturesIdsSize = j.featureIds.size();
		ret.featureIds.add(j.featureIds.get(jFeaturesIdsSize-1));
		
		List<Integer> tids = new Vector<Integer>();
		
		int fit = 0;
		Integer fid = i.featureIds.get(fit);
		Itemset itemset = database.get(fid);
		
		tids.addAll(itemset.tids);
		
		for(++fit; fit < i.featureIds.size(); ++fit){
			fid = i.featureIds.get(fit);
			itemset = database.get(fid);
			
			this.calcIntersection(tids, itemset.tids);
		}
		
		itemset = database.get(j.featureIds.get(jFeaturesIdsSize - 1));
		
		this.calcIntersection(tids, itemset.tids);
		
		for(Integer id : tids){
			ret.addTid(id, tidClassMap.get(id));
		}
		
		return ret;
	}
	
	private void calcIntersection(List<Integer> a, List<Integer> b){
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
}
