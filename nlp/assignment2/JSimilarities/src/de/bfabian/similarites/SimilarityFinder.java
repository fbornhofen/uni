package de.bfabian.similarites;

import java.util.ArrayList;
import java.util.Collections;

public class SimilarityFinder {
	
	Dictionary dict;
	OrderVector orderVec;
	int wordsSize;
	int tagsSize;
	
	public SimilarityFinder(Dictionary dictionary) {
		this.dict = dictionary;
		this.orderVec = dict.createOrderVector();
		this.wordsSize = orderVec.wordsSize();
		this.tagsSize = orderVec.tagsSize();
	}
	
	public SimilarityResult compareWords(DictionaryEntry e1, DictionaryEntry e2) {
		return compareWords(e1.word, e2.word);
	}
	
	public SimilarityResult compareWords(String w1, String w2) {
		DictionaryEntry entry1 = dict.get(w1),
						entry2 = dict.get(w2);
		return new SimilarityResult(entry1, entry2,
						cosineSimilarity(entry1, entry2));
	}
	
	public double cosineSimilarity(DictionaryEntry e1, DictionaryEntry e2) {
		return this.dotProduct(e1, e2) / (this.magnitude(e1) * this.magnitude(e2));
	}
	
	double magnitude(DictionaryEntry e) {
		if (e.magnitude != null) {
			return e.magnitude.doubleValue();
		}
		double result = 0.0;
		int tmp = 0;
		for (int i = 0; i < wordsSize; i++) {
			tmp = e.contextWordOccurrences(orderVec.wordAt(i));
			result += tmp * tmp;
		}
		for (int i = 0; i < tagsSize; i++) {
			tmp = e.contextTagOccurrences(orderVec.tagAt(i));
			result += tmp * tmp;
		}
		result = Math.sqrt(result);
		e.magnitude = result;
		return result;
	}
	
	double dotProduct(DictionaryEntry e1, DictionaryEntry e2) {
		double result = 0.0;
		for (int i = 0; i < e1.similarityVector.length; i++) {
			result += e1.similarityVector[i] * e2.similarityVector[i];
		}
		
		// non-cached solution
		/*String tmp; // significant speedup by not calling contextXXXOccurrences twice
		for (int i = 0; i < wordsSize; i++) {
			tmp = orderVec.wordAt(i);
			result += e1.contextWordOccurrences(tmp) *
					e2.contextWordOccurrences(tmp);
		}
		for (int i = 0; i < tagsSize; i++) {
			tmp = orderVec.tagAt(i);
			result += e1.contextTagOccurrences(tmp) *
					e2.contextTagOccurrences(tmp);
		}*/
		return result;
	}
	
	int[] createSimilarityVectorFor(DictionaryEntry e) {
		int[] res = new int[wordsSize + tagsSize];
		int i, j;
		for (i = 0, j = 0; j < wordsSize; i++, j++) {
			res[i] = e.contextWordOccurrences(orderVec.wordAt(j));
		}
		for (j = 0; j < tagsSize; i++, j++) {
			res[i] = e.contextTagOccurrences(orderVec.tagAt(j));
		}
		return res;
	}
	
	void addSimilarityVectorFor(DictionaryEntry e) {
		e.similarityVector = createSimilarityVectorFor(e);
	}
	
	ArrayList<SimilarityResult> nMostSimilarWords(int n) {
		SimilarityResultsQueue topItems = new SimilarityResultsQueue(n);
		ArrayList<String> sortedWords = new ArrayList<String>(dict.words.keySet());
		Collections.sort(sortedWords, String.CASE_INSENSITIVE_ORDER);
		int nWords = sortedWords.size();
		int nComparisons = 0;
		int totalComparisons = nWords*nWords/2;
		int onePercent = totalComparisons / 100;
		int nextPercentage = 1;
		
		// magnitudes faster by caching similarity vectors as raw arrays
		// beware of memory consumption!
		System.out.println("Creating " + nWords + " similarity vectors");
		for (int i = 0; i < nWords; i++) {
			addSimilarityVectorFor(dict.get(sortedWords.get(i)));
		}
		
		System.out.println("crunching!");
		for (int i = 0; i < nWords; i++) {
			for (int j = i + 1; j < nWords; j++) {
				String w1 = sortedWords.get(i),
					w2 = sortedWords.get(j);
				topItems.add(compareWords(w1, w2));
				if (++nComparisons > nextPercentage*onePercent) {
					System.out.println(nextPercentage++ + "% ...");
				}
			}
		}
		return topItems.getTopItems();
	}
	
}
