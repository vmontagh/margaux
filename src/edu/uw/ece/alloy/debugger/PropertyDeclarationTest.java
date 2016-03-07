package edu.uw.ece.alloy.debugger;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4compiler.ast.Func;
import edu.mit.csail.sdg.alloy4compiler.parser.CompModule;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;

public class PropertyDeclarationTest {

	static CompModule world;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final String alloyTestCode = "sig S{}"
				+ "fun f:S{S}"
				+ "pred p1[a:S]{}"
				+ "pred p2[a:S, left:S]{}"
				+ "pred p3[a:S->S->S->S, left:S]{}"
				+ "pred p4[a:S->S, left:S]{}"
				+ "pred p5[a:S->S->S, left:S]{}"				
				+ "pred p6[a:S->S, left,right:S]{}"
				+ "pred p7[a:S->S, left:S,right:S]{}"
				+ "pred p8[a:S->S, left,middle,right:S]{}"
				+ "pred p9[a:S->S, left,middle:S,right:S]{}"
				+ "pred p10[a:S->S, left:S,middle,right:S]{}"
				+ "pred p11[a:S->S, right,left:S]{}"
				+ "pred p12[a:S->S, right:S, left:S]{}"
				+ "pred p13[a:S->S, left,right,middle:S]{}"
				+ "pred p14[a:S->S, middle,left:S,right:S]{}"
				+ "pred p15[a:S->S, left:S,right,middle:S]{}"
				+ "pred p16[a:S->S, left,right:S, left_first:S, left_next:S->S]{}"
				+ "pred p17[a:S->S, left,right:S, left_first:S, left_next:S->S, middle_first:S, middle_next:S->S]{}"
				+ "pred p18[a:S->S, left,right:S, left_first:S, left_next:S->S, middle_first:S, middle_next:S->S, right_first:S, right_next:S->S]{}"
				+ "pred p19[a:S->S, left,right:S, left_next:S->S, left_first:S]{}"
				+ "pred p20[a:S->S, left,right:S, left_first:S, left_next:S->S, middle_first:S, middle_next:S]{}"
				+ "pred p21[a:S->S, left,right:S, left_first:S, middle_first:S, middle_next:S->S, right_first:S, right_next:S->S]{}"
				+ "run{}";
		world = CompUtil.parseOneModule(alloyTestCode);
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
	public void testPropertyIsPred() {
		Func f = world.getAllFunc().get(0);
		PropertyDeclaration pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());
		
	}

	@Test
	public void testPropertyIsField() {
		Func f = world.getAllFunc().get(1);
		PropertyDeclaration pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());
		
		 f = world.getAllFunc().get(2);
		 pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

	}
	
	
	@Test
	public void testPropertyIsBinaryOrTernary() {
		Func f = world.getAllFunc().get(2);
		PropertyDeclaration pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		 f = world.getAllFunc().get(3);
		 pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		
		f = world.getAllFunc().get(4);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(5);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());

	}
	
	@Test
	public void testPropertyIsNamedSingletongs() {
		Func f = world.getAllFunc().get(6);
		PropertyDeclaration pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());
		
		f = world.getAllFunc().get(7);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(8);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(9);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(10);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());
		
		f = world.getAllFunc().get(11);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(12);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(13);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(14);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(15);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());
	}
	
	@Test
	public void testPropertyIsOrdered() {
		Func f = world.getAllFunc().get(16);
		PropertyDeclaration pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());
		
		f = world.getAllFunc().get(17);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());
		
		f = world.getAllFunc().get(18);
		pd = new PropertyDeclaration(f);
		assertTrue(pd.isAPropertyDefinition());
		
		f = world.getAllFunc().get(19);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(20);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());

		f = world.getAllFunc().get(21);
		pd = new PropertyDeclaration(f);
		assertFalse(pd.isAPropertyDefinition());
	}
	
}
