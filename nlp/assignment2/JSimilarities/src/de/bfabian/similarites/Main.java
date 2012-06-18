package de.bfabian.similarites;

import java.util.HashSet;
import java.util.Iterator;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//new Dictionary(args[0], 4, 1);
		
		HashSet<String> s = new HashSet<String>();
		s.add("Foo");
		s.add("Bar");
		s.add("Baz");
		Iterator<String> i = s.iterator();
		while (i.hasNext()) {
			System.out.println(i);
			i.next();
		}
	}

}
