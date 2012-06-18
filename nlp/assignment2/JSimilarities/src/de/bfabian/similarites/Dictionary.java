package de.bfabian.similarites;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Dictionary {

	HashMap<String, DictionaryEntry> words;
	ArrayList<String> posTags;
	int wordWin;
	int posWin;
	
	public Dictionary(String fileName, int contextWindow, int posWindow) {
		this.wordWin = contextWindow;
		this.posWin = posWindow;
		words = new HashMap<String, DictionaryEntry>();
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
				words.get(word);
				
				// update number of occurrences of the current POS tag
				this.increaseOrInitialize(entry.posTags, tag);
				
				// extract context
				System.out.println("Word: " + wordAndTag[0]);
				for (int k = j - winSize; k <= j + winSize; k++) { // kth context word
					if (k < 0 || k == j || k >= splitLine.length) continue;
					String[] ctxWordAndTag = splitLine[k].split("\\/");
					if (k >= j - wordWin && k <= j + wordWin) {
						this.increaseOrInitialize(entry.contextWords, ctxWordAndTag[0]);
					}
					if (k >= j - posWin && k <= j + posWin) {
						this.increaseOrInitialize(entry.contextTags, ctxWordAndTag[1]);
					}
					System.out.println("\tWord: " + ctxWordAndTag[0]);
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
			System.err.println("Error: " + e.getMessage());
		}
		return contents;
	}
	
	void dumpToFile(String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(writer);
			Iterator curWord = words.keySet().iterator();
			while (curWord.hasNext()) {
				curWord.next();
			}
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
