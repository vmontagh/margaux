package edu.uw.ece.alloy.debugger.mutate;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.mutate.experiment.DebuggerAlgorithmHeuristics;
import edu.uw.ece.alloy.debugger.mutate.experiment.DebuggerAlgorithmRandom;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.util.LazyFile;

/**
 * Testing DebuggerRunnerTest
 * 
 * @author vajih
 *
 */
public class DebuggerRunnerTest {

	InetSocketAddress testingHost;

	final long startTime = System.currentTimeMillis();

	public final void print(String... args) {
		final long current = System.currentTimeMillis() - startTime;
		System.out.print(current + " - ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println();
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		testingHost = ProcessorUtil.findEmptyLocalSocket();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStrongestApproximationOneProp() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(tmpLocalDirectory,
				"toBeAnalyzedCode.als");
		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(),
				"sig A{r: one A}\n pred p[]{  some A and no A.r}\nrun {p implies some A}");

		File relationalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"relationalPropModuleOriginal.als");
		Util.writeAll(relationalPropModuleOriginal.getAbsolutePath(),
				"pred weaklyConnected [ r :univ->univ,  left:univ, right:univ ] {\n"
						+ "\tall d: right | all g: left - d  | d in g.^(r + ~r)\n" + "}\n"
						+ "pred rootedOne [r: univ->univ, left:univ, right: univ]{"
						+ "\tone root:left | right in root.*r" + "}");

