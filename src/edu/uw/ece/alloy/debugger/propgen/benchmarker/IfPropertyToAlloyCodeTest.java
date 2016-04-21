package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IfPropertyToAlloyCodeTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testTemporalRootAllInferred() {
		PropertyToAlloyCode ifPropertyToAlloyCode = IfPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt("predBodyA", "predBodyB", "predCallA", "predCallB",
						"predNameA", "OrdIncrs_SzShrnk_Lcl_SdMdl_EmptEnd_",
						new ArrayList<Dependency>(), AlloyProcessingParam.EMPTY_PARAM,
						"header", "scope", "field");

		System.out.println(ifPropertyToAlloyCode.getInferedPropertiesCoder(-1));

	}

}
