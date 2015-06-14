package br.ufmg.dcc.lac;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

class Hash implements Serializable{
	
	private static final long serialVersionUID = 4424249058175635347L;
	
	public static final int MAX_INT_SMALLER_TWIN_PRIME = 2147482949;
	private final int seedA;
	private final int seedB;
	private final int seedC;

	Hash(int seedA, int seedB, int seedC) {
		this.seedA = seedA;
		this.seedB = seedB;
		this.seedC = seedC;
	}


	public int hash(byte[] bytes) {
		long hashValue = 31;
		
		for (byte byteVal : bytes) {
			hashValue *= seedA * (byteVal >> 4);
			hashValue += seedB * byteVal + seedC;
		}
		
		return Math.abs((int) (hashValue % MAX_INT_SMALLER_TWIN_PRIME));
	}
}

public class MinHash implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private final int numHashes;
	private final Hash[] hashFunctions;
	
	public MinHash(int numHashes){
		this.numHashes = numHashes;
		this.hashFunctions = new Hash[numHashes];
		
		Random seed = new Random(11);
		for(int i = 0; i < numHashes; i++){
			this.hashFunctions[i] = new Hash(seed.nextInt(), seed.nextInt(), seed.nextInt());
		}
	}
	
	public String[] minHash(Collection<Integer> items){
		
		int[] minHashes = new int[numHashes];
		for(int i = 0; i < numHashes; i++){
			minHashes[i] = Integer.MAX_VALUE;
		}
		
		byte[] bytesToHash = new byte[4];
		int hashIndex;
		for(int i = 0; i < numHashes; i++){
			for(int item : items){
				bytesToHash[0] = (byte) (item >> 24);
				bytesToHash[1] = (byte) (item >> 16);
				bytesToHash[2] = (byte) (item >> 8);
				bytesToHash[3] = (byte) (item);
				
				hashIndex = this.hashFunctions[i].hash(bytesToHash);
				
				if(hashIndex < minHashes[i]){
					minHashes[i] = hashIndex;
				}
			}
		}
		
		String[] hashes = new String[numHashes];
		int i;
		for(i = 0; i < numHashes; i++){
			hashes[i] = minHashes[i]+"";
		}

		Arrays.sort(hashes);
		return hashes;
	}
	
	public String[] minHash(int[] items){
		
		int[] minHashes = new int[numHashes];
		for(int i = 0; i < numHashes; i++){
			minHashes[i] = Integer.MAX_VALUE;
		}
		
		byte[] bytesToHash = new byte[4];
		int hashIndex;
		for(int i = 0; i < numHashes; i++){
			for(int item : items){
				bytesToHash[0] = (byte) (item >> 24);
				bytesToHash[1] = (byte) (item >> 16);
				bytesToHash[2] = (byte) (item >> 8);
				bytesToHash[3] = (byte) (item);
				
				hashIndex = this.hashFunctions[i].hash(bytesToHash);
				
				if(hashIndex < minHashes[i]){
					minHashes[i] = hashIndex;
				}
			}
		}
		
		String[] hashes = new String[numHashes];
		int i;
		for(i = 0; i < numHashes; i++){
			hashes[i] = minHashes[i]+"";
		}

		Arrays.sort(hashes);
		return hashes;
	}
}
