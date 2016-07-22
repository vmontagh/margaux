package edu.uw.ece.alloy.debugger.knowledgebase;

import edu.uw.ece.alloy.Configuration;

public class StructuralImplicationLatticeImperativeGenerator extends ImplicationLatticeImeprativeGenerator {
	public static String pathToLegend = Configuration.getProp("kb_structural_legend");
	public static String pathToImplication = Configuration.getProp("kb_structural_imply");
	public static String pathToIff = Configuration.getProp("kb_structural_iff");

	public StructuralImplicationLatticeImperativeGenerator(String pathToLegend, String pathToImplication,
			String pathToIff) {
		super(pathToLegend, pathToImplication, pathToIff);
	}

	public StructuralImplicationLatticeImperativeGenerator() {
		super(pathToLegend, pathToImplication, pathToIff);
	}
}
