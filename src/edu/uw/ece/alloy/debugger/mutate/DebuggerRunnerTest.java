package edu.uw.ece.alloy.debugger.mutate;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.mutate.experiment.DebuggerAlgorithmHeuristics;
import edu.uw.ece.alloy.debugger.mutate.experiment.DebuggerAlgorithmRandom;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessorUtil;
import edu.uw.ece.alloy.util.LazyFile;
import edu.uw.ece.alloy.util.Utils;

/**
 * Testing DebuggerRunnerTest
 * 
 * @author vajih
 *
 */
public class DebuggerRunnerTest {

	InetSocketAddress testingHost;
	final static File testFolder = new File("models/debugger/casestudy/journal");
	final static File tmpFolder = new File("tmp/testing");

	final long startTime = System.currentTimeMillis();

	public final void print(String... args) {
		final long current = System.currentTimeMillis() - startTime;
		System.out.print(current + " - ");
		for (String arg : args)
			System.out.print(arg + " ");
		System.out.println();
	}

	// a map from the cast-study name, to mocked approximation map and rest.
	@SuppressWarnings("rawtypes")
	static Map<String, Map<String, Map>> allMokedApproximations;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//@formatter:off		

		allMokedApproximations = new HashMap<>();
		// mocking list
		allMokedApproximations.put("list.v0", new HashMap());
		allMokedApproximations.get("list.v0").put("allCon", new HashMap<>());
		allMokedApproximations.get("list.v0").put("weakestCon", new HashMap<>());
		allMokedApproximations.get("list.v0").put("isIncon", new HashMap<>());
		allMokedApproximations.get("list.v0").put("strongestImpl", new HashMap<>());
		allMokedApproximations.get("list.v0").put("weakestIncon", new HashMap<>());
		allMokedApproximations.get("list.v0").put("allInCon", new HashMap<>());
		
