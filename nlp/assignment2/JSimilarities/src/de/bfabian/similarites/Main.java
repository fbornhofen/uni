package de.bfabian.similarites;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dictionary d = new Dictionary(args[0], 4, 1);
		d.dumpToFile(args[1]);
		SimilarityFinder sf = new SimilarityFinder(d, 10);
		System.out.println(sf.compareWords(d.get(args[2]), d.get(args[3])).similarity);
	}
}
