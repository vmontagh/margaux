package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.util.Utils;

public class AlloyProcessingParamTester {

	final File tmpDir = new File("./AlloyProcessingParamTesterTmpDir");
	final static Logger logger = Logger
			.getLogger(AlloyProcessingParamTester.class.getName() + "--"
					+ Thread.currentThread().getName());

	final String predBodyA = "predBodyA";
	final String predBodyB = "predBodyB";
	final String predCallA = "predCallA";
	final String predCallB = "predCallB";
	final String predNameA = "predNameA";
	final String predNameB = "predNameB";

	final String header = "header";
	final String scope = "scope";
	final String field = "field";

	final String fileContentAltered = "This is an altered test that shows how a dumfile correctly works.\n";

	final String depContent1 = "I am a dependency file 1 and should be stored on the disk before running the file. \n";
	final String depContent2 = "I am a dependency file 2 and should be stored on the disk before running the file. \n";
	final String depContent3 = "I am a dependency file 3 and should be stored on the disk before running the file. \n";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		// Make a temporary folder to place the files.
		if (tmpDir.exists()) {
			if (Configuration.IsInDeubbungMode)
				logger.info("The " + tmpDir.getAbsolutePath()
						+ " is accidentally exists, so it has to be deleted first.");
			Utils.deleteRecursivly(tmpDir);
		}
		tmpDir.mkdirs();
		if (Configuration.IsInDeubbungMode)
			logger.info("The " + tmpDir.getAbsolutePath()
					+ " is created before test cases start.");
	}

	@After
	public void tearDown() throws Exception {
		// Delete the temporary folder
		if (tmpDir.exists()) {
			Utils.deleteRecursivly(tmpDir);
			if (Configuration.IsInDeubbungMode)
				logger.info("The " + tmpDir.getAbsolutePath()
						+ " is deleted at the end of unit test.");
		} else {
			logger.warning("The " + tmpDir.getAbsolutePath()
					+ " was accidentally deleted and not cleared up in the tearDown.");
		}
	}

	@Test
	public void testAlloyProcessingParam_DumpFile_reWriteEnable()
			throws Exception {

		// A file exists
		// The content has to be have newline at the very last. Utils.writeAll
		// append a newline to the end of
		// string does not have newline at the last character

		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, Collections.emptyList(),
						AlloyProcessingParam.EMPTY_PARAM, header, scope, field// [tmpDirectory],
																																	// tmpDir
		);

		AlloyProcessingParam aParam = vacPropertyToAlloyCode.generate();

		final Field reWrite = AlloyProcessingParam.EMPTY_PARAM.getClass()
				.getDeclaredField("reWrite");

		final File outputFile = new File(tmpDir, "dumpedFile.txt");
		// rewrite is enabled
		Utils.setFinalStatic(reWrite, Boolean.TRUE);

		final String fileContent = header + '\n' + predBodyA + '\n' + predBodyB
				+ '\n' + "run{ some r and " + predCallA + "}" + scope + "\n";

		final File path1 = aParam.dumpFile(outputFile, fileContent);
		assertEquals(path1, outputFile);
		assertEquals(Util.readAll(path1.getAbsolutePath()), fileContent);

		final File path2 = aParam.dumpFile(outputFile, fileContentAltered);
		assertEquals(path1, path2);
		assertEquals(Util.readAll(path2.getAbsolutePath()), fileContentAltered);

		// delete the file

		// rewrite is disabled
		reWrite.set(aParam, false);

	}

	@Test
	public void testAlloyProcessingParam_DumpFile_reWriteDisable()
			throws Exception {

		// A file exists
		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, Collections.emptyList(),
						AlloyProcessingParam.EMPTY_PARAM, header, scope, field// [tmpDirectory],tmpDir
		);

		AlloyProcessingParam aParam = vacPropertyToAlloyCode.generate();

		final Field reWrite = aParam.getClass().getDeclaredField("reWrite");
		// reWrite.setAccessible(true);

		final File outputFile = new File(tmpDir, "dumpedFile.txt");
		// rewrite is enabled
		Utils.setFinalStatic(reWrite, Boolean.FALSE);

		final String fileContent = "";

		final File path1 = aParam.dumpFile(outputFile, fileContent);
		assertEquals(path1, outputFile);
		final String storedContent = Util.readAll(path1.getAbsolutePath());
		assertEquals(storedContent, fileContent);

		final File path2 = aParam.dumpFile(outputFile, fileContentAltered);
		assertEquals(path1, path2);
		assertEquals(Util.readAll(path2.getAbsolutePath()), fileContent);

	}

	@Test
	public void testAlloyProcessingParamLazy_prepareToUse() throws Exception {

		final File depFile1 = new File("dep1.als");
		final List<Dependency> dependecies = new LinkedList<Dependency>();

		final File depFile2 = new File("dep2.als");

		dependecies.add(new Dependency(depFile1, depContent1));
		dependecies.add(new Dependency(depFile2, depContent2));

		// A file exists
		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, dependecies, AlloyProcessingParamLazy.EMPTY_PARAM,
						header, scope, field// [tmpDirectory],tmpDir
		);

		final String fileContent = header + '\n' + predBodyA + '\n'
				+ "run{ some r and " + predCallA + "}" + scope + "\n";

		AlloyProcessingParam aParam = vacPropertyToAlloyCode.generate();

		aParam = aParam.changeTmpDirectory(tmpDir).prepareToUse();

		// The source is stored
		final File newSrc = new File(this.tmpDir, "predNameA_VAC_predNameA.als");
		assertTrue(newSrc.exists());
		assertEquals(Util.readAll(newSrc.getAbsolutePath()), fileContent);

		final File newDep1 = new File(this.tmpDir, depFile1.getPath());
		assertTrue(newDep1.exists());
		assertEquals(Util.readAll(newDep1.getAbsolutePath()), depContent1);

		final File newDep2 = new File(this.tmpDir, depFile2.getPath());
		assertTrue(newDep2.exists());
		assertEquals(Util.readAll(newDep2.getAbsolutePath()), depContent2);

		final File newDestPath = new File(this.tmpDir,
				"predNameA_VAC_predNameA.als.out.txt");
		assertEquals(newDestPath.getAbsolutePath(),
				aParam.destPath().getAbsolutePath());
	}

	@Test
	public void testAlloyProcessingParamLazyCompressed_prepareToUse()
			throws Exception {

		final File srcPath = new File(tmpDir, "src.als");
		final File destPath = new File(srcPath.getAbsolutePath() + ".dst");

		final File depFile1 = new File(tmpDir, "dep1.als");
		final List<Dependency> dependecies = new LinkedList<Dependency>();

		final File depFile2 = new File(tmpDir, "dep2.als");

		dependecies.add(new Dependency(depFile1, depContent1));
		dependecies.add(new Dependency(depFile2, depContent2));

		// A file exists
		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, dependecies,
						AlloyProcessingParamLazyCompressing.EMPTY_PARAM, header, scope,
						field// [tmpDirectory],tmpDir
		);

		AlloyProcessingParam aParam = vacPropertyToAlloyCode.generate();

		final String fileContent = header + '\n' + predBodyA + '\n' + predBodyB
				+ '\n' + "run{ some r and " + predCallA + "}" + scope + "\n";

		AlloyProcessingParam aParam2 = aParam.prepareToSend();
		assertNotEquals(fileContent, aParam2.content());
		assertNotEquals(srcPath.getAbsolutePath(),
				aParam2.srcPath().getAbsolutePath());
		assertNotEquals(destPath.getAbsolutePath(),
				aParam2.destPath().getAbsolutePath());
		assertEquals(0, aParam2.priority);
		assertTrue(aParam2.dependencies().isEmpty());
		assertEquals(aParam2.content(),
				VacPropertyToAlloyCode.EMPTY_CONVERTOR.generateAlloyCode());

	}

	@Test
	public void testAlloyProcessingParamLazyCompressed_hashCode()
			throws Exception {

		final File depFile1 = new File("dep1.als");
		final List<Dependency> dependecies = new LinkedList<>();

		final File depFile2 = new File("dep2.als");

		dependecies.add(new Dependency(depFile1, depContent1));
		dependecies.add(new Dependency(depFile2, depContent2));

		// A file exists
		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, dependecies,
						AlloyProcessingParamLazyCompressing.EMPTY_PARAM, header, scope,
						field// [tmpDirectory],tmpDir
		);

		AlloyProcessingParam aParam = vacPropertyToAlloyCode.generate();

		AlloyProcessingParam aParamCoded = aParam.prepareToSend();

		AlloyProcessingParam aParamCodedCopy = aParamCoded.createItself();

		assertEquals(aParamCodedCopy, aParamCoded);

		assertEquals(aParamCodedCopy.hashCode(), aParamCoded.hashCode());

		assertNotEquals(aParam.hashCode(), aParamCoded.hashCode());

		AlloyProcessingParam aParamDecoded = aParamCoded.prepareToUse();

		System.out.println(aParam);
		System.out.println(aParamDecoded);
		System.out.println(aParamCoded.getClass());

		aParamDecoded.equals(aParam);
		assertEquals(aParamDecoded, aParam);

		System.out.println(aParamDecoded.getClass());
		System.out.println(aParam.getClass());

		assertEquals(aParamDecoded.hashCode(), aParam.hashCode());

	}

	@Test
	public void testAlloyProcessingParam_FileStorage() throws Exception {

		final File depFile1 = new File("dep1.als");
		final File depFile2 = new File("dep2.als");

		final List<Dependency> dependecies = new LinkedList<>();
		dependecies.add(new Dependency(depFile1, depContent1));
		dependecies.add(new Dependency(depFile2, depContent2));

		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR
				.createIt(predBodyA, predBodyB, predCallA, predCallB, predNameA,
						predNameB, dependecies, AlloyProcessingParamLazy.EMPTY_PARAM,
						header, scope, field// [tmpDirectory],tmpDir
		);

		AlloyProcessingParam aParam_1 = vacPropertyToAlloyCode.generate();

		aParam_1 = aParam_1.changeTmpDirectory(tmpDir);
		aParam_1.dumpAll();

		assertTrue(aParam_1.srcPath().exists());

		aParam_1.removeContent();

		assertFalse(aParam_1.srcPath().exists());
	}

}
