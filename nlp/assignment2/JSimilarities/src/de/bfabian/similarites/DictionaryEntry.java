package de.bfabian.similarites;

import java.util.HashMap;

public class DictionaryEntry {

	public String word;
	public HashMap<String, Integer> posTags;
	public HashMap<String, Integer> contextWords;
	public HashMap<String, Integer> contextTags;
	
	public DictionaryEntry(String word) {
		posTags = new HashMap<String, Integer>();
		contextWords = new HashMap<String, Integer>();
		contextTags = new HashMap<String, Integer>();
	}
}
