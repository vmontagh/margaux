/**
 * 
 */
package edu.uw.ece.alloy.debugger.filters;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pos;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Expr;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

/**
 * @author vajih
 *
 */
public class ExpressionWithinPosVisitorTest {

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
				+ "fact{\n some r \n}\n"
				+ "fact{\n p1[univ->univ, univ] and all a: A| some a.r\n"
				+ "some c}\n"
				+ "run{}";
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
	public void testFindExprWithinPos1() {
		try {
			Expr expr = ExpressionWithinPosVisitor.findExprWhitinPos(
					new Pos(world.pos().filename, 27, 9, 45, 9),
					world.getAllCommands().get(0).formula);
			assertTrue(expr.toString().equals("(all a | some a . (this/A <: r))"));
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindExprWithinPos2() {
		try {
			Expr expr = ExpressionWithinPosVisitor.findExprWhitinPos(
					new Pos(world.pos().filename, 0, 9, 45, 9),
					world.getAllCommands().get(0).formula);
			System.out.println(expr);
			assertTrue(expr.toString().equals(
					"AND[this/p1[univ -> univ, univ], (all a | some a . (this/A <: r))]"));
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindExprWithinPos3() {
		try {
			Expr expr = ExpressionWithinPosVisitor.findExprWhitinPos(
					new Pos(world.pos().filename, 0, 9, 45, 10),
					world.getAllCommands().get(0).formula);
			System.out.println(expr);
			assertTrue(expr.toString().equals(
					"AND[this/p1[univ -> univ, univ], (all a | some a . (this/A <: r)), some (this/B <: c)]"));
		} catch (Err e) {
			e.printStackTrace();
		}
	}

}
