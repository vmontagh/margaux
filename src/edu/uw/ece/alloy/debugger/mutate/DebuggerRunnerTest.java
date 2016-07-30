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
		allMokedApproximations.put("list", new HashMap());
		allMokedApproximations.get("list").put("allCon", new HashMap<>());
		allMokedApproximations.get("list").put("weakestCon", new HashMap<>());
		allMokedApproximations.get("list").put("isIncon", new HashMap<>());
		allMokedApproximations.get("list").put("strongestImpl", new HashMap<>());
		allMokedApproximations.get("list").put("weakestIncon", new HashMap<>());
		allMokedApproximations.get("list").put("allInCon", new HashMap<>());
		
		allMokedApproximations.get("list").get("allCon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt for 3", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("rootedOne", "rootedOne[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]"), new Pair<>("complete", "complete[nxt, Node, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list").get("allCon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt for 3", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("rootedOne", "rootedOne[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("acyclic", "acyclic[nxt, Node]"), new Pair<>("complete", "complete[nxt, Node, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		
		allMokedApproximations.get("list").get("weakestCon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt for 3", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		allMokedApproximations.get("list").get("weakestCon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt for 3", Arrays.asList(new Pair<>("irreflexive", "irreflexive[nxt, Node, Node]"), new Pair<>("weaklyConnected", "weaklyConnected[nxt, Node, Node]"), new Pair<>("transitive", "transitive[nxt, Node, Node]"), new Pair<>("functional", "functional[nxt, Node]"), new Pair<>("injective", "injective[nxt, Node]"), new Pair<>("antisymmetric", "antisymmetric[nxt, Node, Node]")));
		
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraintNxt[ ] ) and ( structuralConstraintVal[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )nxt", true);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraintNxt[ ] ) and ( structuralConstraintVal[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )val", true);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraintNxtFixed[ ] ) and ( structuralConstraintVal[ ] ) and ( allreachable[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )nxt", true);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraintNxtFixed[ ] ) and ( structuralConstraintVal[ ] ) and ( allreachable[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )val", true);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraintNxtFixed[ ] ) and ( structuralConstraintVal[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )nxt", false);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( !( ( ( ( noLoop[ ] ) and ( structuralConstraintNxtFixed[ ] ) and ( structuralConstraintVal[ ] ) and ( lowerBound[ ] ) and ( sorted[ ] ) )  =>   rootIsLowest[ ] ) ) ) )val", false);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( declarativeFormulaForNext[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) and ( singleHead[ ] ) )nxt for 3", true);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( declarativeFormulaForNext[ ] ) and ( acyclic[ ] ) )nxt for 3", true);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) )nxt for 3", false);
		allMokedApproximations.get("list").get("isIncon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt for 3", false);
		
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" lowerBound[ ]nxt", Arrays.asList());
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" lowerBound[ ]val", Arrays.asList(new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" noLoop[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" noLoop[ ]val", Arrays.asList(new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" sorted[ ]nxt", Arrays.asList());
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" sorted[ ]val", Arrays.asList(new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" structuralConstraintNxt[ ]nxt", Arrays.asList(new Pair<>("function", "function[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" structuralConstraintNxt[ ]val", Arrays.asList(new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" structuralConstraintNxtFixed[ ]nxt", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" structuralConstraintNxtFixed[ ]val", Arrays.asList(new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" structuralConstraintVal[ ]nxt", Arrays.asList());
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" structuralConstraintVal[ ]val", Arrays.asList(new Pair<>("function", "function[val, Node]"), new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" allreachable[ ]val", Arrays.asList(new Pair<>("acyclic", "acyclic[val, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" allreachable[ ]nxt", Arrays.asList(new Pair<>("rootedOne", "rootedOne[nxt, Node, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" declarativeFormulaForNext[ ]nxt for 3", Arrays.asList(new Pair<>("function", "function[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" acyclic[ ]nxt for 3", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" connected[ ]nxt for 3", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" singleHead[ ]nxt for 3", Arrays.asList(new Pair<>("injective", "injective[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" declarativeFormulaForNext_fixed[ ]nxt for 3", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		allMokedApproximations.get("list").get("strongestImpl")
			.put(" connected[ ]nxt for 3", Arrays.asList(new Pair<>("functional", "functional[nxt, Node]")));
		
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" lowerBound[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" lowerBound[ ]val", Arrays.asList(new Pair<>("bijection", "bijection[val, Node, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" noLoop[ ]nxt", Arrays.asList(new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" noLoop[ ]val", Arrays.asList(new Pair<>("bijection", "bijection[val, Node, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" sorted[ ]nxt", Arrays.asList(new Pair<>("partialOrder", "partialOrder[nxt, Node, Node]"), new Pair<>("function", "function[nxt, Node]"), new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" sorted[ ]val", Arrays.asList(new Pair<>("bijection", "bijection[val, Node, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" structuralConstraintNxt[ ]nxt", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" structuralConstraintNxt[ ]val", Arrays.asList(new Pair<>("bijection", "bijection[val, Node, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" structuralConstraintNxtFixed[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" structuralConstraintNxtFixed[ ]val", Arrays.asList(new Pair<>("bijection", "bijection[val, Node, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" structuralConstraintVal[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" structuralConstraintVal[ ]val", Arrays.asList(new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("weaklyConnected", "weaklyConnected[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("surjective", "surjective[val, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" allreachable[ ]nxt", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" allreachable[ ]val", Arrays.asList(new Pair<>("bijection", "bijection[val, Node, Int]"), new Pair<>("antisymmetric", "antisymmetric[val, Node, Int]"), new Pair<>("reflexive", "reflexive[val, Node]"), new Pair<>("irreflexive", "irreflexive[val, Node, Int]"), new Pair<>("symmetric", "symmetric[val, Node, Int]"), new Pair<>("transitive", "transitive[val, Node, Int]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" declarativeFormulaForNext[ ]nxt for 3", Arrays.asList(new Pair<>("acyclic", "acyclic[nxt, Node]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" acyclic[ ]nxt for 3", Arrays.asList(new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" connected[ ]nxt for 3", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" singleHead[ ]nxt for 3", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put(" declarativeFormulaForNext_fixed[ ]nxt for 3", Arrays.asList(new Pair<>("empty", "empty[nxt]")));
		allMokedApproximations.get("list").get("weakestIncon")
			.put("( ( declarativeFormulaForNext_fixed[ ] ) and ( acyclic[ ] ) and ( connected[ ] ) )nxt for 3", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("symmetric", "symmetric[nxt, Node, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]")));
		allMokedApproximations.get("list").get("weakestIncon")
		.put(" connected[ ]nxt for 3", Arrays.asList(new Pair<>("total", "total[nxt, Node]"), new Pair<>("surjective", "surjective[nxt, Node]"), new Pair<>("stronglyConnected", "stronglyConnected[nxt, Node, Node]"), new Pair<>("empty", "empty[nxt]")));
		
		
		// mocking Dining philosophers 
		allMokedApproximations.put("dph", new HashMap());
		allMokedApproximations.get("dph").put("allCon", new HashMap<>());
		allMokedApproximations.get("dph").put("weakestCon", new HashMap<>());
		allMokedApproximations.get("dph").put("isIncon", new HashMap<>());
		allMokedApproximations.get("dph").put("strongestImpl", new HashMap<>());
		allMokedApproximations.get("dph").put("weakestIncon", new HashMap<>());
		allMokedApproximations.get("dph").put("allInCon", new HashMap<>());
		
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", Arrays.asList());
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", Arrays.asList());
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "SzGrwtStrc_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "SzGrwtStrc_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[waits,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("allCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", Arrays.asList(new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptStrt_", "SzGrwt_Lcl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Lcl_SdMdl_EmptNon_", "SzGrwt_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "SzGrwtStrc_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdIncrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdIncrsStrc_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Glbl_SdMdl_EmptStrt_", "OrdIncrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_", "OrdDcrsStrc_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrsStrc_SzGrwt_Lcl_SdMdl_EmptStrt_", "OrdDcrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_", "OrdIncrsStrc_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));

		
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestCon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", Arrays.asList(new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));

		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder_2[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder_2[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder_3[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder_3[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder_4[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabOrRelease[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("strongestImpl").put(" GrabbedInOrder_4[ ]holds", Arrays.asList());
		
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptNon_", "SzGrwt_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptNon_", "SzGrwt_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder[ ]holds", Arrays.asList(new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder[ ]holds", Arrays.asList(new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdMdl_EmptStrt_", "SzGrwt_Glbl_SdMdl_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdIncrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwt_Glbl_SdEnd_EmptStrt_", "SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdIncrs_SzGrwt_Lcl_SdMdl_EmptNon_", "OrdIncrs_SzGrwt_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwt_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwt_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_2[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_2[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_2[ ]holds", Arrays.asList(new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_2[ ]holds", Arrays.asList(new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_3[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_3[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_3[ ]holds", Arrays.asList(new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_3[ ]holds", Arrays.asList(new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Glbl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Glbl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]waits", Arrays.asList(new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdEnd_EmptNon_", "SzGrwtStrc_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdEnd_EmptNon_", "SzGrwtStrc_Lcl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Glbl_SdMdl_EmptNon_", "SzGrwtStrc_Glbl_SdMdl_EmptNon_[waits,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[waits,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_4[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_4[ ]waits", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" lowerBoundProcess[ ]holds", Arrays.asList());
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabOrRelease[ ]holds", Arrays.asList(new Pair<>("SzNChng_Glbl_SdEnd_EmptNon_", "SzNChng_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzNChng_Glbl_SdMdl_EmptNon_", "SzNChng_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzGrwtStrc_Lcl_SdMdl_EmptNon_", "SzGrwtStrc_Lcl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdMdl_EmptNon_", "SzShrnk_Glbl_SdMdl_EmptNon_[holds,State,Process,Mutex,so/first,so/next]"), new Pair<>("SzShrnk_Glbl_SdEnd_EmptNon_", "SzShrnk_Glbl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_4[ ]holds", Arrays.asList(new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		allMokedApproximations.get("dph").get("weakestIncon").put(" GrabbedInOrder_4[ ]holds", Arrays.asList(new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptNon_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]"), new Pair<>("OrdDcrs_SzGrwtStrc_Lcl_SdMdl_EmptNon_", "OrdDcrs_SzGrwtStrc_Lcl_SdEnd_EmptStrt_[holds,State,Process,Mutex,so/first,so/next,mo/first,mo/next]")));
		
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )holds", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder[ ] ) )waits", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )holds", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_2[ ] ) )waits", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )holds", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_3[ ] ) )waits", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )holds", false);
		allMokedApproximations.get("dph").get("isIncon").put("( ( lowerBoundProcess[ ] ) and ( GrabOrRelease[ ] ) and ( GrabbedInOrder_4[ ] ) )waits", false);


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
		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel, dependentFiles, tmpFolder, testingHost,
				DebuggerAlgorithmRandom.EMPTY_ALGORITHM, new File("!~@#"), reviewed, new File("!~@#"));

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

		DebuggerRunner runner = new DebuggerRunner(toBeAnalyzedCode, correctedModel,
				testingHost, DebuggerAlgorithmRandom.EMPTY_ALGORITHM, reviewed);
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
				return ((Boolean) allMokedApproximations.get(mockedName).get("isIncon")
						.get(statement + fieldLabel));
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
				new File(tmpFolder, skipTerms), DebuggerAlgorithmHeuristics.EMPTY_ALGORITHM);
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
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList1() {
		testHeuristic("list.v0.bug1.als", "corrected.list.v0.bug1.als", "list.v0.reviewed.txt", "list.v0.bug1.reviewed.txt", "list.v0.skip.txt");
	}

	@Test
	public void testHeuristicMockedList1() {
		testHeuristicMocked("list.v0.bug1.als", "corrected.list.v0.bug1.als", "list.v0.reviewed.txt", "list.v0.bug1.reviewed.txt", "list.v0.skip.txt", "list");
	}

	@Test
	public void testRandomList1() {
		testRandom("list.v0.bug1.als", "corrected.list.v0.bug1.als", "list.v0.reviewed.txt", "list.v0.bug1.reviewed.txt", "list.v0.skip.txt");

	}

	@Test
	public void testRandomMockedList1() {
		testRandomMocked("list.v0.bug1.als", "corrected.list.v0.bug1.als", "list.v0.reviewed.txt", "list.v0.bug1.reviewed.txt", "list.v0.skip.txt", "list");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 2
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList2() {
		testHeuristic("list.v0.bug2.als", "corrected.list.v0.bug2.als", "list.v0.bug1.reviewed.txt", "list.v0.bug2.reviewed.txt", "list.v0.skip.txt");
	}

	@Test
	public void testHeuristicMockedList2() {
		testHeuristicMocked("list.v0.bug2.als", "corrected.list.v0.bug2.als", "list.v0.bug1.reviewed.txt", "list.v0.bug2.reviewed.txt", "list.v0.skip.txt", "list");
	}

	@Test
	public void testRandomList2() {
		testRandom("list.v0.bug2.als", "corrected.list.v0.bug2.als", "list.v0.bug1.reviewed.txt", "list.v0.bug2.reviewed.txt", "list.v0.skip.txt");

	}

	@Test
	public void testRandomMockedList2() {
		testRandomMocked("list.v0.bug2.als", "corrected.list.v0.bug2.als", "list.v0.bug1.reviewed.txt", "list.v0.bug2.reviewed.txt", "list.v0.skip.txt", "list");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 3
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicList3() {
		testHeuristic("list.v0.bug3.als", "corrected.list.v0.bug3.als", "list.v0.bug2.reviewed.txt", "list.v0.bug3.reviewed.txt", "list.v0.skip.txt");
	}

	@Test
	public void testHeuristicMockedList3() {
		testHeuristicMocked("list.v0.bug3.als", "corrected.list.v0.bug3.als", "list.v0.bug2.reviewed.txt", "list.v0.bug3.reviewed.txt", "list.v0.skip.txt", "list");
	}

	@Test
	public void testRandomList3() {
		testRandom("list.v0.bug3.als", "corrected.list.v0.bug3.als", "list.v0.bug2.reviewed.txt", "list.v0.bug3.reviewed.txt", "list.v0.skip.txt");

	}

	@Test
	public void testRandomMockedList3() {
		testRandomMocked("list.v0.bug3.als", "corrected.list.v0.bug3.als", "list.v0.bug2.reviewed.txt", "list.v0.bug3.reviewed.txt", "list.v0.skip.txt", "list");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// Dijkstra's Dining Philosophers: DPH
	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 1
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDPH1() {
		testHeuristic("dijkstra.bug1.als", "corrected.dijkstra.bug1.als", "dijkstra.reviewed.txt", "dijkstra.bug1.reviewed.txt", "dijkstra.skip.txt");
	}

	@Test
	public void testHeuristicMockedDPH1() {
		testHeuristicMocked("dijkstra.bug1.als", "corrected.dijkstra.bug1.als", "dijkstra.reviewed.txt", "dijkstra.bug1.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	@Test
	public void testRandomDPH1() {
		testRandom("dijkstra.bug1.als", "corrected.dijkstra.bug1.als", "dijkstra.reviewed.txt", "dijkstra.bug1.reviewed.txt", "dijkstra.skip.txt");

	}

	@Test
	public void testRandomMockedDPH1() {
		testRandomMocked("dijkstra.bug1.als", "corrected.dijkstra.bug1.als", "dijkstra.reviewed.txt", "dijkstra.bug1.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 2
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDPH2() {
		testHeuristic("dijkstra.bug2.als", "corrected.dijkstra.bug2.als", "dijkstra.bug1.reviewed.txt", "dijkstra.bug2.reviewed.txt", "dijkstra.skip.txt");
	}

	@Test
	public void testHeuristicMockedDPH2() {
		testHeuristicMocked("dijkstra.bug2.als", "corrected.dijkstra.bug2.als", "dijkstra.bug1.reviewed.txt", "dijkstra.bug2.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	@Test
	public void testRandomDPH2() {
		testRandom("dijkstra.bug2.als", "corrected.dijkstra.bug2.als", "dijkstra.bug1.reviewed.txt", "dijkstra.bug2.reviewed.txt", "dijkstra.skip.txt");

	}

	@Test
	public void testRandomMockedDPH2() {
		testRandomMocked("dijkstra.bug2.als", "corrected.dijkstra.bug2.als", "dijkstra.bug1.reviewed.txt", "dijkstra.bug2.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 3
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDPH3() {
		testHeuristic("dijkstra.bug3.als", "corrected.dijkstra.bug3.als", "dijkstra.bug2.reviewed.txt", "dijkstra.bug3.reviewed.txt", "dijkstra.skip.txt");
	}

	@Test
	public void testHeuristicMockedDPH3() {
		testHeuristicMocked("dijkstra.bug3.als", "corrected.dijkstra.bug3.als", "dijkstra.bug2.reviewed.txt", "dijkstra.bug3.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	@Test
	public void testRandomDPH3() {
		testRandom("dijkstra.bug3.als", "corrected.dijkstra.bug3.als", "dijkstra.bug2.reviewed.txt", "dijkstra.bug3.reviewed.txt", "dijkstra.skip.txt");

	}

	@Test
	public void testRandomMockedDPH3() {
		testRandomMocked("dijkstra.bug3.als", "corrected.dijkstra.bug3.als", "dijkstra.bug2.reviewed.txt", "dijkstra.bug3.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// bug 4
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHeuristicDPH4() {
		testHeuristic("dijkstra.bug4.als", "corrected.dijkstra.bug4.als", "dijkstra.bug3.reviewed.txt", "dijkstra.bug4.reviewed.txt", "dijkstra.skip.txt");
	}

	@Test
	public void testHeuristicMockedDPH4() {
		testHeuristicMocked("dijkstra.bug4.als", "corrected.dijkstra.bug4.als", "dijkstra.bug3.reviewed.txt", "dijkstra.bug4.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

	@Test
	public void testRandomDPH4() {
		testRandom("dijkstra.bug4.als", "corrected.dijkstra.bug4.als", "dijkstra.bug3.reviewed.txt", "dijkstra.bug4.reviewed.txt", "dijkstra.skip.txt");

	}

	@Test
	public void testRandomMockedDPH4() {
		testRandomMocked("dijkstra.bug4.als", "corrected.dijkstra.bug4.als", "dijkstra.bug3.reviewed.txt", "dijkstra.bug4.reviewed.txt", "dijkstra.skip.txt", "dph");
	}

}
