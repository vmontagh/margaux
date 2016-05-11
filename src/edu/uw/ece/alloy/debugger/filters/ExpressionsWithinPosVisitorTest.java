package edu.uw.ece.alloy.debugger.filters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

/**
 * @author vajih
 *
 */
public class ExpressionsWithinPosVisitorTest {

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
	public void testFindAllExprsWithinPos() {
		try {
			List<Expr> exprs = ExpressionsWithinPosVisitor.findAllExprsWithinPos(
					new Pos(world.pos().filename, 27, 9, 45, 9),
					world.getAllCommands().get(0).formula);
			assertEquals(1, exprs.size());
			assertTrue(
					exprs.get(0).toString().equals("(all a | some a . (this/A <: r))"));
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindAllFieldsWithinPosOneRelation() {
		try {
			List<Sig.Field> fields = ExpressionsWithinPosVisitor
					.findAllFieldsWithinPos(new Pos(world.pos().filename, 27, 9, 45, 9),
							world.getAllCommands().get(0).formula);
			assertEquals(1, fields.size());
			String fieldName = fields.get(0).label.trim();
			assertEquals("r", fieldName);
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFindAllFieldsWithinPosIndirectRelations() {
		try {
			List<Sig.Field> fields = ExpressionsWithinPosVisitor
					.findAllFieldsWithinPos(new Pos(world.pos().filename, 1, 9, 45, 9),
							world.getAllCommands().get(0).formula);
			List<String> fieldsName = fields.stream().map(f -> f.label.trim())
					.collect(Collectors.toList());
			String[] fieldsNameArray = fieldsName
					.toArray(new String[fieldsName.size()]);
			Arrays.sort(fieldsNameArray);
			assertArrayEquals(new String[] { "c", "r" }, fieldsNameArray);
		} catch (Err e) {
			e.printStackTrace();
		}
	}

}
