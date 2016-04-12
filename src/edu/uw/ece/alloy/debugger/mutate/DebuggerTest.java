package edu.uw.ece.alloy.debugger.mutate;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.debugger.filters.BlocksExtractorByComments;
 
public class DebuggerTest {

	static CompModule world;
	final static String AlloyTmpTestPath = "tmp/testing.als";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// @formatter:off
		final String alloyTestCode = 
				 "open util/ordering [A] \n"
				+ "sig B{c:B}\n"
				+ "sig A{r:B}\n"				
				+ "pred p1[r:univ->univ, left:univ]{one c}\n"
				+ "pred p2[]{lone c and some c}\n"
				+ "pred p3[]{ " + BlocksExtractorByComments.ExtractExpression.BEGIN 
				+ "(lone c and some c) => some r" + BlocksExtractorByComments.ExtractExpression.END
				+ "}\n"
				//+ "fact{\n some r \n}\n"
				//+ "fact{\n p1[univ->univ, univ] and all a: A| some a.r}"
				//+ "//\nsome c\n}\n"
				//+ "run p2"
				+ "run p3 "+  BlocksExtractorByComments.ExtractScope.BEGIN
				+ " for 5" + BlocksExtractorByComments.ExtractScope.END;
				;
		// @formatter:on

		Util.writeAll(AlloyTmpTestPath, alloyTestCode);

		world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
				AlloyTmpTestPath);
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
	public void testDecomposeInput() throws Err {
		Debugger deg = new Debugger(AlloyTmpTestPath);
		System.out.println(deg);
	}

	@Test
	public void testAnalyzer() throws Err, IOException {
		Debugger deg = new Debugger(AlloyTmpTestPath);
		deg.bootRemoteAnalyzer();
		//deg.analyzeImpliedPatterns();
	}
	
}
