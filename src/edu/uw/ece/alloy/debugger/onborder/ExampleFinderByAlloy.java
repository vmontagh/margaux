package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.util.Optional;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.uw.ece.alloy.MyReporter;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.filters.ExtractorUtils;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;

/**
 * The class is running Alloy to find on border examples
 * 
 * @author vajih
 *
 */
public class ExampleFinderByAlloy implements ExampleFinder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.ece.alloy.debugger.mutate.ExampleFinder#findOnBorderExamples(java.io
	 * .File, java.lang.String, java.lang.String)
	 */
	@Override
	public Pair<Optional<String>, Optional<String>> findOnBorderExamples(
			File path, String predNameA, String predNameB) {

		Pair<Optional<String>, Optional<String>> result = new Pair<>(
				Optional.empty(), Optional.empty());

		try {
			result = new Pair<>(findExample(path, predNameA),
					findExample(path, predNameB));
		} catch (Err e) {
			e.printStackTrace();
		}

		return result;

	}

	protected Optional<String> findExample(File path, String predName)
			throws Err {
		Optional<String> result = Optional.empty();

		System.out.println(path);

		MyReporter rep = new MyReporter();
		A4Solution solution = A4CommandExecuter.getInstance()
				.runThenGetAnswers(path.getAbsolutePath(), rep, predName);

		if (rep.sat != 1 && rep.sat != -1)
			throw new RuntimeException(
					"The analyzing result is not expected. rep.sat=" + rep.sat);

		if (rep.sat == 1)
			result = Optional
					.of(ExtractorUtils.convertA4SolutionToAlloySyntax(solution));

		return result;
	}

}