		File temporalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"temporalPropModuleOriginal.als");
		Util.writeAll(temporalPropModuleOriginal.getAbsolutePath(),
				"open relational_lib as relational_properties\n"
						+ "pred OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[r: univ->univ->univ, left, middle, right: univ, left_first: univ, left_next: univ->univ, right_first: univ, right_next: univ->univ]{\n"
						+ "\tall left': left - relational_properties/last[left,left_next] |let left'' = left'.left_next |\n"
						+ "\tlet c = middle.(left'.r) |let c' = middle.(left''.r) | let delta = c' - c |  (c in c' ) and   (c' !in c ) and   (some delta implies(some c implies relational_properties/lt[relational_properties/max[delta,right_next],relational_properties/min[c,right_next],right_next] ) )\n"
						+ "}");

		File relationalLib = new LazyFile(tmpLocalDirectory, "relational_lib.als");
		Util.writeAll(relationalLib.getAbsolutePath(), "module relational_lib\n"
				+ "fun last[elem: univ, next:univ->univ]: one univ { elem - (next.elem) }\n"
				+ "fun max [es: set univ, next: univ->univ ]: lone univ { es - es.^(~(next)) }\n"
				+ "fun min [es: set univ, next: univ->univ ]: lone univ { es - es.^(next) }\n"
				+ "pred lt [e1, e2: univ, next:univ->univ ] { e1 in this/prevs[e2, next] }\n"
				+ "fun prevs [e: univ, next:univ->univ ]: set univ { e.^(~(next)) }\n");

		List<File> dependentFiles = new ArrayList<>();
		dependentFiles.add(relationalLib);
		File correctedModel = new File(
				"models/debugger/casestudy/journal/correctedlist.als");

		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				dependentFiles, testingHost, DebuggerAlgorithmRandom.EMPTY_ALGORITHM);

		runner.approximator = new Approximator(runner.approximator.interfacE,
				runner.approximator.processManager,
				runner.approximator.patternToProperty,
				runner.approximator.tmpLocalDirectory, toBeAnalyzedCode,
				relationalPropModuleOriginal, temporalPropModuleOriginal,
				dependentFiles);
		runner.debuggerAlgorithm.approximator = runner.approximator;
		assertEquals(1, runner.debuggerAlgorithm.model.size());
		assertEquals(1, runner.debuggerAlgorithm.fields.size());
		runner.start();
		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationAllProps() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(tmpLocalDirectory,
				"toBeAnalyzedCode.als");
		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(),
				"sig A{r: one A}\n pred p[]{  some A and no A.r}\nrun {p implies some A}");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/correctedlist.als");

		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationList() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/correctedlist.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationListConsistent() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/correctedlist.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		System.out.println(runner.debuggerAlgorithm.approximator
				.weakestInconsistentApproximation("acyclic", "nxt", ""));

		// runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestRandomApproximationListMocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/correctedlist.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		Map<String, List<Pair<String, String>>> listProperties = new HashMap<>();
		listProperties.put("this/lowerBoudnxt for 3", Collections.emptyList());

		listProperties.put("this/acyclicnxt for 3", new ArrayList<>());
		listProperties.get("this/acyclicnxt for 3")
				.add(new Pair<>("acyclic", "acyclic[nxt, Node]"));

		listProperties.put("this/structuralConstraintnxt for 3", new ArrayList<>());
		listProperties.get("this/structuralConstraintnxt for 3")
				.add(new Pair<>("function", "function[nxt, Node]"));

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				System.out.println(statement + fieldLabel + scope);
				return listProperties.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;
		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestHeuristicApproximationListMocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/correctedlist.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);

		// change the debugger algorithm in runner
		runner.debuggerAlgorithm = DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM
				.createIt(runner.toBeAnalyzedCode, runner.tmpLocalDirectory,
						runner.approximator, runner.oracle, runner.exampleFinder);

		runner.start();

		Map<String, List<Pair<String, String>>> listProperties = new HashMap<>();
		listProperties.put(" lowerBoud[ ]nxt for 3", Collections.emptyList());

		listProperties.put(" acyclic[ ]nxt for 3", new ArrayList<>());
		listProperties.get(" acyclic[ ]nxt for 3")
				.add(new Pair<>("acyclic", "acyclic[nxt, Node]"));

		listProperties.put(" structuralConstraint[ ]nxt for 3", new ArrayList<>());
		listProperties.get(" structuralConstraint[ ]nxt for 3")
				.add(new Pair<>("function", "function[nxt, Node]"));

		Map<String, List<Pair<String, String>>> weakestIncon = new HashMap<>();
		weakestIncon.put(" structuralConstraint[ ]nxt for 3",
				Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		weakestIncon.put(" acyclic[ ]nxt for 3",
				Arrays.asList(new Pair<>("symmetric", "symmetric[nxt, Node, Node]"),
						new Pair<>("stronglyConnected",
								"stronglyConnected[nxt, Node, Node]"),
						new Pair<>("total", "total[nxt, Node]"),
						new Pair<>("surjective", "surjective[nxt, Node]")));
		weakestIncon.put(" lowerBoud[ ]nxt for 3",
				Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		Map<String, Boolean> isIncon = new HashMap<>();
		isIncon.put(
				"( ( ( ( ( structuralConstraint[ ] ) and ( acyclic[ ] ) and ( lowerBoud[ ] ) )  =>   allReachable[ ] )  ) )nxt for 3",
				false);

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				System.out.println(statement + fieldLabel + scope);
				return listProperties.get(statement + fieldLabel + scope);
			}

			@Override
			public List<Pair<String, String>> weakestInconsistentApproximation(
					String statement, String fieldLabel, String scope) {
				return weakestIncon.get(statement + fieldLabel + scope);
			}

			@Override
			public Boolean isInconsistent(String statement, String fieldLabel,
					String scope) {
				return isIncon.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;
		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationBinaryTreeRadonom() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/binary_tree.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected_binary_tree.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationBinaryTreeHeuristicMocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/binary_tree.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected_binary_tree.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		//@formatter:off
		Map<String, List<Pair<String, String>> > strongestImpl = new HashMap<>();
		strongestImpl.put(" structuralConstraint[ ]right for 3", Arrays.asList(new Pair<>("functional", "functional[right, Node]")));
		strongestImpl.put(" acyclic[ ]right for 3", Arrays.asList(new Pair<>("acyclic", "acyclic[right, Node]")));
		strongestImpl.put(" distinctChildren[ ]right for 3", Arrays.asList());
		strongestImpl.put(" lowerBoud[ ]right for 3", Arrays.asList());
		strongestImpl.put(" structuralConstraint[ ]left for 3", Arrays.asList(new Pair<>("functional", "functional[left, Node]")));
		strongestImpl.put(" acyclic[ ]left for 3", Arrays.asList(new Pair<>("acyclic", "acyclic[left, Node]")));
		strongestImpl.put(" distinctChildren[ ]left for 3", Arrays.asList());
		strongestImpl.put(" lowerBoud[ ]left for 3", Arrays.asList());
		Map<String, List<Pair<String, String>> > strongestCon = new HashMap<>();
		Map<String, List<Pair<String, String>> > weakestIncon = new HashMap<>();
		weakestIncon.put(" structuralConstraint[ ]right for 3", Arrays.asList(new Pair<>("empty", "empty[right]")));
		weakestIncon.put(" acyclic[ ]right for 3", Arrays.asList(new Pair<>("stronglyConnected", "stronglyConnected[right, Node, Node]"), new Pair<>("symmetric", "symmetric[right, Node, Node]"), new Pair<>("total", "total[right, Node]"), new Pair<>("surjective", "surjective[right, Node]")));
		weakestIncon.put(" distinctChildren[ ]right for 3", Arrays.asList(new Pair<>("empty", "empty[right]")));
		weakestIncon.put(" lowerBoud[ ]right for 3", Arrays.asList(new Pair<>("empty", "empty[right]")));
		weakestIncon.put(" structuralConstraint[ ]left for 3", Arrays.asList(new Pair<>("empty", "empty[left]")));
		weakestIncon.put(" acyclic[ ]left for 3", Arrays.asList(new Pair<>("stronglyConnected", "stronglyConnected[left, Node, Node]"), new Pair<>("symmetric", "symmetric[left, Node, Node]"), new Pair<>("total", "total[left, Node]"), new Pair<>("surjective", "surjective[left, Node]")));
		weakestIncon.put(" distinctChildren[ ]left for 3", Arrays.asList(new Pair<>("empty", "empty[left]")));
		weakestIncon.put(" lowerBoud[ ]left for 3", Arrays.asList(new Pair<>("empty", "empty[left]")));
		Map<String, Boolean > isIncon = new HashMap<>();
		isIncon.put("( ( !( ( ( ( structuralConstraint[ ] ) and ( acyclic[ ] ) and ( distinctChildren[ ] ) and ( lowerBoud[ ] ) )  =>   allReachable[ ] ) ) ) )right for 3", true);
		isIncon.put("( ( !( ( ( ( structuralConstraint[ ] ) and ( acyclic[ ] ) and ( distinctChildren[ ] ) and ( lowerBoud[ ] ) )  =>   allReachable[ ] ) ) ) )left for 3", true);
		//@formatter:on

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				System.out.println(statement + fieldLabel + scope);
				return strongestImpl.get(statement + fieldLabel + scope);
			}

			@Override
			public List<Pair<String, String>> weakestInconsistentApproximation(
					String statement, String fieldLabel, String scope) {
				return weakestIncon.get(statement + fieldLabel + scope);
			}

			@Override
			public Boolean isInconsistent(String statement, String fieldLabel,
					String scope) {
				return isIncon.get(statement + fieldLabel + scope);
			}

			@Override
			public List<Pair<String, String>> strongestConsistentApproximation(
					String statement, String fieldLabel, String scope) {
				return strongestCon.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationBinaryTreeHeuristic() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/binary_tree.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected_binary_tree.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

}
