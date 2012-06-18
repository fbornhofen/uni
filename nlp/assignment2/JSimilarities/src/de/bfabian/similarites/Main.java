package de.bfabian.similarites;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Dictionary d = new Dictionary(args[0], 4, 1);
		d.dumpToFile(args[1]);
	}

}
