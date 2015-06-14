package br.ufmg.dcc.lac;

import java.util.LinkedHashMap;
import java.util.Map;

class CacheMap<K, V> extends LinkedHashMap<K, V>{

	private static final long serialVersionUID = 4889271166978244348L;

	private final int maxSize;

    public CacheMap(int maxSize) {
        this.maxSize = maxSize;
    }

    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}

public class Cache<Key, Content> {
	
	private int maxSize;
	private long hits;
	private long misses;
	
	private Map<Key, Content> cacheContent;
	
	public Cache(int maxSize){
		this.maxSize = maxSize;
		this.hits = 0;
		this.misses = 0;
		this.cacheContent = new CacheMap<Key, Content>(this.maxSize);
	}
	
	public void insert(Key key, Content content){
		if(!cacheContent.containsKey(key)){			
			this.cacheContent.put(key, content);
		}else{
			this.cacheContent.remove(key);
			this.cacheContent.put(key, content);
		}
	}
	
	public boolean get(Key key, Content content){
		if(cacheContent.containsKey(key)){
			this.hits++;
			content = cacheContent.get(key);
			return true;
		}
		this.misses++;
		return false;
	}
	
	public void clear(){
		this.hits = 0;
		this.misses = 0;
		this.cacheContent.clear();
	}
	
	public String toString(){
		return cacheContent.toString();
	}
}
