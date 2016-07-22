package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.graph.AdjMatrixEdgeWeightedDigraph;
import edu.uw.ece.alloy.util.graph.DirectedEdge;
import edu.uw.ece.alloy.util.graph.FloydWarshall;

public abstract class ImplicationLatticeImeprativeGenerator {


	final Map<Integer, String> legends = new HashMap<>();
	final Map<String, Integer> revLegends = new HashMap<>();

	final Map<String, Set<String>> implicationMap = new HashMap<>();
	final Map<String, Set<String>> revImplicationMap = new HashMap<>();

	final Map<String, Set<String>> allReachableMaps = new HashMap<>();
	final Map<String, Set<String>> allRevReachableMaps = new HashMap<>();

	public ImplicationLatticeImeprativeGenerator(String pathToLegend, String pathToImplication, String pathToIff) {

		final Map<Integer, Set<Integer>> implicationMap = new HashMap<>();
		final Map<Integer, Set<Integer>> revImplicationMap = new HashMap<>();
		final Map<Integer, Set<Integer>> iffMap = new HashMap<>();
		final Map<Integer, Integer> groupingMap = new HashMap<>();

		// read the legend first. The CVS format is: Number->Name
		for (String line : Utils.readFileLines(pathToLegend)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			try {
				Integer code = Integer.parseInt(splittedRow[0]);
				legends.put(code, splittedRow[1]);
				revLegends.put(splittedRow[1], code);
				implicationMap.put(code, new HashSet<>());
				iffMap.put(code, new HashSet<>());
				groupingMap.put(code, null);
			} catch (NumberFormatException nfe) {
			}
		}

		// read the iff map
		for (String line : Utils.readFileLines(pathToIff)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			// codeA <=> codeB
			int codeA = Integer.parseInt(splittedRow[0]);
			int codeB = Integer.parseInt(splittedRow[1]);
			assert legends.containsKey(codeA);
			assert legends.containsKey(codeB);

			assert iffMap.containsKey(codeA);
			// Only add one direction
			if (!iffMap.get(codeB).contains(codeA))
				iffMap.get(codeA).add(codeB);
		}

		groupingMap.keySet().stream().sorted().forEachOrdered(key -> {
			int groupKey = key;
			while (!iffMap.get(groupKey).isEmpty()) {
				groupKey = iffMap.get(groupKey).stream().sorted().findFirst().get();
			}
			groupingMap.put(key, groupKey);
		});

		// read the implication map
		for (String line : Utils.readFileLines(pathToImplication)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			Integer codeA = Integer.parseInt(splittedRow[0]);
			Integer codeB = Integer.parseInt(splittedRow[1]);
			assert legends.containsKey(codeA);
			assert legends.containsKey(codeB);
			assert implicationMap.containsKey(groupingMap.get(codeA));
			implicationMap.get(groupingMap.get(codeA)).add(groupingMap.get(codeB));
		}

		Set<Integer> toBeRemovedKeys = new HashSet<>();
		// remove equal properties from legends
		for (Integer key : groupingMap.keySet()) {
			if (!key.equals(groupingMap.get(key))) {
				revLegends.remove(legends.get(key));
				legends.remove(key);
				implicationMap.remove(key);
				toBeRemovedKeys.add(key);
			}
		}

		implicationMap.keySet().stream().forEach(key -> implicationMap.get(key).removeAll(toBeRemovedKeys));

		// rearrenge the legends key-value. start from 0 ends to
		// legends.size()-1
		Map<Integer, Integer> newKeyMap = new HashMap<>();
		int i = 0;
		final Map<Integer, String> tmpLegends = new HashMap<>();
		final Map<String, Integer> tmpRevLegends = new HashMap<>();
		for (Integer key : legends.keySet()) {
			String name = legends.get(key);
			Integer newKey = i++;
			tmpLegends.put(newKey, name);
			tmpRevLegends.put(name, newKey);
			newKeyMap.put(key, newKey);
		}
		legends.clear();
		legends.putAll(tmpLegends);
		revLegends.clear();
		revLegends.putAll(tmpRevLegends);

		final Map<Integer, Set<Integer>> tmpImplicationMap = new HashMap<>();
		for (Integer key : implicationMap.keySet()) {
			Integer newKey = newKeyMap.get(key);
			tmpImplicationMap.put(newKey, new HashSet<>());
			for (Integer value : implicationMap.get(key)) {
				Integer newValue = newKeyMap.get(value);
				tmpImplicationMap.get(newKey).add(newValue);
			}
		}
		implicationMap.clear();
		implicationMap.putAll(tmpImplicationMap);

		
		// sanitize the implication lattice. The lattice might be also contains
		// self loops or the transitive edges. The transitive edges are the one 
		// like A=>B , B=>C, so that A=>C becomes a transitive edge.
		// Including transitive edges prevents finding the immediate
		// implication, correctly.

		// remove all self implications
		for (Integer key : implicationMap.keySet()) {
			implicationMap.get(key).remove(key);
		}
		
		// To remove the transitive edges, remove all the not immediate neighbors.
		for (Integer from: implicationMap.keySet()){
			Set<Integer> tos = new HashSet<>(implicationMap.get(from));
			for (Integer to: tos){
				Set<Integer> totos =  new HashSet<>(implicationMap.get(to));
				for (Integer toto: totos){
					if (implicationMap.get(from).contains(toto)){
						implicationMap.get(from).remove(toto);
					}
				}
			}
		}
		

		legends.keySet().forEach(key -> revImplicationMap.put(key, new HashSet<>()));
		for (Integer key : legends.keySet()) {
			for (Integer value : implicationMap.get(key)) {
				revImplicationMap.get(value).add(key);
			}
		}

		assert (legends.values().stream().sorted().collect(Collectors.toList())
				.equals(revLegends.keySet().stream().sorted().collect(Collectors.toList())));

		this.implicationMap.putAll(decodeMap(implicationMap));
		this.revImplicationMap.putAll(decodeMap(revImplicationMap));
		this.allReachableMaps.putAll(decodeMap(findAllReachables(legends.keySet(), implicationMap)));
		this.allRevReachableMaps.putAll(decodeMap(findAllReachables(legends.keySet(), revImplicationMap)));
	}

