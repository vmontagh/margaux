/**
 * 
 */
package edu.uw.ece.alloy.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uw.ece.alloy.debugger.PrecomputedProperties;
import edu.uw.ece.alloy.debugger.PropertySet.Property;

/**
 * @author vajih
 *
 */
public class PrecomputedProperties_Tester {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_BackwardForwardImplication() {

		final Map<Property, Set<Property>> bw = PrecomputedProperties.INSTANCE.backwardImplicationHierarchy;
		final Map<Property, Set<Property>> fw = PrecomputedProperties.INSTANCE.forwardImplicationHierarchy;

		System.out.println("Backward");
		for (final Property p : bw.keySet()) {
			final Set<Property> parentsP = bw.get(p);
			System.out.printf("%s <-> %s%n", p, parentsP);
			for (final Property parentP : parentsP) {
				assertNotNull(fw.get(parentP));
				assertTrue(fw.get(parentP).contains(p));
			}
		}

		System.out.println("\n\n\n\n\n\nForward");
		for (final Property p : fw.keySet()) {
			final Set<Property> childrenP = fw.get(p);
			System.out.printf("%s <-> %s%n", p, childrenP);
			for (final Property childP : childrenP) {
				assertNotNull(bw.get(childP));
				assertTrue(bw.get(childP).contains(p));
			}

		}

	}

}
