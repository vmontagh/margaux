package edu.uw.ece.alloy.debugger.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.CommandScope;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;

/**
 * The class contains static methods that are helpful for extracting Alloy
 * entities.
 * 
 * @author vajih
 *
 */
public class ExtractorUtils {
	/**
	 * Given a command, its scope is returned as String
	 * 
	 * @param command
	 * @return
	 */
	public static String extractScopeFromCommand(Command command) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		if (command.overall >= 0 && (command.bitwidth >= 0 || command.maxseq >= 0
				|| command.scope.size() > 0))
			sb.append(" for ").append(command.overall).append(" but");
		else if (command.overall >= 0)
			sb.append(" for ").append(command.overall);
		else if (command.bitwidth >= 0 || command.maxseq >= 0
				|| command.scope.size() > 0)
			sb.append(" for");
		if (command.bitwidth >= 0) {
			sb.append(" ").append(command.bitwidth).append(" int");
			first = false;
		}
		if (command.maxseq >= 0) {
			sb.append(first ? " " : ", ").append(command.maxseq).append(" seq");
			first = false;
		}
		for (CommandScope e : command.scope) {
			sb.append(first ? " " : ", ").append(e);
			first = false;
		}
		if (command.expects >= 0)
			sb.append(" expect ").append(command.expects);
		return sb.toString();
	}

	/**
	 * Given an A4solution object from AlloyExecuter, it converts it to a Alloy
	 * syntax
	 * 
	 * @param solution
	 * @return
	 */
	public static String convertA4SolutionToAlloySyntax(A4Solution solution) {
		List<String> emptySigs = new ArrayList<>();
		List<String> constraints = new ArrayList<>();
		List<String> quantifiers = new ArrayList<>();
		for (Sig sig : solution.getAllReachableSigs()) {
			if (sig.builtin)
				continue;
			String sigName = sig.label.replace("this/", "");

			if (solution.eval(sig).size() == 0) {
				emptySigs.add(sigName);
			} else {
				List<String> atoms = new ArrayList<>();
				for (A4Tuple tuple : solution.eval(sig)) {
					atoms.add(tuple.toString().replace("$", "_"));
				}
				quantifiers.add("some disj "
						+ atoms.stream().collect(Collectors.joining(", ")) + ": univ");
				constraints.add("\t(" + atoms.stream().collect(Collectors.joining("+"))
						+ ") in " + sigName);
			}
		}

		for (String noSigName : emptySigs) {
			constraints.add("\tno " + noSigName);
		}

		for (Sig sig : solution.getAllReachableSigs()) {
			if (sig.builtin)
				continue;

			for (Field field : sig.getFields()) {
				A4TupleSet fieldsTuples = solution.eval(field);
				String fieldName = field.label;
				if (fieldsTuples.size() == 0) {
					constraints.add("\tno " + fieldName);
				} else {
					final List<String> tuples = new ArrayList<>();
					fieldsTuples.forEach(t -> tuples.add(t.toString().replace("$", "_")));
					constraints
							.add("\t(" + tuples.stream().collect(Collectors.joining("+"))
									+ ") in " + fieldName);
				}
			}
		}

		String result = "{";
		if (quantifiers.size() > 0) {
			result = quantifiers.stream().collect(Collectors.joining("| ")) + "| {";
		}

		result = result + constraints.stream().collect(Collectors.joining("\n"))
				+ "}";
		return result;
	}
}
