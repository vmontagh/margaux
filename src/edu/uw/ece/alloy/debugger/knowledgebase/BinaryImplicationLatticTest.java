/**
 * A test case for the ImplicationLattic class.
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class BinaryImplicationLatticTest {

	final String sourceFolderPath = "models/debugger/knowledge_base";
	final String[] moduleNames = {"binary_implication.als",
																"property_structure.als"};
	final String tempFolderPath = "tmp/kb";
	BinaryImplicationLattic bil;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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

		// Create the temp folder
		File tempFolder = new File(tempFolderPath);
		if (!tempFolder.exists())
			try{
				tempFolder.mkdirs();
			}catch(Exception e){
				e.printStackTrace();
			}
		// Copy the module file into the temp folder
		for (String module: moduleNames){
			File source = new File(sourceFolderPath, module);
			File dest = new File(tempFolder, module);
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}		
		
		bil = new BinaryImplicationLattic(tempFolderPath, moduleNames);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		File tempFolder = new File(tempFolderPath);
		Utils.deleteRecursivly(tempFolder);
	}

	@Test
	public void testgetAllSources() {
		try {
			List<String> sources = bil.getAllSources();
			assertFalse(sources.isEmpty());
			
			for (String source: sources){
				assertNotEquals("", source);
			}
			
			assertArrayEquals(
					sources.toArray(new String[]{}),
					new String[]{"empty", "totalOrder", "equivalence", "bijection"});
			
		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgetAllSinks() {
		try {
			List<String> sinks = bil.getAllSinks();
			assertFalse(sinks.isEmpty());
			
			for (String source: sinks){
				assertNotEquals("", source);
			}
						
			assertArrayEquals(
					sinks.toArray(new String[]{}),
					new String[]{"irreflexive",
											 "antisymmetric",
											 "symmetric",
											 "transitive",
											 "weaklyConnected",
											 "total",
											 "functional"});
			
		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}	
	
	@Test
	public void testgetImply() {
		try {
			List<String> nexts = bil.getAllImpliedProperties("reflexive");
			assertFalse(nexts.isEmpty());
			
			for (String source: nexts){
				assertNotEquals("", source);
			}
						
			assertArrayEquals(
					nexts.toArray(new String[]{}),
					new String[]{"total"});
			
		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testgetRevImply() {
		try {
			List<String> previouses = bil.getAllReverseImpliedProperties("reflexive");
			assertFalse(previouses.isEmpty());
			
			for (String source: previouses){
				assertNotEquals("", source);
			}
			
			System.out.println(previouses);
			
			assertArrayEquals(
					previouses.toArray(new String[]{}),
					new String[]{ "preorder",
												"totalOrder",
												"equivalence", 
												"partialOrder"});
		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}
	
	
	
}
