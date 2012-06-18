package de.bfabian.similarites;

public class SimilarityResult {
	public double similarity;
	public DictionaryEntry entry1;
	public DictionaryEntry entry2;
	
	public SimilarityResult(DictionaryEntry e1, DictionaryEntry e2, double similarity) {
		this.entry1 = e1;
		this.entry2 = e2;
		this.similarity = similarity;
	}
}
