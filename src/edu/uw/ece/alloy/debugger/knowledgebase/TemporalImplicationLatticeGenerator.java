/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.graph.AdjMatrixEdgeWeightedDigraph;
import edu.uw.ece.alloy.util.graph.DirectedEdge;
import edu.uw.ece.alloy.util.graph.FloydWarshall;

/**
 * Given the implication info, the class generates an Alloy file to infer the
 * implications. The given info,has to be two csv files: 1- Implication relation
 * 2- legend relation
 * 
 * @author vajih
 *
 */
public class TemporalImplicationLatticeGenerator {
	public static String pathToLegend = Configuration
			.getProp("kb_temporal_legend");
	public static String pathToImplication = Configuration
			.getProp("kb_temporal_imply");
	public static String pathToIff = Configuration.getProp("kb_temporal_iff");

	final Map<String, Set<String>> implicationMap = new HashMap<>();
	final Map<String, Set<String>> revImplicationMap = new HashMap<>();
	final Map<String, Set<String>> iffMap = new HashMap<>();
	final Map<String, String> legends = new HashMap<>();
	final Map<String, String> revLegends = new HashMap<>();
	final Map<String, String> groupingMap = new HashMap<>();

	final Map<String, List<String>> allReachableMaps = new HashMap<>();
	final Map<String, List<String>> allRevReachableMaps = new HashMap<>();

	public TemporalImplicationLatticeGenerator(String pathToLegend,
			String pathToImplication, String pathToIff) {

		// read the legend first. The CVS format is: Number->Name
		for (String line : Utils.readFileLines(pathToLegend)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			try {
				Integer.parseInt(splittedRow[0]);
				legends.put(splittedRow[0], splittedRow[1]);
				revLegends.put(splittedRow[1], splittedRow[0]);
				implicationMap.put(legends.get(splittedRow[0]), new HashSet<>());
				iffMap.put(legends.get(splittedRow[0]), new HashSet<>());
				groupingMap.put(legends.get(splittedRow[0]), "");
			} catch (NumberFormatException nfe) {
			}
		}

		// read the iff map
		for (String line : Utils.readFileLines(pathToIff)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			assert legends.containsKey(splittedRow[0]);
			assert legends.containsKey(splittedRow[1]);
			assert iffMap.containsKey(legends.get(splittedRow[0]));
			iffMap.get(legends.get(splittedRow[0])).add(legends.get(splittedRow[1]));
		}

		for (String key : groupingMap.keySet()) {
			if (groupingMap.get(key).equals("")) {
				groupingMap.put(key, key);
				for (String otherKey : iffMap.get(key)) {
					groupingMap.put(otherKey, key);
				}
			}
		}

		// read the implication map
		for (String line : Utils.readFileLines(pathToImplication)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			assert legends.containsKey(splittedRow[0]);
			assert legends.containsKey(splittedRow[1]);
			assert implicationMap
					.containsKey(groupingMap.get(legends.get(splittedRow[0])));
			implicationMap.get(groupingMap.get(legends.get(splittedRow[0])))
					.add(groupingMap.get(legends.get(splittedRow[1])));
		}

		// remove equal properties from legends
		for (String key : groupingMap.keySet()) {
			if (!key.equals(groupingMap.get(key))) {
				legends.remove(revLegends.get(key));
				revLegends.remove(key);
				implicationMap.remove(key);
			}
		}

		// rearrenge the legends key-value. start from 0 ends to legends.size()-1
		int i = 0;
		final Map<String, String> tmpLegends = new HashMap<>();
		final Map<String, String> tmpRevLegends = new HashMap<>();
		for (String key : legends.keySet()) {
			String name = legends.get(key);
			String newKey = "" + i++;
			tmpLegends.put(newKey, name);
			tmpRevLegends.put(name, newKey);
		}
		legends.clear();
		legends.putAll(tmpLegends);
		revLegends.clear();
		revLegends.putAll(tmpRevLegends);

		for (String key : implicationMap.keySet()) {
			for (String value : implicationMap.get(key)) {
				if (!revImplicationMap.containsKey(value)) {
					revImplicationMap.put(value, new HashSet<>());
				}
				if (!revImplicationMap.containsKey(key)) {
					revImplicationMap.put(key, new HashSet<>());
				}
				revImplicationMap.get(value).add(key);
			}
		}

		allReachableMaps.putAll(findAllReachables(implicationMap));
		allRevReachableMaps.putAll(findAllReachables(revImplicationMap));
	}

