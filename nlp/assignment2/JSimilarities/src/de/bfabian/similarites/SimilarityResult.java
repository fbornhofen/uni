package de.bfabian.similarites;

public class SimilarityResult implements Comparable<SimilarityResult> {
	public double similarity;
	public DictionaryEntry entry1;
	public DictionaryEntry entry2;
	
	public SimilarityResult(DictionaryEntry e1, DictionaryEntry e2, double similarity) {
		this.entry1 = e1;
		this.entry2 = e2;
		this.similarity = similarity;
	}

	@Override
	public int compareTo(SimilarityResult arg0) {
		double res = this.similarity - arg0.similarity;
		if (res < 0) return -1;
		if (res > 0) return 1;
		return 0;
	}
	
	public String toString() {
		String res = "";
		if (entry1 == null) {
			res += "(nil)";
		} else {
			res += entry1.word;
		}
		res += " ";
		if (entry2 == null) {
			res += "(nil)";
		} else {
			res += entry2.word;
		}
		return res + " " + similarity;
	}
	
	
}
