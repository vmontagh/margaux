package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ExpressionPropertyGenerator;

@Deprecated
public class BinaryImplicationLatticDeclarative extends ImplicationLattic {

	final static String sourceFolderPath = Configuration.getProp("kb_folder");
	final static String[] moduleNames = Configuration.getProp("kb_modules").split(";");

	public BinaryImplicationLatticDeclarative() {
		this(sourceFolderPath, moduleNames);
	}

	public BinaryImplicationLatticDeclarative(String tempPath, String[] moduleName) {
		super(tempPath, moduleName);
	}

	public List<String> getAllSources() throws Err {
		final String alloyCode = "open binary_implication\n" + "pred run_getSourcesImplication[]{\n"
				+ "  some p: prop | getSourcesImplication[p]}\n" + "run run_getSourcesImplication for 0";
		final File file = new File(this.TEMPORARY_FOLDER,
				"sources_" + System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()) + ".als");

		List<String> result = new ArrayList<>();
		for (A4Solution sol : writeAndFind(alloyCode, file)) {
			result.add(extractSkolemedValue(sol));
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getNextImpliedProperties(String property) throws Err {
		List<String> result = new ArrayList<>();
		if (!property.startsWith(ExpressionPropertyGenerator.ExpressionPredicate.predNamePrefix)) {
			final String alloyCode = String.format("open binary_implication\n"
					+ "pred run_getNextImplications[p: prop]{\n" + "	some p': prop | getNextImplications[p, p']}\n"
					+ "run {run_getNextImplications[%s]} for 0", property);
			final File file = new File(this.TEMPORARY_FOLDER,
					"implied_" + System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()) + ".als");
			for (A4Solution sol : writeAndFind(alloyCode, file)) {
				result.add(extractSkolemedValue(sol));
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getNextRevImpliedProperties(String property) throws Err {
		List<String> result = new ArrayList<>();
		if (!property.startsWith(ExpressionPropertyGenerator.ExpressionPredicate.predNamePrefix)) {
			final String alloyCode = String.format("open binary_implication\n"
					+ "pred run_getPreviousImplications[p: prop]{\n"
					+ "	some p': prop | getNextImplications[p', p]}\n" + "run {run_getPreviousImplications[%s]} for 0",
					property);
			final File file = new File(this.TEMPORARY_FOLDER,
					"revimplied_" + System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()) + ".als");
			for (A4Solution sol : writeAndFind(alloyCode, file)) {
				result.add(extractSkolemedValue(sol));
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getAllSinks() throws Err {
		final String alloyCode = "open binary_implication\n" + "pred run_getFinalSinksImplication[]{\n"
				+ "  some p: prop | getFinalSinksImplication[p]}\n" + "run run_getFinalSinksImplication for 0";
		final File file = new File(this.TEMPORARY_FOLDER,
				"sinks_" + System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()) + ".als");
		List<String> result = new ArrayList<>();
		for (A4Solution sol : writeAndFind(alloyCode, file)) {
			result.add(extractSkolemedValue(sol));
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getAllImpliedProperties(String property) throws Err {
		List<String> result = new ArrayList<>();
		if (!property.startsWith(ExpressionPropertyGenerator.ExpressionPredicate.predNamePrefix)) {

			final String alloyCode = String.format(
					"open binary_implication\n" + "pred run_getAllImplied[p: prop]{\n"
							+ "  some p': prop | getAllImplied[p,p']\n}" + "run {run_getAllImplied[%s]} for 0",
					property);
			final File file = new File(this.TEMPORARY_FOLDER,
					"allimplied_" + System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()) + ".als");
			for (A4Solution sol : writeAndFind(alloyCode, file)) {
				result.add(extractSkolemedValue(sol));
			}
		}
		return Collections.unmodifiableList(result);
	}

	public List<String> getAllRevImpliedProperties(String property) throws Err {
		List<String> result = new ArrayList<>();
		if (!property.startsWith(ExpressionPropertyGenerator.ExpressionPredicate.predNamePrefix)) {
			final String alloyCode = String.format(
					"open binary_implication\n" + "pred run_getAllRevImplied[p: prop]{\n"
							+ "  some p': prop | getAllRevImplied[p,p']\n}" + "run {run_getAllRevImplied[%s]} for 0",
					property);
			final File file = new File(this.TEMPORARY_FOLDER,
					"allimplied_" + System.currentTimeMillis() + "_" + Math.abs(rand.nextInt()) + ".als");
			for (A4Solution sol : writeAndFind(alloyCode, file)) {
				result.add(extractSkolemedValue(sol));
			}
		}
		return Collections.unmodifiableList(result);
	}

	final Set<String> allPatternNamesCache = new HashSet<>();
	@Override
	public boolean hasPattern(String pattern) throws Err {
		if (allPatternNamesCache.isEmpty())
			allPatternNamesCache.addAll(getAllPatternsASSet());
		return allPatternNamesCache.contains(pattern);
	}

	@Override
	public List<String> getAllPatterns() throws Err {
		return Collections.unmodifiableList(new ArrayList<>(getAllPatternsASSet()));
	}
	
	protected Set<String> getAllPatternsASSet() throws Err{
		Set<String> patterns = new HashSet<>();
		for (String source: getAllSources()){
			patterns.add(source);
			patterns.addAll(getAllImpliedProperties(source));
		}
		return Collections.unmodifiableSet(patterns);
	}
}
