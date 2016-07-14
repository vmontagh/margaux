/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Compressor.STATE;
import edu.uw.ece.alloy.debugger.knowledgebase.InconsistencyGraph.STATUS;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class TernaryInconsistencyGraphTest {

	final static File tmpDir = new File("tmp");
	final static File legendPath = new File(tmpDir, "legends.csv");
	final static File inconPath = new File(tmpDir, "incons.csv");
	final static File iffPath = new File(tmpDir, "iff.csv");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (!tmpDir.exists())
			tmpDir.mkdirs();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (legendPath.exists())
			legendPath.deleteOnExit();
		if (inconPath.exists())
			inconPath.deleteOnExit();
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
		if (legendPath.exists())
			legendPath.deleteOnExit();
		if (inconPath.exists())
			inconPath.deleteOnExit();
	}

	protected void writefiles(String legendContent, String inconContent, String iffContent){
		try {
			Util.writeAll(legendPath.getAbsolutePath(), legendContent);
			Util.writeAll(inconPath.getAbsolutePath(), inconContent);
			Util.writeAll(iffPath.getAbsolutePath(), iffContent);
		} catch (Err e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}
	
	@Test
	public void testInitiateSuccessfull() {
		String legendContent = "\n0,A\n1,B\n2,C\n";
		String inconContent = "\n0,1\n1,2\n";
		String iffContent = "\n";
		writefiles(legendContent, inconContent, iffContent);
		
		new TernaryInconsistencyGraph(legendPath.getAbsolutePath(),
				inconPath.getAbsolutePath(), iffPath.getAbsolutePath());
	}
	
	@Test(expected=NullPointerException.class)
	public void testInitiatefail(){
		String legendContent = "\n0,A\n1,B\n2,C\n";
		String inconContent = "\n0,1\n1,3\n";
		String iffContent = "\n";
		writefiles(legendContent, inconContent, iffContent);

		new TernaryInconsistencyGraph(legendPath.getAbsolutePath(),
				inconPath.getAbsolutePath(), iffPath.getAbsolutePath());
	}

	@Test
	public void testInconsistent() {
		String legendContent = "\n0,A\n1,B\n2,C\n";
		String inconContent = "\n0,1\n";
		String iffContent = "\n";
		writefiles(legendContent, inconContent, iffContent);

		final TernaryInconsistencyGraph tig = new TernaryInconsistencyGraph(legendPath.getAbsolutePath(),
				inconPath.getAbsolutePath(), iffPath.getAbsolutePath());
		assertTrue(tig.isInconsistent("A", "B").equals(STATUS.True));
		assertTrue(tig.isInconsistent("A", "C").equals(STATUS.False));
		assertTrue(tig.isInconsistent("A", "D").equals(STATUS.Unknown));
	}

	@Test
	public void testInconsistencySet() {
		String legendContent = "\n0,A\n1,B\n2,C\n";
		String inconContent = "\n0,1\n0,2\n";
		String iffContent = "\n";
		writefiles(legendContent, inconContent, iffContent);

		final TernaryInconsistencyGraph tig = new TernaryInconsistencyGraph(legendPath.getAbsolutePath(),
				inconPath.getAbsolutePath(), iffPath.getAbsolutePath());
		assertTrue(tig.getAllInconsistecies("A").stream().sorted().collect(Collectors.toList()).containsAll(Arrays.asList("B","C")));
		assertTrue(tig.getAllInconsistecies("B").stream().sorted().collect(Collectors.toList()).containsAll(Arrays.asList("A")));

	}

	@Test
	public void testInconsistentSetWithEquivalency() {
		String legendContent = "\n0,A\n1,B\n2,C\n3,D\n";
		String inconContent = "\n0,1\n3,1\n";
		String iffContent = "\n0,3\n";
		writefiles(legendContent, inconContent, iffContent);

		final TernaryInconsistencyGraph tig = new TernaryInconsistencyGraph(legendPath.getAbsolutePath(),
				inconPath.getAbsolutePath(), iffPath.getAbsolutePath());
		assertTrue(tig.getAllInconsistecies("A").stream().sorted().collect(Collectors.toList()).containsAll(Arrays.asList("B")));
		assertTrue(tig.getAllInconsistecies("B").stream().sorted().collect(Collectors.toList()).containsAll(Arrays.asList("A")));
	}
	
	@Test
	public void testAactualData() {
		final TernaryInconsistencyGraph tig = new TernaryInconsistencyGraph();
		System.out.println(tig.getAllInconsistecies("SzGrwtStrc_Glbl_SdMdl_EmptStrt_"));
	}
	
	
}
