/**
 * 
 */
package edu.uw.ece.alloy.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.uw.ece.alloy.debugger.RelationalPropertiesChecker;

/**
 * @author vajih
 *
 */
public class RelationalPropertiesChecker_Tester {

	final static String testingRelationalPropertiesFile = "relational_props_test.ini";
	final static String testingRelationalPropertiesAlloyCommands = "alloy_testing_commands.als";
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {

		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "RelationalPropertiesChecker is started to be tested.");

		//A file is created to test the property files reader.
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Creates a new file containing properties.");
		/*final String properties = "total,b,d,0\nfunctional,b,d,0\nantisymmetric,b,0,0\nweaklyConnected,b,0,r\n"+
				"irreversible,t,0,0\nstronglyConnected,b,0,r\nbijection,b,d,r\ntransitive3,t,0,0";*/
		final String properties = "total,b,d,0\ntransitive3,t,0,0";
		Util.writeAll(testingRelationalPropertiesFile, properties);

		//A file is created to test the command and their expression reader.
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Creates a new file for testing Alloy Commands.");
		final String alloySpec = "sig A{r: B}\n\n"+
				"sig B{s: A->B}\n"+
				"sig C extends A{c: B}{#B = 1}"+
				"fact{ no B}\n"+
				"pred p[]{ some A}\n"+
				"fun f[a:A]:B->B{{b,b':B|a.r = b}}\n"+
				"pred q[]{ some f[A]}\n\n"+
				"pred w[a:A]{ some f[a]}\n\n"+
				"assert s{ q and p \r { \np \n}\n }\n\n"+
				"run {}\n"+
				"run {some A} for 4\n"+
				"run {p and q}\n"+
				"run w\n"+
				"run {some a:A | w[a]}\n"+
				"run q for 0 but 3 A\n"+
				"check s for 0 but 3 A\n"+
				"check {q[] implies p} for 5\n";
		Util.writeAll(testingRelationalPropertiesAlloyCommands, alloySpec);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownClass() throws Exception {

		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Starting to delete the temporary file created in setUp.");

		List<String> fileNames = Arrays.asList(testingRelationalPropertiesFile, testingRelationalPropertiesAlloyCommands);

		for(String fileName: fileNames){
			final File file = new File(fileName);

			if(file.delete()){
				LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Temporary files '"+fileName+"' is successfully deleted.");
			}else{
				LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Temporary files '"+fileName+"' is note successfully deleted.");
			}
		}

	}

	@Test
	public void test_getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "");
		assertArrayEquals(rpc.getAllBinaryWithDomainWithRangeRelationalProperties().toArray(),Arrays.asList("bijective").toArray());

	}

	@Test
	public void test_getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "");
		assertArrayEquals(rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().toArray(),Arrays.asList("total", "functional").toArray());

	}

	@Test
	public void test_getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "");
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().toArray(),Arrays.asList("weaklyConnected", "stronglyConnected").toArray());


	}

	@Test
	public void test_getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "");
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().toArray(),Arrays.asList("antisymmetric").toArray());		

	}

	@Test
	public void test_getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "");
		assertArrayEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().toArray(),Arrays.asList("irreversible").toArray());		

	}

	@Test
	public void test_allRelationalPropertiesCovered() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "");

		assertEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithRangeRelationalProperties().size(), 7 );

	}
	
	@Test
	public void test_transformForChecking() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, testingRelationalPropertiesAlloyCommands);
		
		try {
			rpc.transformForChecking("./tmp-prop-analysis");
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

	}

}
