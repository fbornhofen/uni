package de.bfabian.similarites;

import java.util.ArrayList;
import java.util.Vector;

/**
 * 
 * @author Fabian Bornhofen
 * 
 * Vectors for cosine similarity will be sparse.
 * Instead of creating a sparse vector per word pair, we define the order
 * of an imaginary vector for cosine similarity calculation.
 */
public class OrderVector {
	Vector<String> wordVec;
	Vector<String> tagVec;
	
	public OrderVector(ArrayList<String> words, ArrayList<String> tags) {
		this.wordVec = new Vector<String>(words);
		this.tagVec = new Vector<String>(tags);
	}
	
	public int wordsSize() {
		return wordVec.size();
	}
	
	public int tagsSize() {
		return wordVec.size();
	}
	
	public String wordAt(int index) {
		return wordVec.get(index);
	}
	
	public String tagAt(int index) {
		return tagVec.get(index);
	}
	
	public String toString() {
		String res = "[";
		for (int i = 0; i < wordVec.size(); i++) {
			res += wordVec.get(i) + " ";
		}
		for (int i = 0; i < tagVec.size(); i++) {
			res += tagVec.get(i) + " ";
		}
		return res + "]";
	}
}