		allMokedApproximations.get("list.v0").get("allCon").put("( ( declarativeFormulaForNext[ ] ) and ( acyclic[ ] ) )nxt", Arrays.asList());
		allMokedApproximations.get("list.v0").get("isIncon").put("( ( declarativeFormulaForNext[ ] ) and ( acyclic[ ] ) )nxt", true);
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" declarativeFormulaForNext[ ]nxt", Arrays.asList(new Pair<>("function", "function[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("weakestCon").put("( ( declarativeFormulaForNext[ ] ) and ( acyclic[ ] ) )nxt", Arrays.asList());
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" declarativeFormulaForNext[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put("( ( declarativeFormulaForNext[ ] ) and ( acyclic[ ] ) )nxt", Arrays.asList(new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]")));

		allMokedApproximations.get("list.v0").get("allCon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("rootedOne", "rootedOne[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]"), new Pair<>("complete", "complete[nxt, Node, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("isIncon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt", false);
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" declarativeFormulaForNext_fixed[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("weakestCon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" declarativeFormulaForNext_fixed[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]")));

		allMokedApproximations.get("list.v0").get("allCon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("rootedOne", "rootedOne[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]"), new Pair<>("complete", "complete[nxt, Node, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("isIncon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", false);
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" connected[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" declarativeFormulaForNext_fixed[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("weakestCon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" connected[ ]nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" declarativeFormulaForNext_fixed[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]")));

		
		allMokedApproximations.get("list.v0").get("allCon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("rootedOne", "rootedOne[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]"), new Pair<>("complete", "complete[nxt, Node, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("isIncon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", false);
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" connected[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("strongestImpl").put(" declarativeFormulaForNext_fixed[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list.v0").get("weakestCon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" acyclic[ ]nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" connected[ ]nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put(" declarativeFormulaForNext_fixed[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list.v0").get("weakestIncon").put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]")));

		// mocking Dining philosophers 
		allMokedApproximations.put("dijkstra", new HashMap());
		allMokedApproximations.get("dijkstra").put("allCon", new HashMap<>());
		allMokedApproximations.get("dijkstra").put("weakestCon", new HashMap<>());
		allMokedApproximations.get("dijkstra").put("isIncon", new HashMap<>());
		allMokedApproximations.get("dijkstra").put("strongestImpl", new HashMap<>());
		allMokedApproximations.get("dijkstra").put("weakestIncon", new HashMap<>());
		allMokedApproximations.get("dijkstra").put("allInCon", new HashMap<>());
		
		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", false);
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", false);
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder[ ]holds", Arrays.asList(new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder[ ]holds", Arrays.asList(new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));

		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", false);
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", false);
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_2[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_2[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_2[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_2[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_2[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_2[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));

		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", false);
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", false);
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_3[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_3[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_3[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_3[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_3[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_3[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));

		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", false);
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", false);
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_4[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_4[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_4[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_4[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_4[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_4[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));


		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )holds", false);
		allMokedApproximations.get("dijkstra").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )waits", false);
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_5[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" GrabbedInOrder_5[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_5[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_5[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_5[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" GrabbedInOrder_5[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList(new Pair<>("SzShrnkStrc_Lcl_SdMdl_EmptNon_", "SzShrnkStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Lcl_SdEnd_EmptNon_", "SzShrnkStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdEnd_EmptNon_", "SzShrnkStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnkStrc_Glbl_SdMdl_EmptNon_", "SzShrnkStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dijkstra").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_5[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));

		
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
		File toBeAnalyzedCode = new LazyFile(tmpLocalDirectory, "toBeAnalyzedCode.als");
		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(),
				"sig A{r: one A}\n pred p[]{  some A and no A.r}\nrun {p implies some A}");

		File relationalPropModuleOriginal = new LazyFile(tmpLocalDirectory, "relational_properties_tagged.als");
		Util.writeAll(relationalPropModuleOriginal.getAbsolutePath(),
				"pred weaklyConnected [ r :univ->univ,  left:univ, right:univ ] {\n"
						+ "\tall d: right | all g: left - d  | d in g.^(r + ~r)\n" + "}\n"
						+ "pred rootedOne [r: univ->univ, left:univ, right: univ]{"
						+ "\tone root:left | right in root.*r" + "}");

		File temporalPropModuleOriginal = new LazyFile(tmpLocalDirectory, "temporal_properties_tagged.als");
		Util.writeAll(temporalPropModuleOriginal.getAbsolutePath(),
				"open relational_lib as relational_properties\n"
						+ "pred OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[r: univ->univ->univ, left, middle, right: univ, left_first: univ, left_next: univ->univ, right_first: univ, right_next: univ->univ]{\n"
						+ "\tall left': left - relational_properties/last[left,left_next] |let left'' = left'.left_next |\n"
						+ "\tlet c = middle.(left'.r) |let c' = middle.(left''.r) | let delta = c' - c |  (c in c' ) and   (c' !in c ) and   (some delta implies(some c implies relational_properties/lt[relational_properties/max[delta,right_next],relational_properties/min[c,right_next],right_next] ) )\n"
						+ "}");

		File relationalLib = new LazyFile(tmpLocalDirectory, "relational_lib.als");
		Util.writeAll(relationalLib.getAbsolutePath(),
				"module relational_lib\n" + "fun last[elem: univ, next:univ->univ]: one univ { elem - (next.elem) }\n"
						+ "fun max [es: set univ, next: univ->univ ]: lone univ { es - es.^(~(next)) }\n"
						+ "fun min [es: set univ, next: univ->univ ]: lone univ { es - es.^(next) }\n"
						+ "pred lt [e1, e2: univ, next:univ->univ ] { e1 in this/prevs[e2, next] }\n"
						+ "fun prevs [e: univ, next:univ->univ ]: set univ { e.^(~(next)) }\n");

		List<File> dependentFiles = new ArrayList<>();
		dependentFiles.add(relationalLib);
		File correctedModel = new File("models/debugger/casestudy/journal/corrected.list.v0.als");
		File reviewed = new File(tmpFolder, "rviewedExamples.als");
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel, dependentFiles, tmpFolder,
				testingHost, DebuggerAlgorithmRandom.EMPTY_ALGORITHM, new File("!~@#"), reviewed, new File("!~@#"));

		runner.approximator = new Approximator(runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.patternToProperty, runner.approximator.tmpLocalDirectory, toBeAnalyzedCode,
				relationalPropModuleOriginal, temporalPropModuleOriginal, dependentFiles);
		runner.debuggerAlgorithm.approximator = runner.approximator;
		assertEquals(1, runner.debuggerAlgorithm.model.size());
		assertEquals(1, runner.debuggerAlgorithm.fields.size());
		runner.start();
		runner.debuggerAlgorithm.run();
	}

	@Test
	public void testStrongestApproximationAllProps() throws Err {
		File toBeAnalyzedCode = new LazyFile(tmpFolder, "toBeAnalyzedCode.als");
		File reviewed = new File(tmpFolder, "rviewedExamples.als");
		Util.writeAll(toBeAnalyzedCode.getAbsolutePath(),
				"sig A{r: one A}\n pred p[]{  some A and no A.r}\nrun {p implies some A}");
		File correctedModel = new File("models/debugger/casestudy/journal/corrected.list.v0.als");

		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel, testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM, reviewed);
		runner.start();

		runner.debuggerAlgorithm.run();
	}

	protected Approximator createdMockedApproximator(DebuggerRunner runner, String mockedName) {
		return new Approximator(runner.approximator.interfacE, runner.approximator.processManager,
				runner.approximator.tmpLocalDirectory, runner.approximator.toBeAnalyzedCode,
				runner.approximator.dependentFiles) {

			@SuppressWarnings("unchecked")
			@Override
			public List<Pair<String, String>> strongestImplicationApproximation(String statement, String fieldLabel,
					String scope) {
				return ((List<Pair<String, String>>) allMokedApproximations.get(mockedName).get("strongestImpl")
						.get(statement + fieldLabel));
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<Pair<String, String>> weakestInconsistentApproximation(String statement, String fieldLabel,
					String scope) {
				return ((List<Pair<String, String>>) allMokedApproximations.get(mockedName).get("weakestIncon")
						.get(statement + fieldLabel));
			}

			@Override
			public Boolean isInconsistent(String statement, String fieldLabel, String scope) {
				return ((Boolean) allMokedApproximations.get(mockedName).get("isIncon").get(statement + fieldLabel));
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<Pair<String, String>> allConsistentApproximation(String statement, String fieldLabel,
					String scope) {
				return ((List<Pair<String, String>>) allMokedApproximations.get(mockedName).get("allCon")
						.get(statement + fieldLabel));
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<Pair<String, String>> allInconsistentApproximation(String statement, String fieldLabel,
					String scope) {
				return ((List<Pair<String, String>>) allMokedApproximations.get(mockedName).get("allInCon")
						.get(statement + fieldLabel));
			}

			@SuppressWarnings("unchecked")
			@Override
			public List<Pair<String, String>> weakestConsistentApproximation(String statement, String fieldLabel,
					String scope) {
				return ((List<Pair<String, String>>) allMokedApproximations.get(mockedName).get("weakestCon")
						.get(statement + fieldLabel));
			}
		};
	}

	protected DebuggerRunner createDebuggerRunner(File toBeAnalyzedCode, File correctedModel,
			final File reviewedExamples, final File newReviewedExamples, final File skipTerms,
			DebuggerAlgorithm algorithm) {
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel, testingHost, algorithm,
				reviewedExamples, newReviewedExamples, skipTerms);
		runner.start();
		return runner;
	}

	protected void runDebuggerRunner(File toBeAnalyzedCode, File correctedModel, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms, DebuggerAlgorithm algorithm) {
		createDebuggerRunner(toBeAnalyzedCode, correctedModel, reviewedExamples, newReviewedExamples, skipTerms,
				algorithm).debuggerAlgorithm.run();
	}

	protected void runDebuggerRunnerWithMockedApproximator(File toBeAnalyzedCode, File correctedModel,
			final File reviewedExamples, final File newReviewedExamples, final File skipTerms,
			DebuggerAlgorithm algorithm, String mockedTestName) {
		DebuggerRunner runner = createDebuggerRunner(toBeAnalyzedCode, correctedModel, reviewedExamples,
				newReviewedExamples, skipTerms, algorithm);
		runner.debuggerAlgorithm.approximator = createdMockedApproximator(runner, mockedTestName);
		runner.debuggerAlgorithm.run();
	}

	protected void testHeuristic(File toBeAnalyzedCode, File correctedModel, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms) {
		runDebuggerRunner(toBeAnalyzedCode, correctedModel, reviewedExamples, newReviewedExamples, skipTerms,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
	}

	protected void testHeuristicMocked(File toBeAnalyzedCode, File correctedModel, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms, String mockedTestName) {
		runDebuggerRunnerWithMockedApproximator(toBeAnalyzedCode, correctedModel, reviewedExamples, newReviewedExamples,
				skipTerms, DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM, mockedTestName);
	}

	protected void testRandom(File toBeAnalyzedCode, File correctedModel, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms) {
		runDebuggerRunner(toBeAnalyzedCode, correctedModel, reviewedExamples, newReviewedExamples, skipTerms,
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
	}

	protected void testRandomMocked(File toBeAnalyzedCode, File correctedModel, final File reviewedExamples,
			final File newReviewedExamples, final File skipTerms, String mockedTestName) {
		runDebuggerRunnerWithMockedApproximator(toBeAnalyzedCode, correctedModel, reviewedExamples, newReviewedExamples,
				skipTerms, DebuggerAlgorithmRandom.EMPTY_ALGORITHM, mockedTestName);
	}

	protected void testHeuristic(String toBeAnalyzedCode, String correctedModel, final String reviewedExamples,
			final String newReviewedExamples, final String skipTerms) {
		runDebuggerRunner(new LazyFile(testFolder, toBeAnalyzedCode), new LazyFile(testFolder, correctedModel),
				new File(tmpFolder, reviewedExamples), new File(tmpFolder, newReviewedExamples),
				new File(tmpFolder, skipTerms),

				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
	}

	protected void testHeuristicMocked(String toBeAnalyzedCode, String correctedModel, final String reviewedExamples,
			final String newReviewedExamples, final String skipTerms, String mockedTestName) {
		runDebuggerRunnerWithMockedApproximator(new LazyFile(testFolder, toBeAnalyzedCode),
				new LazyFile(testFolder, correctedModel), new File(tmpFolder, reviewedExamples),
				new File(tmpFolder, newReviewedExamples), new File(tmpFolder, skipTerms),
				DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM, mockedTestName);
	}

	protected void testRandom(String toBeAnalyzedCode, String correctedModel, final String reviewedExamples,
			final String newReviewedExamples, final String skipTerms) {
		runDebuggerRunner(new LazyFile(testFolder, toBeAnalyzedCode), new LazyFile(testFolder, correctedModel),
				new File(tmpFolder, reviewedExamples), new File(tmpFolder, newReviewedExamples),
				new File(tmpFolder, skipTerms), DebuggerAlgorithmRandom.EMPTY_ALGORITHM);
	}

	protected void testRandomMocked(String toBeAnalyzedCode, String correctedModel, final String reviewedExamples,
			final String newReviewedExamples, final String skipTerms, String mockedTestName) {
		runDebuggerRunnerWithMockedApproximator(new LazyFile(testFolder, toBeAnalyzedCode),
				new LazyFile(testFolder, correctedModel), new File(tmpFolder, reviewedExamples),
				new File(tmpFolder, newReviewedExamples), new File(tmpFolder, skipTerms),
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM, mockedTestName);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// linked list
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 1
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList1() {
		testHeuristic.accept("list.v0",1);
	}

	@Test
	public void testHeuristicMockedList1() {
		testHeuristicMocked.accept("list.v0",1);
	}

	@Test
	public void testRandomList1() {
		testRandom.accept("list.v0",1);

	}

	@Test
	public void testRandomMockedLis1() {
		testRandomMocked.accept("list.v0",1);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 2
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList2() {
		testHeuristic.accept("list.v0",2);
	}

	@Test
	public void testHeuristicMockedList2() {
		testHeuristicMocked.accept("list.v0",2);
	}

	@Test
	public void testRandomList2() {
		testRandom.accept("list.v0",2);

	}

	@Test
	public void testRandomMockedLis2() {
		testRandomMocked.accept("list.v0",2);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 3
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList3() {
		testHeuristic.accept("list.v0",3);
	}

	@Test
	public void testHeuristicMockedList3() {
		testHeuristicMocked.accept("list.v0",3);
	}

	@Test
	public void testRandomList3() {
		testRandom.accept("list.v0",3);

	}

	@Test
	public void testRandomMockedLis3() {
		testRandomMocked.accept("list.v0",3);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 4
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList4() {
		testHeuristic.accept("list.v0",4);
	}

	@Test
	public void testHeuristicMockedList4() {
		testHeuristicMocked.accept("list.v0",4);
	}

	@Test
	public void testRandomList4() {
		testRandom.accept("list.v0",4);

	}

	@Test
	public void testRandomMockedLis4() {
		testRandomMocked.accept("list.v0",4);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	BiConsumer<String, Integer> testHeuristic = (name, i) -> {
		testHeuristic(name + ".bug" + i + ".als", "corrected." + name + ".bug" + i + ".als",
				name + ".bug" + (i - 1) + ".heuristic.reviewed.txt", name + ".bug" + i + ".heuristic.reviewed.txt",
				name + ".bug" + i + ".skip.txt");
	};

	BiConsumer<String, Integer> testHeuristicMocked = (name, i) -> {
		testHeuristicMocked(name + ".bug" + i + ".als", "corrected." + name + ".bug" + i + ".als",
				name + ".bug" + (i - 1) + ".heuristic.reviewed.txt", name + ".bug" + i + ".heuristic.reviewed.txt",
				name + ".bug" + i + ".skip.txt", name);
	};

	BiConsumer<String, Integer> testRandom = (name, i) -> {
		testRandom(name + ".bug" + i + ".als", "corrected." + name + ".bug" + i + ".als",
				name + ".bug" + (i - 1) + ".random.reviewed.txt", name + ".bug" + i + ".random.reviewed.txt",
				name + ".bug" + i + ".skip.txt");

	};

	BiConsumer<String, Integer> testRandomMocked = (name, i) -> {
		testRandomMocked(name + ".bug" + i + ".als", "corrected." + name + ".bug" + i + ".als",
				name + ".bug" + (i - 1) + ".random.reviewed.txt", name + ".bug" + i + ".random.reviewed.txt",
				name + ".bug" + i + ".skip.txt", name);
	};

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//
	/////////////////////////////////////////////////////////////////////////////////////////////////

	public void benchmark(String name, String type, int bugCount, int repeatCount,
			BiConsumer<String, Integer> testSuite) {
		final File resultFile = new File(testFolder, name + "." + type + ".result.csv");
		final File archiveFolder = new File(tmpFolder, "archive");
		if (!archiveFolder.exists())
			archiveFolder.mkdirs();
		String result = resultFile.exists() ? Utils.readFile(resultFile.getAbsolutePath()) : "";
		long time = 0;
		for (Integer i = 0; i < repeatCount; ++i) {
			String experimentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
			int totalMutations = 0;
			long totalTime = 0;
			String record = name + "," + type + "," + experimentTimestamp;
			for (Integer j = 1; j <= bugCount; ++j) {
				time = System.currentTimeMillis();
				testSuite.accept(name, j);
				time = System.currentTimeMillis() - time;
				// copy to archive
				final File discriminatingExamplesFile = new File(tmpFolder,
						name + ".bug" + j + "." + type + ".reviewed.txt.detailed");
				try {
					Files.copy(discriminatingExamplesFile, new File(archiveFolder,
							discriminatingExamplesFile.getName() + "." + experimentTimestamp + ".txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				final File detailedDiscriminatingExamplesFile = new File(tmpFolder,
						name + ".bug" + j + "." + type + ".reviewed.txt.detailed");
				try {
					Files.copy(detailedDiscriminatingExamplesFile, new File(archiveFolder,
							detailedDiscriminatingExamplesFile.getName() + "." + experimentTimestamp + ".txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				totalTime += time;
				int mutationsCount = Utils.readFileLines(detailedDiscriminatingExamplesFile.getAbsolutePath()).size();
				totalMutations += mutationsCount;
				int aggregativeMutationsCount = Utils.readFileLines(discriminatingExamplesFile.getAbsolutePath())
						.size();
				record += "," + mutationsCount + "," + aggregativeMutationsCount + "," + time;
			}
			record += "," + totalMutations + "," + totalTime + "\n";
			result += record;
			try {
				Util.writeAll(resultFile.getAbsolutePath(), result);
			} catch (Err e) {
				e.printStackTrace();
			}
		}

	}
	
	@Test
	public void benchmarkListHeuristcMock() {
		benchmark("list.v0", "heuristic", 4, 5, testHeuristicMocked);
	}
	
	@Test
	public void benchmarkListRandomcMock() {
		benchmark("list.v0", "random", 4, 5, testRandomMocked);
	}
	
	@Test
	public void benchmarkDijkstraHeuristcMock() {
		benchmark("dijkstra", "heuristic", 5, 5, testHeuristicMocked);
	}
	
	@Test
	public void benchmarkDijkstraRandomcMock() {
		benchmark("dijkstra", "random", 5, 5, testRandomMocked);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Dijkstra's Dining Philosophers: DPH
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 1
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDijkstra1() {
		testHeuristic.accept("dijkstra",1);
	}

	@Test
	public void testHeuristicMockedDijkstra1() {
		testHeuristicMocked.accept("dijkstra",1);
	}

	@Test
	public void testRandomDijkstra1() {
		testRandom.accept("dijkstra",1);

	}

	@Test
	public void testRandomMockedDijkstra1() {
		testRandomMocked.accept("dijkstra",1);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 2
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDijkstra2() {
		testHeuristic.accept("dijkstra",2);
	}

	@Test
	public void testHeuristicMockedDijkstra2() {
		testHeuristicMocked.accept("dijkstra",2);
	}

	@Test
	public void testRandomDijkstra2() {
		testRandom.accept("dijkstra",2);

	}

	@Test
	public void testRandomMockedDijkstra2() {
		testRandomMocked.accept("dijkstra",2);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 3
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDijkstra3() {
		testHeuristic.accept("dijkstra",3);
	}

	@Test
	public void testHeuristicMockedDijkstra3() {
		testHeuristicMocked.accept("dijkstra",3);
	}

	@Test
	public void testRandomDijkstra3() {
		testRandom.accept("dijkstra",3);

	}

	@Test
	public void testRandomMockedDijkstra3() {
		testRandomMocked.accept("dijkstra",3);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 4
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDijkstra4() {
		testHeuristic.accept("dijkstra",4);
	}

	@Test
	public void testHeuristicMockedDijkstra4() {
		testHeuristicMocked.accept("dijkstra",4);
	}

	@Test
	public void testRandomDijkstra4() {
		testRandom.accept("dijkstra",4);

	}

	@Test
	public void testRandomMockedDijkstra4() {
		testRandomMocked.accept("dijkstra",4);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 4
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDijkstra5() {
		testHeuristic.accept("dijkstra",5);
	}

	@Test
	public void testHeuristicMockedDijkstra5() {
		testHeuristicMocked.accept("dijkstra",5);
	}

	@Test
	public void testRandomDijkstra5() {
		testRandom.accept("dijkstra",5);

	}

	@Test
	public void testRandomMockedDijkstra5() {
		testRandomMocked.accept("dijkstra",5);
	}

}
