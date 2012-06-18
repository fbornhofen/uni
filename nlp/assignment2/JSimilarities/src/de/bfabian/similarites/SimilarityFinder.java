package de.bfabian.similarites;

public class SimilarityFinder {
	
	Dictionary dict;
	OrderVector orderVec;
	int wordsSize;
	int tagsSize;
	
	public SimilarityFinder(Dictionary dictionary, int nItems) {
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
		return Math.sqrt(result);
	}
	
	double dotProduct(DictionaryEntry e1, DictionaryEntry e2) {
		double result = 0.0;
		for (int i = 0; i < wordsSize; i++) {
			result += e1.contextWordOccurrences(orderVec.wordAt(i)) *
					e2.contextWordOccurrences(orderVec.wordAt(i));
		}
		for (int i = 0; i < tagsSize; i++) {
			result += e1.contextTagOccurrences(orderVec.tagAt(i)) *
					e2.contextTagOccurrences(orderVec.tagAt(i));
		}
		return result;
	}
}
