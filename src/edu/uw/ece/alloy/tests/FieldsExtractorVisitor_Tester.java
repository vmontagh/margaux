package edu.uw.ece.alloy.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.uw.ece.alloy.debugger.filters.FieldsExtractorVisitor;

public class FieldsExtractorVisitor_Tester {

	final String testingAlloyFile = "fileds_extracter.als";
	
	
	@Before
	public void setUp() throws Exception {
		
		LoggerUtil.debug(this, "FieldsExtractorVisitor is started to be tested");
		LoggerUtil.debug(this, "Creates a new file for test case.");
		
		String alloySpec = 	"sig A{}\n"+
							"sig B{r: A, s: B->A}\n"+
							"sig C{t: set A, g: A one->one B}\n\n"+
							"pred p1[c: C]{\n"+
							"  all c': c | some c'.t}\n\n"+
							"pred p2[a: A]{\n"+
							"  some a: A | B.(B.s) = a\n"+
							"  p1[C]}\n\n"+
							"pred p3[]{\n"+
							"  all c': C | ( p1[c'] implies ( some b:B | c'.t = b.r ) )}\n\n"+
							"run p2\n"+
							"run p3";
		Util.writeAll(testingAlloyFile, alloySpec);
		
		
	}

	@After
	public void tearDown() throws Exception {
		LoggerUtil.debug(this, "Starting to delete the temporary file created in setUp.");
		File file = new File(testingAlloyFile);
	
		if(file.delete()){
			LoggerUtil.debug(this, "Temporary files is successfully deleted.");
		}else{
			LoggerUtil.debug(this, "Temporary files is note successfully deleted.");
		}
	}

	@Test
	public void test_FieldsExtractorCommand1() throws Err {
		
		Module world = CompUtil.parseEverything_fromFile(null, null, testingAlloyFile);
		
		assertEquals(FieldsExtractorVisitor.getReferencedFields(
				world.getAllCommands().get(0).formula).stream().sorted(
						(Sig.Field a,Sig.Field b)->{return a.toString().compareTo(b.toString());}).collect(
								Collectors.toList()).toString(), "[field (this/B <: s), field (this/C <: t)]");		
		
	}

	@Test
	public void test_FieldsExtractorCommand2() throws Err {
		
		Module world = CompUtil.parseEverything_fromFile(null, null, testingAlloyFile);
		
		System.out.println(FieldsExtractorVisitor.getReferencedFields(world.getAllCommands().get(1).formula).toString());
		
		assertEquals(FieldsExtractorVisitor.getReferencedFields(
				world.getAllCommands().get(1).formula).stream().sorted(
						(Sig.Field a,Sig.Field b)->{return a.toString().compareTo(b.toString());}).collect(
								Collectors.toList()).toString(), "[field (this/B <: r), field (this/C <: t)]");		
		
	}
	
}
