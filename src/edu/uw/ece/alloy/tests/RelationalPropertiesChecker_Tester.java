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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;
import edu.uw.ece.alloy.debugger.exec.RelationalPropertiesChecker;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class RelationalPropertiesChecker_Tester {

	final static File propertiesModuleFile = new File("relational_properties.als");
	final static File resourcesDirectory = new File("models/debugger/models2015");
	
	//These files is temporarily created and deleted after the test
	final static File testingRelationalPropertiesFile = new File( "relational_props_test.ini");
	final static File testingRelationalPropertiesAlloyCommands = new File("alloy_testing_commands.als");
	final static File testingRelationalPropertiesTMPDirectory = new File("tmp-prop-analysis");
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {

		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "RelationalPropertiesChecker is started to be tested.");

		//A file is created to test the property files reader.
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Creates a new file containing properties.");
		final String properties = "total,b,d,0,0\nfunctional,b,d,0,0\nantisymmetric,b,0,0,0\nweaklyConnected,b,d,r,0\n"+
				"irreversible,t,0,0,0\nstronglyConnected,b,d,r,0\nbijection,b,d,r,0\ntransitive3,t,0,0,0\nempty,b,0,0,0\n"+
				"empty3,t,0,0,0\ntripleRootedOne,t,d,m,r\nisIncrease_s_t_local_m,ot,d,0,r\nisIncreaseStrictly_s_t_local_m,ot,d,0,r\n";
		//final String properties = "total,b,d,0\ntransitive3,t,0,0";
		Util.writeAll(testingRelationalPropertiesFile.getAbsolutePath(), properties);

		//A file is created to test the command and their expression reader.
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Creates a new file for testing Alloy Commands.");
		final String alloySpec = 
				"open util/ordering [A] as so\n"+
				"open util/ordering [B]\n"+
				"open util/relation as rel"+
				"one sig A{r: B}\n\n"+
				"sig G,F,E extends A{}\n"+
				"sig B{s: F->A}\n"+
				"sig Z{t: B, p:t->A}\n"+
				"sig C extends A{c: B}{#B = 1}\n"+
				"sig T{w:A->B}\n"+
				"fact FFFF{ no B\n #A=1}\n"+
				"fact{ no B}\n"+
				"pred p[]{ some G<:(A<:r)}\n"+
				"fun f[a:A]:B->B{{b,b':B|a.r = b}}\n"+
				"pred q[]{ some f[A]}\n\n"+
				"pred w[a:A]{ some f[a]}\n\n"+
				"pred s{ q and p \r { \np \n}\n }\n\n"+
				"/*run {}\n"+
				"run {some A} for 4\n"+
				"run {p and q}\n"+
				"run w\n"+
				"run p\n"+
				"run {some a:A | w[a]}\n"+
				"run q for 0 but 3 F\n*/"+
				"run s for 0 but 3 G\n"+
				"//check {q[] implies p} for 5\n";
		
		Util.writeAll(testingRelationalPropertiesAlloyCommands.getAbsolutePath(), alloySpec);
		
		//Now setup a tmp directory
		if(testingRelationalPropertiesTMPDirectory.exists()){
			Utils.deleteRecursivly(testingRelationalPropertiesTMPDirectory);
		}

		//Now create a temp folder
		testingRelationalPropertiesTMPDirectory.mkdir();
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "A temporary directory is created.");
		
		Files.copy(new File( resourcesDirectory, propertiesModuleFile.getName() ).toPath(), 
						new File( testingRelationalPropertiesTMPDirectory, propertiesModuleFile.getName() ).toPath());
		
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, String.format( "A %s module file is copied.", propertiesModuleFile));

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownClass() throws Exception {

		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Starting to delete the temporary file created in setUp.");

		List<File> files = Arrays.asList(testingRelationalPropertiesFile, testingRelationalPropertiesAlloyCommands);

		for(File file: files){

			if(file.delete()){
				LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Temporary files '"+file.getName()+"' is successfully deleted.");
			}else{
				LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Temporary files '"+file.getName()+"' is note successfully deleted.");
			}
		}

		if(testingRelationalPropertiesTMPDirectory.exists()){
			//Utils.deleteRecursivly(tmpDir);
		}
		LoggerUtil.debug(RelationalPropertiesChecker_Tester.class, "Temporary Directory '"+testingRelationalPropertiesTMPDirectory.getName()+"' is successfully deleted.");

	}

	@Test
	public void test_getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File("NONEXSTINGFILE"), propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithDomainWithRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("bijection").stream().sorted().toArray());

	}

	@Test
	public void test_getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File("NONEXSTINGFILE"), propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("total", "functional").stream().sorted().toArray());

	}

	@Test
	public void test_getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File("NONEXSTINGFILE"), propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("weaklyConnected", "stronglyConnected").stream().sorted().toArray());


	}

	@Test
	public void test_getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File("NONEXSTINGFILE"), propertiesModuleFile);
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("antisymmetric","empty").stream().sorted().toArray());		

	}

	@Test
	public void test_getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File("NONEXSTINGFILE"), propertiesModuleFile);
		assertArrayEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().stream().sorted().toArray(),
				Arrays.asList("transitive3", "irreversible", "empty3").stream().sorted().toArray());		

	}

	@Test
	public void test_allRelationalPropertiesCovered() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File("NONEXSTINGFILE"), propertiesModuleFile);

		assertEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithRangeRelationalProperties().size(), 9 );

	}
	
	@Test
	public void test_transformForChecking() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, testingRelationalPropertiesAlloyCommands, propertiesModuleFile);
		rpc = rpc.replacingCheckAndAsserts();
		try {
			for(File file: rpc.transformForChecking(testingRelationalPropertiesTMPDirectory) ){
				assertNotNull( A4CommandExecuter.getInstance().parse(file.getAbsolutePath(), null) );
			}
			
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void test_makeApproximation() throws FileNotFoundException, IOException, Err {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(
								new File("models/debugger/models2015","props.ini"), 
								new File("models/debugger/models2015/anlayzing_models", "dijkstra_buggy_splitted.als"), 
								propertiesModuleFile);
		rpc = rpc.replacingCheckAndAsserts();
		try {
				rpc.makeApproximation(resourcesDirectory) ;
			
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void test_transformForCheckingRealExample() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(testingRelationalPropertiesFile, new File( "models/examples/toys/CeilingsAndFloors.als"), propertiesModuleFile);
		
		try {
			assertEquals(rpc.transformForChecking(testingRelationalPropertiesTMPDirectory).size(), 336);/*.stream().forEach(System.out::println)*/;
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
