package de.bfabian.similarites;

import java.util.ArrayList;
import java.util.Collections;

public class SimilarityResultsQueue {

	int elementsLeft;
	ArrayList<SimilarityResult> queue;
	
	public SimilarityResultsQueue(int maxSize) {
		queue = new ArrayList<SimilarityResult>();
		elementsLeft = maxSize;
		if (maxSize == 0) {
			maxSize = 1;
		}
	}
	
	public void add(SimilarityResult sr) {
		if (elementsLeft > 0) {
			queue.add(sr);
			Collections.sort(queue);
			elementsLeft--;
		} else {
			if (queue.get(0).similarity < sr.similarity) {
				queue.remove(0);
				queue.add(sr);
				Collections.sort(queue);
			}
		}
	}
	
	public ArrayList<SimilarityResult> getTopItems() {
		ArrayList<SimilarityResult> result = new ArrayList<SimilarityResult>(queue);
		Collections.reverse(result);
		return result;
	}
}
