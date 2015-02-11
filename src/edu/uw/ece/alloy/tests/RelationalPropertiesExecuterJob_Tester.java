/**
 * 
 */
package edu.uw.ece.alloy.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.uw.ece.alloy.debugger.RelationalPropertiesExecuterJob;

/**
 * @author vajih
 *
 */
public class RelationalPropertiesExecuterJob_Tester {

	final static String testingRelationalPropertiesExecuterJob_TesterCommands = "alloy_testing_jobexecuter.als";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//A file is created to test the command and their expression reader.
		LoggerUtil.debug(RelationalPropertiesExecuterJob_Tester.class, "Creates a new file for testing Alloy Executer Job.");
		final String alloySpec = "sig A{r: A}\n"+
				"check {some r} for 5\n";
		Util.writeAll(testingRelationalPropertiesExecuterJob_TesterCommands, alloySpec);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		LoggerUtil.debug(RelationalPropertiesExecuterJob_Tester.class, "Starting to delete the temporary file created in setUp.");

		List<String> fileNames = Arrays.asList(testingRelationalPropertiesExecuterJob_TesterCommands);

		for(String fileName: fileNames){
			final File file = new File(fileName);

			if(file.delete()){
				LoggerUtil.debug(RelationalPropertiesExecuterJob_Tester.class, "Temporary files '"+fileName+"' is successfully deleted.");
			}else{
				LoggerUtil.debug(RelationalPropertiesExecuterJob_Tester.class, "Temporary files '"+fileName+"' is note successfully deleted.");
			}
		}
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

	/**
	 * Test method for {@link edu.uw.ece.alloy.debugger.RelationalPropertiesExecuterJob#callExecuter(java.lang.String)}.
	 */
	@Test
	public void testCallExecuter() {
		
		final String outputName = testingRelationalPropertiesExecuterJob_TesterCommands+".log";
		
		RelationalPropertiesExecuterJob rpej = new RelationalPropertiesExecuterJob(outputName);
		try {
			Method callExecuterMethod = rpej.getClass().getDeclaredMethod("callExecuter", String.class);
			//Access to hidden operators for the sake of testing
			callExecuterMethod.setAccessible(true);
			callExecuterMethod.invoke(rpej, testingRelationalPropertiesExecuterJob_TesterCommands);
			
			final File file = new File(outputName);
			
			assertTrue("The output file exists?",file.exists());
			
			assertTrue(Util.readAll(outputName).contains(testingRelationalPropertiesExecuterJob_TesterCommands) );
			
			if(file.delete()){
				LoggerUtil.debug(this, "Temporary files '"+outputName+"' is successfully deleted.");
			}else{
				LoggerUtil.debug(this, "Temporary files '"+outputName+"' is note successfully deleted.");
			}
			
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		//fail("Not yet implemented");
	}

}