	public String convertImplicationMapToAlloy() {

		StringBuilder result = new StringBuilder();

		result.append("module temporal_ternary_implication\n");

		result.append("open property_structure as ps\n");

		result.append("abstract sig temporal_ternary_prop extends prop{}\n");

		for (String key : legends.keySet()) {
			result.append(String.format(
					"one sig %s  extends temporal_ternary_prop{}\n", legends.get(key)));
		}

		result.append("fact implication{\n");
		// Printing the result
		for (String key : implicationMap.keySet()) {
			if (implicationMap.get(key).isEmpty()) {
				result.append(String.format("\tno %s.imply", key)).append("\n");
			} else {
				String impliedProps = "";
				for (String value : implicationMap.get(key)) {
					if (impliedProps.isEmpty())
						impliedProps = value;
					else
						impliedProps = impliedProps + " + " + value;
				}
				result.append(String.format("\t%s = %s.imply", impliedProps, key))
						.append("\n");
			}
		}

		result.append("}");
		return result.toString();
	}

	public Map<String, List<String>> findAllIffs() {
		Map<String, List<String>> result = new HashMap<>();
		return Collections.unmodifiableMap(result);
	}

	protected Map<String, List<String>> findAllReachables(
			Map<String, Set<String>> input) {
		Map<String, List<String>> result = new HashMap<>();

		AdjMatrixEdgeWeightedDigraph G = new AdjMatrixEdgeWeightedDigraph(
				legends.size());
		for (String from : legends.keySet()) {
			String fromName = legends.get(from);
			int fromInt = Integer.parseInt(from);
			for (String toName : input.get(fromName)) {
				String to = revLegends.get(toName);
				int toInt = Integer.parseInt(to);
				G.addEdge(new DirectedEdge(fromInt, toInt, 1));
			}
		}
		FloydWarshall spt = new FloydWarshall(G);

		for (String from : legends.keySet()) {
			String fromName = legends.get(from);
			int fromInt = Integer.parseInt(from);
			if (!result.containsKey(fromName))
				result.put(fromName, new ArrayList<>());
			for (String to : legends.keySet()) {
				if (from.equals(to))
					continue;
				String toName = legends.get(to);
				int toInt = Integer.parseInt(to);
				if (spt.hasPath(fromInt, toInt)) {
					result.get(fromName).add(toName);
				}
			}
		}

		return Collections.unmodifiableMap(result);
	}

	public Map<String, Set<String>> findReachable() {
		return Collections.unmodifiableMap(implicationMap);
	}

	public Map<String, Set<String>> findRevReachable() {
		return Collections.unmodifiableMap(revImplicationMap);
	}

	/**
	 * Find all reachable properties from a property E.g., a=>b, b=>c. return
	 * [a->{b,c}]
	 * 
	 * @return
	 */
	public Map<String, List<String>> findAllReachable() {
		return Collections.unmodifiableMap(allReachableMaps);
	}

	/**
	 * Find all reachable properties from a property E.g., a=>b, b=>c. return
	 * [c->{b,a}]
	 * 
	 * @return
	 */
	public Map<String, List<String>> findAllRevReachable() {
		return Collections.unmodifiableMap(allRevReachableMaps);
	}

	public static void main(String... args) {
		TemporalImplicationLatticeGenerator generator = new TemporalImplicationLatticeGenerator(
				pathToLegend, pathToImplication, pathToIff);

		for (String key : generator.iffMap.keySet()) {
			if (!generator.iffMap.get(key).isEmpty()) {
				System.out.println(generator.iffMap.get(key));
			}
		}
		System.out.println("----------------------");

		int notGrouped = 0;
		for (String key : generator.groupingMap.keySet()) {
			boolean wasTheSame = key.equals(generator.groupingMap.get(key));
			if (wasTheSame)
				++notGrouped;
			System.out.println("Self?" + (wasTheSame) + "\t" + key + "-->"
					+ generator.groupingMap.get(key));
		}

		System.out.println("Not the same:" + notGrouped);

		System.out.println("----------------------");

		System.out.println(generator.findAllReachable());
	}

}
