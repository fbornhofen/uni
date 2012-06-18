package de.bfabian.similarites;

import java.util.HashMap;
import java.util.Iterator;

public class DictionaryEntry {

	public String word;
	public HashMap<String, Integer> posTags;
	public HashMap<String, Integer> contextWords;
	public HashMap<String, Integer> contextTags;
	
	public DictionaryEntry(String word) {
		this.word = word;
		this.posTags = new HashMap<String, Integer>();
		this.contextWords = new HashMap<String, Integer>();
		this.contextTags = new HashMap<String, Integer>();
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
