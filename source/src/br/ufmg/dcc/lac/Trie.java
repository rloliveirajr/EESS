package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class Trie implements Serializable{

	private static final long serialVersionUID = -2423270729929467987L;

	private final TrieNode root;
	public Trie() {
    	this.root = new TrieNode(-1, null, 0);
    }
   
	public void insertItem(Collection<Integer> itemSet, int tIndex, int classId){
        TrieNode curNode = this.root;
        
        Map<Integer, TrieNode> children = null;
        TrieNode node = null;
        for(int item : itemSet) {
        	children = curNode.links;
            
        	if (!children.containsKey(item)){
            	node = new TrieNode(item, curNode, curNode.depht+1);
            	
            	children.put(item, node);
            }
            
            curNode = children.get(item);
            curNode.tids.put(tIndex, classId);
        }
        
        curNode.fullWord = true; 
    }
    
	public void printTree(){
		System.out.println("Printing tree...");
    	this.printTree(this.root, 0);
    }
   
    private void printTree(TrieNode root, int level) {
    	if(root == null) {
    		return;
    	}
    	
    	TrieNode child = null;
        for(Integer childKey : root.links.keySet()) {
        	 child = root.links.get(childKey);
        	if(child.count() > 1){
        		for(int s = 0; s < child.depht; s++){
	        		System.out.print(" ");
	        	}
	            System.out.println(child);
	
	            this.printTree(root.links.get(childKey), level+1);
        	}
        }
    }
    
    public List<Itemset> generatePotentialItemsetList(){
    	List<Itemset> potentialItemset = new ArrayList<Itemset>();
    	
    	Map<Integer, TrieNode> children = this.root.links;
    	Set<Integer> keys = children.keySet();

    	TrieNode child = null;
    	for(int key : keys){
    		child = children.get(key);
            
            Itemset potentialSet = null;
            
            if(child.count() > 1){
                this.generatePotentialItemsetList(child, potentialItemset);
                
                potentialSet = new Itemset();
                
                this.markNode(child, potentialSet);
                potentialItemset.add(potentialSet);
            }
    	}
    	
    	this.root.finalize();
    	
    	return potentialItemset;
    }
    
    private void generatePotentialItemsetList(TrieNode root, List<Itemset> potentialItemset){
    	
    	if(root.count() > 1 && root.links.size() > 0){
            Map<Integer, TrieNode> children = root.links;
            Set<Integer> keys = children.keySet();
            TrieNode child = null;
            for(Integer childKey : keys){
                child = children.get(childKey);
                
                Itemset potentialSet = null;
                
                if(child.count() > 1){
                    this.generatePotentialItemsetList(child, potentialItemset);
                    
                    potentialSet = new Itemset();
                    
                    this.markNode(child, potentialSet);
                    potentialItemset.add(potentialSet);
                }
            }            
    	}
    }
    
    private void markNode(TrieNode node, Itemset potentialItemset){
    	final int count = node.count();
    	
    	if(!node.marked && node.depht > 0){
    		Set<Integer> nodeTids = node.tids.keySet();
    		List<Integer> itemTids = potentialItemset.tids;
    		
    		if(itemTids.size() == 0){
    			for(Integer t : nodeTids){
    				potentialItemset.addTid(t, node.tids.get(t));
    			}
    		}else{
    			itemTids.retainAll(nodeTids);
    		}
    		
    		potentialItemset.featureIds.add(node.item);
    		
    		node.marked = true;
    		node = node.parent;
    	}
    	
    	while(node != null && node.depht > 0 && node.count() == count){
    		Set<Integer> nodeTids = node.tids.keySet();
    		List<Integer> itemTids = potentialItemset.tids;
    		
    		if(itemTids.size() == 0){
    			for(Integer t : nodeTids){
    				potentialItemset.addTid(t, node.tids.get(t));
    			}
    		}else{
    			itemTids.retainAll(nodeTids);
    		}
    		
    		potentialItemset.featureIds.add(node.item);
    		node.marked = true;
    		node = node.parent;
    	}
    	
    	if(node.item != -1 && node.depht > 0){
    		this.markNode(node, potentialItemset);
    	}
    }
}