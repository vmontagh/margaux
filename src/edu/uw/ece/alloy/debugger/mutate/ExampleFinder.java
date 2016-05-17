package edu.uw.ece.alloy.debugger.mutate;

import java.io.File;
import java.util.Optional;

import edu.mit.csail.sdg.alloy4.Pair;

/**
 * An interface to find on border examples
 * 
 * @author vajih
 *
 */
public interface ExampleFinder {

	/**
	 * Find near border examples. Pair.a satisfies predNameA and Pair.b satisfies
	 * predNameB. predNameA and predNameB has to be inconsistent.
	 * 
	 * @param path
	 * @param predNameA
	 * @param predNameB
	 * @return
	 */
	public Pair<Optional<String>, Optional<String>> findOnBorderExamples(
			File path, String predNameA, String predNameB);

}
