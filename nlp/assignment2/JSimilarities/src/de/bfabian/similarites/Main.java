package de.bfabian.similarites;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 6) {
			usage();
		}
		String inFile = 				args[0];
		String contextsOutFile =		args[1];
		String similaritiesOutFile =	args[2];
		int wordWin = 	Integer.parseInt(args[3]);
		int posWin = 	Integer.parseInt(args[4]);
		int nWords = 	Integer.parseInt(args[5]);
		
		Dictionary d = new Dictionary(inFile, wordWin, posWin);
		d.dumpToFile(contextsOutFile);
		SimilarityFinder sf = new SimilarityFinder(d);
		
		System.out.println("Dictionary has " + d.words.size() + " words and " + d.posTags.size() + " POS tags.");
		
		System.out.println("Now finding " + nWords + " most similar words ...");
		ArrayList<SimilarityResult> mostSimilarWords = sf.nMostSimilarWords(nWords);
		dumpSimilarities(mostSimilarWords, similaritiesOutFile);
		
	}
	
	static void dumpSimilarities(ArrayList<SimilarityResult> aList, String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(writer);
			for (int i = 0; i < aList.size(); i++) {
				bw.write(aList.get(i) + "\n");
			}
			bw.close();
		} catch (Exception e) {
			System.err.println("Error: (" + fileName + ") " + e.getMessage() + "\n" + e);
		}
	}
	
	static void usage() {
		System.out.println("Args: <input-file> <contexts-output-file> <similarities-output-file> "+
							"<word-win> <pos-win> <n-similar-words>");
		System.exit(-1);
	}
}
