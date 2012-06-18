package de.bfabian.similarites;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
		result += this.setToString(contextWords.keySet());
		result += "\t";
		result += this.setToString(contextTags.keySet());
		return result;
	}
	
	String setToString(Set<String> aSet) {
		String res = "";
		Iterator<String> it = aSet.iterator();
		while (it.hasNext()) {
			res += it.next() + " ";
		}
		return res;
	}
}
