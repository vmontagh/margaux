/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.filters.BlocksExtractorByComments;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.PatternToProperty;
import edu.uw.ece.alloy.debugger.knowledgebase.TemporalImplicationLatticeGenerator;
import edu.uw.ece.alloy.debugger.knowledgebase.TernaryImplicationLattic;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;

/**
 * @author vajih
 *
 */
public class ExpressionPropertyCheckerTest {

	final String AlloyTmpTestPath = "tmp/testing.als";
	final String sourceFolderPath = "models/debugger/knowledge_base";
	final String[] moduleNames = { "binary_implication.als", "property_structure.als" };
	final static String tempFolderPath = "tmp/kb";
	ImplicationLattic bil, til;
	ExpressionPropertyGenerator epc;

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
		// @formatter:off
		final String alloyTestCode = "open util/ordering [A] \n" + "sig B{c: B}\n" + "sig A{r:B, s: B->C}\n"
				+ "sig C{}\n" + "pred p1[r:univ->univ, left:univ]{one c}\n" + "fact{\n "
				+ BlocksExtractorByComments.ExtractExpression.BEGIN + "\n some r and some s"
				+ BlocksExtractorByComments.ExtractExpression.END + " \n}\n" + "fact{\n "
				+ " p1[univ->univ, univ] and all a: A| some a.r " + "}\n" + "run{} "
				+ BlocksExtractorByComments.ExtractScope.BEGIN + " for 5" + BlocksExtractorByComments.ExtractScope.END;
		// @formatter:on

		Util.writeAll(AlloyTmpTestPath, alloyTestCode);

		// Create the temp folder
		File tempFolder = new File(tempFolderPath);
		if (!tempFolder.exists())
			try {
				tempFolder.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		// Copy the module file into the temp folder
		for (String module : moduleNames) {
			File source = new File(sourceFolderPath, module);
			File dest = new File(tempFolder, module);
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		bil = new BinaryImplicationLattic(tempFolderPath, moduleNames);
		til = new TernaryImplicationLattic(TemporalImplicationLatticeGenerator.pathToLegend,
				TemporalImplicationLatticeGenerator.pathToImplication, TemporalImplicationLatticeGenerator.pathToIff);

		epc = new ExpressionPropertyGenerator(UUID.randomUUID(), new Queue<>(), new File(AlloyTmpTestPath), tempFolder,
				tempFolder, "s", IfPropertyToAlloyCode.EMPTY_CONVERTOR, "expresson", "scope",
				Collections.emptyList(), new PatternToProperty(new File(Configuration.getProp("relational_properties_tagged")), new File(Configuration.getProp("temporal_properties_tagged")), new File(AlloyTmpTestPath)));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateImplicationRelationChekersSources() throws Err {
		Queue<AlloyProcessingParam> result = new Queue<>();
		epc.generatePatternCheckers(new HashSet<>(bil.getAllSources()), result);
	}

	@Test
	public void testGenerateTemporalChekers() throws Err {
		Queue<AlloyProcessingParam> gs = new Queue<>();
		epc.generatePatternCheckers(new HashSet<>(til.getAllSources()), gs);
	}

}
