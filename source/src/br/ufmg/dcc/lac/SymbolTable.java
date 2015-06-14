package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SymbolTable implements Serializable{

	private static final long serialVersionUID = -1351133821643801052L;
	
	private final Map<String, Integer> tableNameId;
	private final Map<Integer, String> tableIdName;
	private int count;
	
	public SymbolTable(){
		count = 0;
		tableNameId = new HashMap<String, Integer>();
		tableIdName = new TreeMap<Integer, String>();
	}
	
	static SymbolTable featuresTable(){
		SymbolTable featuresTable = new SymbolTable();

		return featuresTable;
	}
	
	static SymbolTable classesTable(){
		SymbolTable classesTable = new SymbolTable();	
		
		return classesTable;
	}
	
	public int addName(String name){
		int value = -1;
		
		if(!this.tableNameId.containsKey(name)){
			this.tableNameId.put(name, count);
			this.tableIdName.put(count, name);
			value = count;
			count++;
		}else{
			value = this.tableNameId.get(name);
		}
		
		return value;
	}

	public int getId(String name) throws Exception{
		try{
			return this.tableNameId.get(name);
		}catch(Exception e){
			Exception ex = new Exception("SymbolTable ERROR, invalid key: " + name);
			throw ex;
		}
	}
	
	public int size(){
		return this.tableNameId.size();
	}
	
	public String getName(int id){
		return this.tableIdName.get(id);

	}
	
	Map<String, Integer> getTableName(){
		return new HashMap<String, Integer>(tableNameId);
	}
	
	public void clear(){
		count = 0;
		tableIdName.clear();
		tableNameId.clear();
	}
}
