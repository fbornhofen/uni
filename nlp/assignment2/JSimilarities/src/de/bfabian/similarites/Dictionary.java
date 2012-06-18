package de.bfabian.similarites;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class Dictionary {

	HashMap<String, DictionaryEntry> words;
	HashSet<String> posTags;
	int wordWin;
	int posWin;
	
	public Dictionary(String fileName, int contextWindow, int posWindow) {
		this.wordWin = contextWindow;
		this.posWin = posWindow;
		this.words = new HashMap<String, DictionaryEntry>();
		this.posTags = new HashSet<String>();
		ArrayList<String> corpus = this.readFromCorpus(fileName);
		this.fillDictionary(corpus);
		System.out.println("Extracted " + words.size() + " words");
	}

	private void increaseOrInitialize(HashMap<String,Integer> map, String key) {
		Integer count = map.get(key);
		if (count == null) {
			map.put(key, new Integer(1));
		} else {
			map.put(key, count + 1);
		}
	}
	
	private void fillDictionary(ArrayList<String> corpus) {
		int winSize = Math.max(wordWin, posWin);
		String line;
		for (int i = 0; i < corpus.size(); i++) {
			line = corpus.get(i);
			String[] splitLine = line.split("\\s");
			for (int j = 0; j < splitLine.length; j++) { // jth word in line
				String[] wordAndTag = splitLine[j].split("\\/");
				String word = wordAndTag[0];
				String tag = wordAndTag[1];
				DictionaryEntry entry;
				
				// Look up or create new word
				entry = words.get(word);
				if (entry == null) {
					entry = new DictionaryEntry(word);
					words.put(word, entry);
				}
				posTags.add(tag);
				
				// update number of occurrences of the current POS tag
				this.increaseOrInitialize(entry.posTags, tag);
				
				// extract context
				for (int k = j - winSize; k <= j + winSize; k++) { // kth context word
					if (k < 0 || k == j || k >= splitLine.length) continue;
					String[] ctxWordAndTag = splitLine[k].split("\\/");
					if (k >= j - wordWin && k <= j + wordWin) {
						this.increaseOrInitialize(entry.contextWords, ctxWordAndTag[0]);
					}
					if (k >= j - posWin && k <= j + posWin) {
						this.increaseOrInitialize(entry.contextTags, ctxWordAndTag[1]);
					}
				}
			}
		}
	}

	ArrayList<String> readFromCorpus(String fileName) {
		ArrayList<String> contents = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				contents.add(strLine);
			}
			in.close();
		} catch (Exception e) {
			System.err.println("Error: (" + fileName + ") " + e.getMessage());
		}
		return contents;
	}
	
	void dumpToFile(String fileName) {
		try {
			System.out.println("dumping " + words.keySet().size() + " entries ");
			
			FileWriter writer = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(writer);
			Iterator<String> curWord = words.keySet().iterator();
			while (curWord.hasNext()) {
				DictionaryEntry entry = words.get(curWord.next());
				bw.write(entry.toString() + "\n");
			}
			bw.close();
		} catch (Exception e) {
			System.err.println("Error: (" + fileName + ") " + e.getMessage() + "\n" + e);
		}
	}
	
	Vector<String> createOrderVector() {
		ArrayList<String> allTags = this.sortedList(posTags);
		ArrayList<String> allWords = this.sortedList(words.keySet());
		Vector<String> result = new Vector<String>(allWords);
		result.addAll(allTags);
		return result;
	}
	
	public void printOrderVector() {
		System.out.print("(");
		Vector<String> orderVector = this.createOrderVector();
		for (int i = 0; i < orderVector.size(); i++) {
			System.out.print(orderVector.get(i) + " ");
		}
		System.out.println(")");
	}
	
	ArrayList<String> sortedList(Collection<String> aCollection) {
		ArrayList<String> result = new ArrayList<String>(aCollection);
		java.util.Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
		return result;
	}
}
