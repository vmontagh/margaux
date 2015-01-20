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
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class RelationalPropertiesChecker_Tester {

	final static String propertiesModuleFile = "relational_properties.als";
	final static String resourcesDirectory = "models/debugger/models2015";
	
	//These files is temporarily created and deleted after the test
	final static String testingRelationalPropertiesFile = "relational_props_test.ini";
	final static String testingRelationalPropertiesAlloyCommands = "alloy_testing_commands.als";
	final static String testingRelationalPropertiesTMPDirectory = "tmp-prop-analysis";
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {

		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "RelationalPropertiesChecker is started to be tested.");

		//A file is created to test the property files reader.
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Creates a new file containing properties.");
		final String properties = "total,b,d,0\nfunctional,b,d,0\nantisymmetric,b,0,0\nweaklyConnected,b,0,r\n"+
				"irreversible,t,0,0\nstronglyConnected,b,0,r\nbijection,b,d,r\ntransitive3,t,0,0,\nempty,b,0,0";
		//final String properties = "total,b,d,0\ntransitive3,t,0,0";
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
		
		//Now setup a tmp directory
		File tmpDir = new File(testingRelationalPropertiesTMPDirectory);
		if(tmpDir.exists()){
			Utils.deleteRecursivly(tmpDir);
		}

		//Now create a temp folder
		tmpDir.mkdir();
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "A temporary directory is created.");
		
		Files.copy(new File( resourcesDirectory, propertiesModuleFile ).toPath(), 
						new File( testingRelationalPropertiesTMPDirectory, propertiesModuleFile ).toPath());
		
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, String.format( "A %s module file is copied.", propertiesModuleFile));

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

		File tmpDir = new File(testingRelationalPropertiesTMPDirectory);
		if(tmpDir.exists()){
			//Utils.deleteRecursivly(tmpDir);
		}
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Temporary Directory '"+testingRelationalPropertiesTMPDirectory+"' is successfully deleted.");

	}

	@Test
	public void test_getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "NONEXSTINGFILE", propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithDomainWithRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("bijection").stream().sorted().toArray());

	}

	@Test
	public void test_getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "NONEXSTINGFILE", propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("total", "functional").stream().sorted().toArray());

	}

	@Test
	public void test_getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "NONEXSTINGFILE", propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("weaklyConnected", "stronglyConnected").stream().sorted().toArray());


	}

	@Test
	public void test_getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "NONEXSTINGFILE", propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("antisymmetric","empty").stream().sorted().toArray());		

	}

	@Test
	public void test_getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "NONEXSTINGFILE", propertiesModuleFile);
		assertArrayEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("transitive3", "irreversible").stream().sorted().toArray());		

	}

	@Test
	public void test_allRelationalPropertiesCovered() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "NONEXSTINGFILE", propertiesModuleFile);

		assertEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithRangeRelationalProperties().size(), 9 );

	}
	
	@Test
	public void test_transformForChecking() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, testingRelationalPropertiesAlloyCommands, propertiesModuleFile);
		
		try {
			assertEquals(rpc.transformForChecking(testingRelationalPropertiesTMPDirectory).size(), 480);
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void test_transformForCheckingRealExample() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, "models/examples/toys/CeilingsAndFloors.als", propertiesModuleFile);
		
		try {
			assertEquals(rpc.transformForChecking(testingRelationalPropertiesTMPDirectory).size(), 140);/*.stream().forEach(System.out::println)*/;
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
