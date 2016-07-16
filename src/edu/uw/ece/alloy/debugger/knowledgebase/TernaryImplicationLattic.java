package edu.uw.ece.alloy.debugger.knowledgebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;

/**
 * @author vajih
 *
 */
public class TernaryImplicationLattic extends ImplicationLattic {

	public TernaryImplicationLattic(String tempPath, String[] moduleName) {
		super(tempPath, moduleName);
		generator = null;
	}

	final TemporalImplicationLatticeGenerator generator;

	public TernaryImplicationLattic(String pathToLegend, String pathToImplication,
			String pathToIff) {
		super();
		generator = new TemporalImplicationLatticeGenerator(pathToLegend,
				pathToImplication, pathToIff);
	}

	public TernaryImplicationLattic() {
		this(TemporalImplicationLatticeGenerator.pathToLegend,
				TemporalImplicationLatticeGenerator.pathToImplication,
				TemporalImplicationLatticeGenerator.pathToIff);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic#getAllSources()
	 */
	@Override
	public List<String> getAllSources() throws Err {
		List<String> result = new LinkedList<>();
		Map<String, Set<String>> revImpilications = generator.findRevReachable();
		for (String key : revImpilications.keySet()) {
			Set<String> revImplied = revImpilications.get(key);
			if (revImplied.isEmpty()) {
				result.add(key);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic#
	 * getNextImpliedProperties(java.lang.String)
	 */
	@Override
	public List<String> getNextImpliedProperties(String property) throws Err {
		List<String> result = new ArrayList<>();
		if (generator.findReachable().containsKey(property)) {
			result.addAll(generator.findReachable().get(property));
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic#
	 * getNextRevImpliedProperties(java.lang.String)
	 */
	@Override
	public List<String> getNextRevImpliedProperties(String property) throws Err {
		List<String> result = new ArrayList<>();
		if (generator.findRevReachable().containsKey(property)) {
			result.addAll(generator.findRevReachable().get(property));
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic#getAllSinks()
	 */
	@Override
	public List<String> getAllSinks() throws Err {
		List<String> result = new LinkedList<>();
		Map<String, Set<String>> implications = generator.findReachable();
		for (String key : implications.keySet()) {
			Set<String> implied = implications.get(key);
			if (implied.isEmpty()) {
				result.add(key);
			}
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic#
	 * getAllImpliedProperties(java.lang.String)
	 */
	@Override
	public List<String> getAllImpliedProperties(String property) throws Err {

		List<String> result = new ArrayList<>();
		if (generator.findAllReachable().containsKey(property)) {
			result.addAll(generator.findAllReachable().get(property));
		}
		return Collections.unmodifiableList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic#
	 * getAllRevImpliedProperties(java.lang.String)
	 */
	@Override
	public List<String> getAllRevImpliedProperties(String property) throws Err {

		List<String> result = new ArrayList<>();
		if (generator.findAllRevReachable().containsKey(property)) {
			result.addAll(generator.findAllRevReachable().get(property));
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public boolean hasPattern(String pattern) {
		System.out.println(pattern + "--->" + generator.getAllpatterns());
		return generator.getAllpatterns().contains(pattern);
	}

	@Override
	public List<String> getAllPatterns() {
		return Collections.unmodifiableList(new ArrayList<>(generator.getAllpatterns()));
	}

}
