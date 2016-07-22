/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.Set;

/**
 * Inconsistency graph determines whether two patterns are inconsistent.
 * 
 * @author vajih
 *
 */
public abstract class InconsistencyGraph {
	/**
	 * Two patterns can be inconsistent, consistent. If they are not comparable,
	 * e.g. their names are not paired with the vertices label, the status is
	 * unknown.
	 * 
	 * @author vajih
	 *
	 */
	public enum STATUS {
		True, False, Unknown
	};

	public abstract STATUS isInconsistent(String patternA, String patternB);

	public abstract Set<String> getAllInconsistecies(String pattern);

	public abstract Set<String> getAllPatterns();
}
