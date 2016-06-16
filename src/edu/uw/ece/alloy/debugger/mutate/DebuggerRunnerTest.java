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

	static Map<String, List<Pair<String, String>>> listProperties;
	static Map<String, List<Pair<String, String>>> listWeakestIncon;
	static Map<String, Boolean> listIsIncon;
	static Map<String, List<Pair<String, String>>> binaryTreeStrongestImpl;
	static Map<String, List<Pair<String, String>>> binaryTreeStrongestCon;
	static Map<String, List<Pair<String, String>>> binaryWeakestIncon;
	static Map<String, Boolean> binaryTreeIsIncon;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//@formatter:off
		listProperties = new HashMap<>();
		
		
		listProperties.put(" lowerBound[ ]val", Arrays.asList(new Pair<>("function", "function[val, Node]"), new Pair<>("acyclic", "acyclic[val, Node]")));
		listProperties.put(" noLoop[ ]val", Arrays.asList(new Pair<>("function", "function[val, Node]"), new Pair<>("acyclic", "acyclic[val, Node]")));
		listProperties.put(" sorted[ ]val", Arrays.asList(new Pair<>("function", "function[val, Node]"), new Pair<>("acyclic", "acyclic[val, Node]")));
		listProperties.put(" structuralConstraint[ ]val", Arrays.asList(new Pair<>("function", "function[val, Node]"), new Pair<>("acyclic", "acyclic[val, Node]")));
		listProperties.put(" structuralConstraint[ ]nxt", Arrays.asList(new Pair<>("function", "function[nxt, Node]")));
		listProperties.put(" sorted[ ]nxt", Arrays.asList(new Pair<>("function", "function[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]")));
		listProperties.put(" lowerBound[ ]nxt", Arrays.asList(new Pair<>("function", "function[nxt, Node]")));
		listProperties.put(" noLoop[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]")));
		listProperties.put(" structuralConstraintFixed[ ]val", Arrays.asList(new Pair<>("function", "function[val, Node]"), new Pair<>("acyclic", "acyclic[val, Node]")));
		listProperties.put(" structuralConstraintFixed[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]")));
		listProperties.put(" lowerBoud[ ]nxt for 3", Collections.emptyList());	
		listProperties.put(" acyclic[ ]nxt for 3", new ArrayList<>());
		listProperties.get(" acyclic[ ]nxt for 3").add(new Pair<>("acyclic", "acyclic[nxt, Node]"));
		listProperties.put(" structuralConstraint[ ]nxt for 3", new ArrayList<>());
		listProperties.get(" structuralConstraint[ ]nxt for 3").add(new Pair<>("function", "function[nxt, Node]"));

		listWeakestIncon = new HashMap<>();
		listWeakestIncon.put(" noLoop[ ]nxt", Arrays.asList(new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]")));
		listWeakestIncon.put(" structuralConstraint[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		listWeakestIncon.put(" lowerBound[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		listWeakestIncon.put(" sorted[ ]nxt", Arrays.asList(new Pair<>("partialOrder", "partialOrder[nxt, Node, Node]"), new Pair<>("function", "function[nxt, Node]"), new Pair<>("empty", "empty[nxt]")));
		listWeakestIncon.put(" noLoop[ ]val", Arrays.asList(new Pair<>("surjective", "surjective[val, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("stronglyConnected", "stronglyConnected[val, Node, Int]")));
		listWeakestIncon.put(" structuralConstraint[ ]val", Arrays.asList(new Pair<>("bijective", "bijective[val, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("weaklyConnected", "weaklyConnected[val, Node, Int]")));
		listWeakestIncon.put(" lowerBound[ ]val", Arrays.asList(new Pair<>("weaklyConnected", "weaklyConnected[val, Node, Int]"), new Pair<>("surjective", "surjective[val, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		listWeakestIncon.put(" sorted[ ]val", Arrays.asList(new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("weaklyConnected", "weaklyConnected[val, Node, Int]"), new Pair<>("function", "function[val, Node]"), new Pair<>("surjective", "surjective[val, Int]")));
		Map<String, Boolean > listIsIncon = new HashMap<>();
		listIsIncon.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraint[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )nxt", true);
		listIsIncon.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraint[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )val", true);
		
		binaryTreeStrongestImpl = new HashMap<>();
		binaryTreeStrongestImpl.put(" structuralConstraint[ ]right for 3", Arrays.asList(new Pair<>("functional", "functional[right, Node]")));
		binaryTreeStrongestImpl.put(" acyclic[ ]right for 3", Arrays.asList(new Pair<>("acyclic", "acyclic[right, Node]")));
		binaryTreeStrongestImpl.put(" distinctChildren[ ]right for 3", Arrays.asList());
		binaryTreeStrongestImpl.put(" lowerBoud[ ]right for 3", Arrays.asList());
		binaryTreeStrongestImpl.put(" structuralConstraint[ ]left for 3", Arrays.asList(new Pair<>("functional", "functional[left, Node]")));
		binaryTreeStrongestImpl.put(" acyclic[ ]left for 3", Arrays.asList(new Pair<>("acyclic", "acyclic[left, Node]")));
		binaryTreeStrongestImpl.put(" distinctChildren[ ]left for 3", Arrays.asList());
		binaryTreeStrongestImpl.put(" lowerBoud[ ]left for 3", Arrays.asList());
		binaryTreeStrongestCon = new HashMap<>();
		binaryWeakestIncon = new HashMap<>();
		binaryWeakestIncon.put(" structuralConstraint[ ]right for 3", Arrays.asList(new Pair<>("empty", "empty[right]")));
		binaryWeakestIncon.put(" acyclic[ ]right for 3", Arrays.asList(new Pair<>("stronglyConnected", "stronglyConnected[right, Node, Node]"), new Pair<>("symmetric", "symmetric[right, Node, Node]"), new Pair<>("total", "total[right, Node]"), new Pair<>("surjective", "surjective[right, Node]")));
		binaryWeakestIncon.put(" distinctChildren[ ]right for 3", Arrays.asList(new Pair<>("empty", "empty[right]")));
		binaryWeakestIncon.put(" lowerBoud[ ]right for 3", Arrays.asList(new Pair<>("empty", "empty[right]")));
		binaryWeakestIncon.put(" structuralConstraint[ ]left for 3", Arrays.asList(new Pair<>("empty", "empty[left]")));
		binaryWeakestIncon.put(" acyclic[ ]left for 3", Arrays.asList(new Pair<>("stronglyConnected", "stronglyConnected[left, Node, Node]"), new Pair<>("symmetric", "symmetric[left, Node, Node]"), new Pair<>("total", "total[left, Node]"), new Pair<>("surjective", "surjective[left, Node]")));
		binaryWeakestIncon.put(" distinctChildren[ ]left for 3", Arrays.asList(new Pair<>("empty", "empty[left]")));
		binaryWeakestIncon.put(" lowerBoud[ ]left for 3", Arrays.asList(new Pair<>("empty", "empty[left]")));
		binaryTreeIsIncon = new HashMap<>();
		binaryTreeIsIncon.put("( ( !( ( ( ( structuralConstraint[ ] ) and ( acyclic[ ] ) and ( distinctChildren[ ] ) and ( lowerBoud[ ] ) )  =>   allReachable[ ] ) ) ) )right for 3", true);
		binaryTreeIsIncon.put("( ( !( ( ( ( structuralConstraint[ ] ) and ( acyclic[ ] ) and ( distinctChildren[ ] ) and ( lowerBoud[ ] ) )  =>   allReachable[ ] ) ) ) )left for 3", true);
		//@formatter:on
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
				"relational_properties_tagged.als");
		Util.writeAll(relationalPropModuleOriginal.getAbsolutePath(),
				"pred weaklyConnected [ r :univ->univ,  left:univ, right:univ ] {\n"
						+ "\tall d: right | all g: left - d  | d in g.^(r + ~r)\n" + "}\n"
						+ "pred rootedOne [r: univ->univ, left:univ, right: univ]{"
						+ "\tone root:left | right in root.*r" + "}");

		File temporalPropModuleOriginal = new LazyFile(tmpLocalDirectory,
				"temporal_properties_tagged.als");
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
				"models/debugger/casestudy/journal/corrected.list.v0.als");

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
				"models/debugger/casestudy/journal/corrected.list.v0.als");

		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestRandomApproximationRandomListBug1() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug1.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug1.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestRandomApproximationListBug1Mocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug1.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug1.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				return listProperties.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;
		runner.debuggerAlgorithm.run();
	}
	
	@Test
	public void testStrongestHeuristicApproximationListBug1() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug1.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug1.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}
	
	@Test
	public void testStrongestHeuristicApproximationListBug1Mocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug1.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug1.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				return listProperties.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;
		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestRandomApproximationRandomListBug2() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug2.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug2.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestRandomApproximationListBug2Mocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug2.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug2.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				return listProperties.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;
		runner.debuggerAlgorithm.run();
	}
	
	@Test
	public void testStrongestHeuristicRandomListBug2() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug2.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug2.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestHeuristicListBug2Mocked() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v1.bug2.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug2.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				return listProperties.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;
		runner.debuggerAlgorithm.run();
	}

	
	@Test
	public void testStrongestApproximationListConsistent() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/list.v0.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v0.als");
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
				"models/debugger/casestudy/journal/list.v0.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v0.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();

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
				"models/debugger/casestudy/journal/list.v1.bug1.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.list.v1.bug1.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);

		// change the debugger algorithm in runner
		runner.debuggerAlgorithm = DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM
				.createIt(runner.toBeAnalyzedCode, runner.tmpLocalDirectory,
						runner.approximator, runner.oracle, runner.exampleFinder);

		runner.start();

		/*Approximator approximatorMock = new Approximator(
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
				return listWeakestIncon.get(statement + fieldLabel + scope);
			}

			@Override
			public Boolean isInconsistent(String statement, String fieldLabel,
					String scope) {
				return listIsIncon.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;*/
		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationBinaryTreeRadonom() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/binary.tree.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.binary.tree.als");
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
				"models/debugger/casestudy/journal/binary.tree.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.binary.tree.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		Approximator approximatorMock = new Approximator(
				runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory,
				runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(
					String statement, String fieldLabel, String scope) {
				System.out.println(statement + fieldLabel + scope);
				return binaryTreeStrongestImpl.get(statement + fieldLabel + scope);
			}

			@Override
			public List<Pair<String, String>> weakestInconsistentApproximation(
					String statement, String fieldLabel, String scope) {
				return binaryWeakestIncon.get(statement + fieldLabel + scope);
			}

			@Override
			public Boolean isInconsistent(String statement, String fieldLabel,
					String scope) {
				return binaryTreeIsIncon.get(statement + fieldLabel + scope);
			}

			@Override
			public List<Pair<String, String>> strongestConsistentApproximation(
					String statement, String fieldLabel, String scope) {
				return binaryTreeStrongestCon.get(statement + fieldLabel + scope);
			}
		};

		runner.debuggerAlgorithm.approximator = approximatorMock;

		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationBinaryTreeHeuristic() throws Err {
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/binary.tree.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.binary.tree.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
		runner.start();

		runner.debuggerAlgorithm.run();
	}
	
	
	@Test
	public void testDikjstraBug1Random(){
		File tmpLocalDirectory = new File("tmp/testing");
		File toBeAnalyzedCode = new LazyFile(
				"models/debugger/casestudy/journal/dijkstra.bug1.als");
		File correctedModel = new File(
				"models/debugger/casestudy/journal/corrected.dijkstra.bug1.als");
		
		
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				Collections.emptyList(), testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
		runner.start();
		runner.debuggerAlgorithm.run();
		
	}
	
	

}
