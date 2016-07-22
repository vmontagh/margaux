/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;

/**
 * Implementing the inconsistency graph for temporal ternary patterns. The
 * current implementation takes pre-computed inconsistency graph as a csv file.
 * 
 * <em>The given inconsistency data is already considered the closure.</em>
 * 
 * @author vajih
 *
 */
public class TernaryInconsistencyGraph extends InconsistencyGraph {

	final static Logger logger = Logger
			.getLogger(TernaryInconsistencyGraph.class.getName() + "--" + Thread.currentThread().getName());

	public static String pathToLegend = Configuration.getProp("kb_temporal_legend");
	public static String pathToInconsistency = Configuration.getProp("kb_temporal_incon");
	public static String pathToIff = Configuration.getProp("kb_temporal_iff");

	protected TernaryInconsistencyGraph(String pathToLegend, String pathToInconsistency, String pathToIff) {

		super(pathToLegend, pathToInconsistency, pathToIff);

	}

	public TernaryInconsistencyGraph() {
		this(pathToLegend, pathToInconsistency, pathToIff);
	}

}
