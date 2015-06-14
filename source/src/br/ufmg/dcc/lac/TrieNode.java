package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TrieNode implements Comparable<TrieNode>, Serializable{

	private static final long serialVersionUID = 133411516605313459L;
	
	public final int item;
	public Map<Integer, Integer> tids;
	
    final TrieNode parent;
    final int depht;
    
    Map<Integer, TrieNode> links;
    boolean fullWord;
    boolean marked;
    
    public TrieNode(int item, TrieNode parent, int depht) {
        this.item = item;
        this.links = new HashMap<Integer, TrieNode>();
        
        this.tids = new HashMap<Integer, Integer>();
        this.depht = depht;
        this.parent = parent;
        
        this.fullWord = false;
        this.marked = false;
    }
    
    int count(){
    	return this.tids.size();
    }

	@Override
	public int compareTo(TrieNode o) {
		int comparing = this.count() - o.count();
		return comparing;
	}
	
	public String toString(){
		final StringBuffer s = new StringBuffer();
		s.append("(Depht: " + this.depht + ") ");
		s.append(this.item + ":{" + this.tids + "}(" + this.fullWord + ")");		
		return s.toString();
	}
    
	protected void finalize(){
		this.tids.clear();
		this.tids = null;
		
		if(this.links.size() > 0){
			for(TrieNode child : this.links.values()){
				child.finalize();
				child = null;
			}
		}
		this.links.clear();
		this.links = null;
		
	}
    
}