	public String convertImplicationMapToAlloy() {

		StringBuilder result = new StringBuilder();

		result.append("module temporal_ternary_implication\n");

		result.append("open property_structure as ps\n");

		result.append("abstract sig temporal_ternary_prop extends prop{}\n");

		for (Integer key : legends.keySet()) {
			result.append(String.format("one sig %s  extends temporal_ternary_prop{}\n", legends.get(key)));
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
				result.append(String.format("\t%s = %s.imply", impliedProps, key)).append("\n");
			}
		}

		result.append("}");
		return result.toString();
	}

	protected Map<Integer, Set<Integer>> findAllReachables(Set<Integer> nodes, Map<Integer, Set<Integer>> input) {
		Map<Integer, Set<Integer>> result = new HashMap<>();

		AdjMatrixEdgeWeightedDigraph G = new AdjMatrixEdgeWeightedDigraph(legends.size());
		for (Integer from : nodes) {
			if (!input.containsKey(from))
				continue;
			for (Integer to : input.get(from)) {
				G.addEdge(new DirectedEdge(from, to, 1));
			}
		}
		FloydWarshall spt = new FloydWarshall(G);

		for (Integer from : nodes) {
			if (!result.containsKey(from))
				result.put(from, new HashSet<>());
			for (Integer to : nodes) {
				if (from.equals(to))
					continue;
				if (spt.hasPath(from, to)) {
					result.get(from).add(to);
				}
			}
		}

		return Collections.unmodifiableMap(result);
	}

	protected Map<String, Set<String>> decodeMap(Map<Integer, Set<Integer>> map) {
		final Map<String, Set<String>> result = new HashMap<>();
		for (Integer key : map.keySet()) {
			String decodedKey = legends.get(key);
			result.put(decodedKey, new HashSet<>());
			for (Integer value : map.get(key)) {
				String decodedValue = legends.get(value);
				result.get(decodedKey).add(decodedValue);
			}
		}
		return result;
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
	public Map<String, Set<String>> findAllReachable() {
		return Collections.unmodifiableMap(allReachableMaps);
	}

	/**
	 * Find all reachable properties from a property E.g., a=>b, b=>c. return
	 * [c->{b,a}]
	 * 
	 * @return
	 */
	public Map<String, Set<String>> findAllRevReachable() {
		return Collections.unmodifiableMap(allRevReachableMaps);
	}

	public Set<String> getAllpatterns() {
		return Collections.unmodifiableSet(revLegends.keySet());
	}

}
