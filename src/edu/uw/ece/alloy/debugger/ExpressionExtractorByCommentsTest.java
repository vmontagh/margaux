package edu.uw.ece.alloy.debugger;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;

public class ExpressionExtractorByCommentsTest {

	final static String AlloyTmpTestPath = "tmp/testing.als";
	BlocksExtractorByComments.ExtractExpression eebt;
	BlocksExtractorByComments.ExtractScope esbt;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// @formatter:off
		final String alloyTestCode = 
				 "open util/ordering [A] \n"
				+ "sig B{c: B}\n"
				+ "sig A{r:B}\n"				
				+ "pred p1[r:univ->univ, left:univ]{one c}\n"
				+ "fact{\n " + BlocksExtractorByComments.ExtractExpression.BEGIN 
				+ "\n some r " + BlocksExtractorByComments.ExtractExpression.END + " \n}\n"
				+ "fact{\n " + BlocksExtractorByComments.ExtractExpression.BEGIN
				+ " p1[univ->univ, univ] and all a: A| some a.r " 
				+ BlocksExtractorByComments.ExtractExpression.END
				+ "}\n"
				+ "run{} " +  BlocksExtractorByComments.ExtractScope.BEGIN
				+ " for 5" + BlocksExtractorByComments.ExtractScope.END;
		// @formatter:on

		Util.writeAll(AlloyTmpTestPath, alloyTestCode);		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		eebt = new BlocksExtractorByComments.ExtractExpression(AlloyTmpTestPath);
		esbt = new BlocksExtractorByComments.ExtractScope(AlloyTmpTestPath);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindAllPairs() {
		System.out.println(eebt.findAllPairs());
	}
	
	@Test
	public void testGetAllExpressions() {
		System.out.println(eebt.getAllBlocks());
	}

	@Test
	public void testGetAllExpressionsAndFields() throws Err {
		Map<String, List<Sig.Field>> map = eebt.getAllExpressionsAndFields();
		
		System.out.println(map);
		
		Map<String, List<String>> expectedMap = new HashMap<>();
		
		expectedMap.put("some r", Arrays.asList(new String[]{"r"}));
		expectedMap.put("p1[univ->univ, univ] and all a: A| some a.r", Arrays.asList(new String[]{"r","c"}));
		
		assertEquals(expectedMap.keySet().size(), map.keySet().size());
		
		for(String key: expectedMap.keySet()){
			assertNotNull(map.get(key));
			
			String[] values = map.get(key).stream().map(k->k.label.trim()).collect(Collectors.toList()).toArray(new String[0]);
			Arrays.sort(values);
			
			String[] expectedValues = expectedMap.get(key).toArray(new String[0]);
			Arrays.sort(expectedValues);
			
			assertArrayEquals(expectedValues, values);
		}
	}
	
	@Test
	public void testGetAllScopes() throws Err{
		System.out.println(esbt.getAllBlocks());
		assertArrayEquals(new String[]{" for 5"}, esbt.getAllBlocks().toArray(new String[]{}));
	}

}
