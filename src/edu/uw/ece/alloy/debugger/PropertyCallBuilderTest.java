package edu.uw.ece.alloy.debugger;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.uw.ece.alloy.util.Utils;

public class PropertyCallBuilderTest {

	static CompModule world;
	final static String AlloyTmpTestPath = "tmp/testing.als";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// @formatter:off
		final String alloyTestCode = 
				 "open util/ordering [A] \n"
				+ "open util/ordering [D] \n"
				+ "open util/ordering [B] as bo \n"
				+ "sig B,C{}\n"
				+ "sig A{r:B}\n"
				+ "sig D{s:B->C, w:A}\n" 
				+ "sig E{t:A}\n" + "sig F{g:B}\n" 
				+ "sig H{i:A->B}\n"
				
				+ "pred p1[r:univ->univ, left:univ]{}\n"
				+ "pred p2[r:univ->univ->univ, left:univ]{}\n"
				+ "pred p3[r:univ->univ, left:univ,right:univ]{}\n"
				+ "pred p4[r:univ->univ->univ, left,middle,right:univ]{}\n"
				+ "pred p5[r:univ->univ->univ, left,middle,right:univ, left_first:univ, left_next:univ->univ, middle_first:univ, middle_next:univ->univ]{}\n"
				+ "pred p6[a:univ->univ->univ, left,middle,right:univ, left_first:univ, left_next:univ->univ, middle_first:univ, middle_next:univ->univ, right_first:univ, right_next:univ->univ]{}\n"
				+ "run{}";
		// @formatter:on

		Util.writeAll(AlloyTmpTestPath, alloyTestCode);

		world = CompUtil.parseEverything_fromFile(A4Reporter.NOP, null,
				AlloyTmpTestPath);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		(new File(AlloyTmpTestPath)).delete();

	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddPropertyDeclration() {
		// fail("Not yet implemented");
	}

	@Test
	public void testMakeAllBinaryProperties() {
		PropertyCallBuilder pcb = new PropertyCallBuilder();
		final List<Field> fields = world.getAllReachableSigs().stream()
				.map(a -> a.getFields().makeCopy()).filter(a -> a.size() > 0)
				.flatMap(a -> a.stream()).collect(Collectors.toList());

		// System.out.println(world.getAllSigs().get(5).getFields());
		// System.out.println(world());
		pcb.makeAllBinaryProperties(fields.get(0));
	}

	@Test
	public void testMakeAllTernaryProperties() {
		final PropertyCallBuilder pcb = new PropertyCallBuilder();
		final List<Field> fields = world.getAllReachableSigs().stream()
				.map(a -> a.getFields().makeCopy()).filter(a -> a.size() > 0)
				.flatMap(a -> a.stream()).collect(Collectors.toList());

		System.out.println(PropertyDeclaration.findOrderingName(fields.get(1), 1,
				world.getOpens()));

		for (Func func : world.getAllFunc()) {
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
			}
		}

		System.out.println(fields.get(1));

		System.out
				.println(pcb.makeAllTernaryProperties(fields.get(1), world.getOpens()));
		
		System.out.println(Utils.readFile(AlloyTmpTestPath));
		
	}

	@Test
	public void testSelfTagged() throws Err {
		final PropertyCallBuilder pcb = new PropertyCallBuilder();
		final CompModule world_prop = CompUtil.parseEverything_fromFile(
				A4Reporter.NOP, null,
				"/Users/vajih/Documents/workspace-git/alloy/models/debugger/"
						+ "selftagged/relational_properties_tagged.als");

		for (Func func : world_prop.getAllFunc()) {
			try {
				pcb.addPropertyDeclration(func);
			} catch (IllegalArgumentException ia) {
			}
		}
		
		final List<Field> fields = world.getAllReachableSigs().stream()
				.map(a -> a.getFields().makeCopy()).filter(a -> a.size() > 0)
				.flatMap(a -> a.stream()).collect(Collectors.toList());
		
		System.out
		.println(pcb.makeAllTernaryProperties(fields.get(1), world.getOpens()));
		
		System.out
		.println(pcb.makeAllBinaryProperties(fields.get(0)));
				
	}

}
