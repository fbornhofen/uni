package de.bfabian.similarities;

import java.util.HashMap;
import java.util.Iterator;

public class DictionaryEntry {

	public String word;
	public HashMap<String, Integer> posTags;
	public HashMap<String, Integer> contextWords;
	public HashMap<String, Integer> contextTags;
	public Double magnitude; // cache for SimilarityFinder
	public int[] similarityVector;
	public int[] dotProductIndices; // cache indices into similarity vector
									// that are relevant for dotProduct
	
	public DictionaryEntry(String word) {
		this.word = word;
		this.posTags = new HashMap<String, Integer>();
		this.contextWords = new HashMap<String, Integer>();
		this.contextTags = new HashMap<String, Integer>();
		this.magnitude = null;
		this.similarityVector = null;
		this.dotProductIndices = null;
	}

	public int contextWordOccurrences(String word) {
		return this.getOrZero(contextWords, word);
	}

	
	public int contextTagOccurrences(String tag) {
		return this.getOrZero(contextTags, tag);
	}
	
	int getOrZero(HashMap<String, Integer> map, String key) {
		Integer res = map.get(key);
		if (res == null) {
			return 0;
		}
		return res.intValue();
	}

	public String toString() {
		String result = word + "\t";
		result += this.hashMapToString(contextWords);
		result += "\t";
		result += this.hashMapToString(contextTags);
		return result;
	}
	
	String hashMapToString(HashMap<String, Integer> hashMap) {
		String res = "";
		Iterator<String> it = hashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			res += key + " ";
			//res += key + "=" + hashMap.get(key) + " ";
		}
		return res;
	}
}
