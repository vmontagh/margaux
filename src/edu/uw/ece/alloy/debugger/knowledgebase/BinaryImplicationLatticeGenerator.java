/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.PropertyCallBuilder;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.util.Utils;

/**
 * Using PropertyCallBuilder and PropertyCheckingBuilder This class makes an
 * Alloy file showing the implication relations.
 * 
 * @author vajih
 *
 */
public class BinaryImplicationLatticeGenerator {
	final public static File relationalPropModuleOriginal = new File(
			Configuration.getProp("relational_properties_tagged"));
	final public static File tmpDirectoryRoot = new File(
			Configuration.getProp("temporary_directory"));

	final public static String relationDefinition = "sig Right{s: set Right}\nsig Left{r: set Right}\n";
	final public static String checkStatemet = "open relational_properties_tagged\nassert ac{\t%s implies %s}\n check ac";

	public static void main(String... args) throws Err {
		// read the relationalPropModuleOriginal and make a new file having
		// the relation definition.
		String tggedLibrary = Utils
				.readFile(relationalPropModuleOriginal.getAbsolutePath());
		tggedLibrary += "\n";
		tggedLibrary += relationDefinition;

		// Store back the merged file into the temp directory.
		File tmpFile = new File(tmpDirectoryRoot,
				"relational_properties_tagged.als");
		Util.writeAll(tmpFile.getAbsolutePath(), tggedLibrary);

		CompModule world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
				tmpFile.getAbsolutePath());

		PropertyCallBuilder pcb = new PropertyCallBuilder();
		final List<Field> fields = world.getAllReachableSigs().stream()
				.map(a -> a.getFields().makeCopy()).filter(a -> a.size() > 0)
				.flatMap(a -> a.stream()).collect(Collectors.toList());
		for (Func func : world.getAllFunc()) {
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
			}
		}

		Map<String, Set<String>> implicationMap = new HashMap<>();

		for (String p1 : pcb.makeAllBinaryProperties(fields.get(0))) {
			for (String p2 : pcb.makeAllBinaryProperties(fields.get(0))) {
				if (p1.equals(p2))
					continue;
				String p1Name = p1.substring(0, p1.indexOf("["));
				String p2Name = p2.substring(0, p2.indexOf("["));

				File checkFile = new File(tmpDirectoryRoot,
						String.format("%s-%s.als", p1Name, p2Name));
				Util.writeAll(checkFile.getAbsolutePath(),
						String.format(checkStatemet, p1, p2));
				A4Solution sol = A4CommandExecuter.getInstance().runThenGetAnswers(
						checkFile.getAbsolutePath(), A4Reporter.NOP, "ac");

				if (!implicationMap.containsKey(p1Name))
					implicationMap.put(p1Name, new HashSet<>());

				if (!sol.satisfiable()) {
					implicationMap.get(p1Name).add(p2Name);
				}

			}
		}

		/*
		 * for (String p1: pcb.makeAllBinaryProperties(fields.get(1))){ for (String
		 * p2: pcb.makeAllBinaryProperties(fields.get(1))){ if (p1.equals(p2))
		 * continue; String p1Name = p1.substring(0, p1.indexOf("[") ); String
		 * p2Name = p2.substring(0, p2.indexOf("[") );
		 * 
		 * File checkFile = new File(tmpDirectoryRoot, String.format("%s-%s.als",
		 * p1Name, p2Name)); Util.writeAll(checkFile.getAbsolutePath(),
		 * String.format(checkStatemet, p1, p2)); A4Solution sol =
		 * A4CommandExecuter.getInstance().runThenGetAnswers( new String[] {
		 * checkFile.getAbsolutePath() }, A4Reporter.NOP, "ac");
		 * 
		 * if (!implicationMap.containsKey(p1Name)) implicationMap.put(p1Name, new
		 * HashSet<>());
		 * 
		 * if (!sol.satisfiable()){ implicationMap.get(p1Name).add(p2Name); }
		 * 
		 * } }
		 */

		for (String key : implicationMap.keySet()) {
			System.out.println(String.format("one sig %s  extends bin_prop{}", key));
		}

		System.out.println("fact implication{");
		// Printing the result
		for (String key : implicationMap.keySet()) {

			if (implicationMap.get(key).isEmpty()) {
				System.out.println(String.format("no %s.imply", key));
			} else {
				String impliedProps = "";
				for (String value : implicationMap.get(key)) {
					if (impliedProps.isEmpty())
						impliedProps = value;
					else
						impliedProps = impliedProps + " + " + value;
				}
				System.out.println(String.format("%s = %s.imply", impliedProps, key));
			}
		}
		System.out.println("}");
	}

}
