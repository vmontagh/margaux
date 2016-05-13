package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.EmptNon;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.Emptnes;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.Glbl;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.Lclty;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.Ord;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.OrdDcrs;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.Sd;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.SdMdl;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.SzGrwt;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.SzPrpty;
import edu.uw.ece.alloy.debugger.propgen.tripletemporal.SzShrnk;

public class TemporalPropertiesGeneratorTester {

	TemporalPropertiesGenerator object = new TemporalPropertiesGenerator();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAllinOneModule() throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, Err, FileNotFoundException, IOException {
		Sd side = object.builder.createSideInstance(SdMdl.class);
		Emptnes empty = object.builder.createEmptinessInstance(EmptNon.class);

		Lclty local = object.builder.createLocalityInstance(Glbl.class, side);
		SzPrpty size1 = object.builder.createSizeInstance(SzGrwt.class, local,
				empty);
		SzPrpty size2 = object.builder.createSizeInstance(SzShrnk.class, local,
				empty);

		Ord order1 = object.builder.createOrderInstance(OrdDcrs.class, size1);
		Ord order2 = object.builder.createOrderInstance(OrdDcrs.class, size2);

		Map<String, Pair<String, String>> preds = new TreeMap<>();
		preds.put(size1.genPredName(),
				new Pair<String, String>(size1.genPredCall(), size1.generateProp()));
		preds.put(size2.genPredName(),
				new Pair<String, String>(size2.genPredCall(), size2.generateProp()));

		System.out.println(preds);

		GeneratedStorage<AlloyProcessingParam> result = new GeneratedStorage<>();

		object.generateRelationChekers(preds, result);

		AlloyProcessingParam output = result.getGeneratedProps().get(0);

		String content = Util.readAll(output.getSrcPath().get().getAbsolutePath());
		content = content.replaceAll(object.RelationProps,
				Util.readAll(object.relationalPropModuleOriginal.getAbsolutePath()));

		AlloyProcessedResult rep = new AlloyProcessedResult(output);

		System.out.println(content);

		// Not a complete call and needed to be corrected. Nullpointerexception
		// issue.
		A4CommandExecuter.getInstance().runOverString(content, rep);

		// fail("Not yet implemented");
	}

}
