/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

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

	final Map<Integer, String> legends = new HashMap<>();
	final Map<String, Integer> revLegends = new HashMap<>();
	final Map<Integer, Set<Integer>> iffMap = new HashMap<>();
	final Map<Integer, Integer> groupingMap = new HashMap<>();
	final Map<Integer, Set<Integer>> inconsistencies = new HashMap<>();

	protected TernaryInconsistencyGraph(String pathToLegend, String pathToInconsistency, String pathToIff) {
		// read the legend first. The CVS format is: Number->Name
		for (String line : Utils.readFileLines(pathToLegend)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			try {
				int code = Integer.parseInt(splittedRow[0]);
				legends.put(code, splittedRow[1]);
				revLegends.put(splittedRow[1], code);
				inconsistencies.put(code, new HashSet<>());
				groupingMap.put(code, null);
				iffMap.put(code, new HashSet<>());
			} catch (NumberFormatException nfe) {
				logger.warning(Utils.threadName() + nfe.getMessage());
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
			while (!iffMap.get(groupKey).isEmpty()){
				groupKey = iffMap.get(groupKey).stream().sorted().findFirst().get();
			}
			groupingMap.put(key, groupKey);			
		});
		
		// read inconsistencies
		for (String line : Utils.readFileLines(pathToInconsistency)) {
			String[] splittedRow = line.split(",");
			assert splittedRow.length == 2;
			try {
				int fromCode = Integer.parseInt(splittedRow[0]);
				int toCode = Integer.parseInt(splittedRow[1]);
				int groupIcon4FromCode = groupingMap.get(fromCode);
				int groupIcon4ToCode = groupingMap.get(toCode);
				inconsistencies.get(groupIcon4FromCode).add(groupIcon4ToCode);
				inconsistencies.get(groupIcon4ToCode).add(groupIcon4FromCode);
			} catch (NumberFormatException nfe) {
				logger.warning(Utils.threadName() + nfe.getMessage());
			}
		}

		Set<Integer> toBeRemovedKeys = new HashSet<>();
		// remove equal properties from legends
		for (Integer key : groupingMap.keySet()) {
			if (!key.equals(groupingMap.get(key))) {
				revLegends.remove(legends.get(key));
				legends.remove(key);
				inconsistencies.remove(key);
				toBeRemovedKeys.add(key);
			}
		}
		
		inconsistencies.keySet().stream().forEach(key->inconsistencies.get(key).removeAll(toBeRemovedKeys));
		
		
	}

	public TernaryInconsistencyGraph() {
		this(pathToLegend, pathToInconsistency, pathToIff);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.knowledgebase.InconsistencyGraph#isInconsistent
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public STATUS isInconsistent(String patternA, String patternB) {
		if (!revLegends.containsKey(patternA) ||  !revLegends.containsKey(patternB)) 
			return STATUS.Unknown;
		
		int codeA = revLegends.get(patternA);
		int codeB = revLegends.get(patternB);
		
		if (!inconsistencies.containsKey(codeA) || !inconsistencies.containsKey(codeB))
			return STATUS.False;

		return inconsistencies.get(codeA).contains(codeB) ? STATUS.True : STATUS.False;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.knowledgebase.InconsistencyGraph#
	 * getAllInconsistecies(java.lang.String)
	 */
	@Override
	public Set<String> getAllInconsistecies(String pattern) {
		return revLegends.containsKey(pattern) && inconsistencies.containsKey(revLegends.get(pattern))
				? Collections.unmodifiableSet(inconsistencies.get(revLegends.get(pattern))).stream()
						.map(a -> legends.get(a)).collect(Collectors.toSet())
				: Collections.emptySet();
	}

	/**
	 * The result is a view to the pattern names. IF revLegends is changed
	 * whiting this class, which rarely could happen, the client of this 
	 * method will be effected. The client cannot change the set.
	 */
	@Override
	public Set<String> getAllPatterns() {
		return Collections.unmodifiableSet(revLegends.keySet());
	}

}
