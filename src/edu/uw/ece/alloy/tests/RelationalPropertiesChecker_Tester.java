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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.LoggerUtil;
import edu.uw.ece.alloy.debugger.RelationalPropertiesChecker;

/**
 * @author vajih
 *
 */
public class RelationalPropertiesChecker_Tester {

	final String testingRelationalPropertiesFile = "relational_props_test.ini";
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		LoggerUtil.debug(this, "RelationalPropertiesChecker is started to be tested.");
		LoggerUtil.debug(this, "Creates a new file for test case.");
		String properties = "total,b,d,0\nfunctional,b,d,0\nantisymmetric,b,0,0\nweaklyConnected,b,0,r\n"+
							"irreversible,t,0,0\nstronglyConnected,b,0,r\nbijective,b,d,r";
		Util.writeAll(testingRelationalPropertiesFile, properties);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
		LoggerUtil.debug(this, "Starting to delete the temporary file created in setUp.");
		File file = new File(testingRelationalPropertiesFile);
		 
		if(file.delete()){
			LoggerUtil.debug(this, "Temporary files is successfully deleted.");
		}else{
			LoggerUtil.debug(this, "Temporary files is note successfully deleted.");
		}
		
	}

	@Test
	public void test_getAllBinaryWithDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(null,testingRelationalPropertiesFile);
		assertArrayEquals(rpc.getAllBinaryWithDomainWithRangeRelationalProperties().toArray(),Arrays.asList("bijective").toArray());
		
	}
	
	@Test
	public void test_getAllBinaryWithDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(null,testingRelationalPropertiesFile);
		assertArrayEquals(rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().toArray(),Arrays.asList("total", "functional").toArray());
		
	}

	@Test
	public void test_getAllBinaryWithoutDomainWithRangeRelationalProperties() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(null,testingRelationalPropertiesFile);
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().toArray(),Arrays.asList("weaklyConnected", "stronglyConnected").toArray());
		
		
	}
	
	@Test
	public void test_getAllBinaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(null,testingRelationalPropertiesFile);
		assertArrayEquals(rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().toArray(),Arrays.asList("antisymmetric").toArray());		
		
	}

	@Test
	public void test_getAllTernaryWithoutDomainWithoutRangeRelationalProperties() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(null,testingRelationalPropertiesFile);
		assertArrayEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().toArray(),Arrays.asList("irreversible").toArray());		
		
	}
	
	@Test
	public void test_allRelationalPropertiesCovered() throws FileNotFoundException, IOException {

		RelationalPropertiesChecker rpc = new RelationalPropertiesChecker(null,testingRelationalPropertiesFile);
				
		assertEquals(rpc.getAllTernaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithoutDomainWithRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithoutRangeRelationalProperties().size()+
				rpc.getAllBinaryWithDomainWithRangeRelationalProperties().size(), 7 );
		
	}	
	
}
