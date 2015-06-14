package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class Projection implements Serializable{

	private static final long serialVersionUID = 1277980866123105276L;
	
	private int cacheSize;
	List<Itemset> itemsets;
	
	Cache<List<Integer>, Integer> cache;
	
	Projection(int cacheSize){
		this.cacheSize = cacheSize;
		itemsets = new Vector<Itemset>();
		this.cache = new Cache<List<Integer>, Integer>(this.cacheSize);
	}
	
	int getSize(){
		int size = 0;
		
		List<Integer> keys = new Vector<Integer>();
		for(Itemset i : this.itemsets){
			keys.add(i.featureIds.get(0));
		}
		
		if(!cache.get(keys, size)){
			Set<Integer> set = new HashSet<Integer>();
			for(Itemset itemset : itemsets){
				for(Integer tid : itemset.tids){
					if(!set.contains(tid)){
						size++;
						set.add(tid);
					}
				}
			}
			cache.insert(keys, size);
		}
		
		return size;
	}
}
