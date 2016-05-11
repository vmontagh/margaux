/**
 * 
 */
package edu.uw.ece.alloy.debugger.filters;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

/**
 * @author vajih
 *
 */
public class DecomposeTest {

	static CompModule world;
	final static String AlloyTmpTestPath = "tmp/testing.als";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// @formatter:off
		final String alloyTestCode = 
				 "open util/ordering [A] \n"
				+ "sig B{c:B}\n"
				+ "sig A{r:B}\n"				
				+ "pred p1[r:univ->univ, left:univ]{one c}\n"
				+ "pred p2[]{lone c and some c}\n"
				+ "pred p3[]{(lone c and some c) => some r}\n"
				//+ "fact{\n some r \n}\n"
				//+ "fact{\n p1[univ->univ, univ] and all a: A| some a.r}"
				//+ "//\nsome c\n}\n"
				//+ "run p2"
				+ "run p3"
				;
		// @formatter:on

		Util.writeAll(AlloyTmpTestPath, alloyTestCode);

		world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
				AlloyTmpTestPath);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDecomposetoConjunctions() {
		System.out.println(world.getAllCommands().get(0).formula);
		List<Expr> conjunctions = Decompose
				.decomposetoConjunctions(world.getAllCommands().get(0).formula);
		System.out.println(conjunctions);
	}

	@Test
	public void testDecomposetoImplications() {
		System.out.println(world.getAllCommands().get(0).formula);
		System.out.println(Decompose
				.decomposetoImplications(world.getAllCommands().get(0).formula));
	}

}
