/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import edu.uw.ece.alloy.Configuration;

/**
 * Given the implication info, the class generates an Alloy file to infer the
 * implications. The given info,has to be two csv files: 1- Implication relation
 * 2- legend relation
 * 
 * @author vajih
 *
 */
public final class TemporalImplicationLatticeImperativeGenerator extends ImplicationLatticeImeprativeGenerator {
	public static String pathToLegend = Configuration.getProp("kb_temporal_legend");
	public static String pathToImplication = Configuration.getProp("kb_temporal_imply");
	public static String pathToIff = Configuration.getProp("kb_temporal_iff");

	public TemporalImplicationLatticeImperativeGenerator(String pathToLegend, String pathToImplication,
			String pathToIff) {
		super(pathToLegend, pathToImplication, pathToIff);
	}

	public TemporalImplicationLatticeImperativeGenerator() {
		super(pathToLegend, pathToImplication, pathToIff);
	}
}
