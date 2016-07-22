/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.logging.Logger;

import edu.uw.ece.alloy.Configuration;

/**
 * The class for structural Binary inconsistency graph
 * 
 * @author vajih
 *
 */
public class BinaryInconsistencyGraph extends InconsistencyGraph {

	final static Logger logger = Logger
			.getLogger(BinaryInconsistencyGraph.class.getName() + "--" + Thread.currentThread().getName());

	public static String pathToLegend = Configuration.getProp("kb_structural_legend");
	public static String pathToInconsistency = Configuration.getProp("kb_structural_incon");
	public static String pathToIff = Configuration.getProp("kb_structural_iff");

	protected BinaryInconsistencyGraph(String pathToLegend, String pathToInconsistency, String pathToIff) {

		super(pathToLegend, pathToInconsistency, pathToIff);

	}

	public BinaryInconsistencyGraph() {
		this(pathToLegend, pathToInconsistency, pathToIff);
	}

}
