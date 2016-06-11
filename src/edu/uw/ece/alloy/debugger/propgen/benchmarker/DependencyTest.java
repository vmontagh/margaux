package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class DependencyTest {

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
	public void testBigCompressionDecmpression() throws Exception {
		File bigFile = new File("big_file.txt");

		Random rand = new Random();
		int max = 'Z';
		int min = 'A';
		int fileSizeInChars = 1000000;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fileSizeInChars; ++i) {
			int randomNum = rand.nextInt((max - min) + 1) + min;
			sb.append(Character.toChars(randomNum));
		}

		String fileContent = sb.toString();

		Util.writeAll(bigFile.getAbsolutePath(), fileContent);
		System.out.println(bigFile.length());

		Dependency dp = Dependency.EMPTY_DEPENDENCY.createIt(bigFile, fileContent);

		Dependency encodedDp = dp.compress();

		Dependency decodedDp = encodedDp.deCompress();

		assertEquals(dp.content, fileContent);
		assertEquals(fileContent, decodedDp.content);
		assertEquals(dp.content, decodedDp.content);

		assertNotEquals(fileContent, encodedDp.content);

		assertEquals(dp.path, bigFile);
		assertEquals(bigFile, decodedDp.path);
		assertEquals(dp.path, decodedDp.path);

		// bigFile.delete();

	}

	@Test
	public void testBigCompressionDecmpressionTemporalLib() throws Exception {
		File bigFile = new File(Configuration.getProp("temporal_properties_tagged"));

		System.out.println(bigFile.getAbsolutePath());
		System.out.println(bigFile.length());

		System.out.println(bigFile.exists());

		String fileContent = Utils.readFile(bigFile.getAbsolutePath());

		Dependency dp = Dependency.EMPTY_DEPENDENCY.createIt(bigFile, fileContent);

		Dependency encodedDp = dp.compress();

		Dependency decodedDp = encodedDp.deCompress();

		assertEquals(dp.content, fileContent);
		assertEquals(fileContent.length(), decodedDp.content.length());
		assertEquals(fileContent, decodedDp.content);
		assertEquals(dp.content, decodedDp.content);

		assertNotEquals(fileContent, encodedDp.content);

		assertEquals(dp.path, bigFile);
		assertEquals(bigFile, decodedDp.path);
		assertEquals(dp.path, decodedDp.path);

	}

}
