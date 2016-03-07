/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.BlocksExtractorByComments;
import edu.uw.ece.alloy.debugger.knowledgebase.BinaryImplicationLattic;
import edu.uw.ece.alloy.debugger.knowledgebase.ImplicationLattic;

/**
 * @author vajih
 *
 */
public class ExpressionPropertyCheckerTest {


	final  String AlloyTmpTestPath = "tmp/testing.als";
	final String sourceFolderPath = "models/debugger/knowledge_base";
	final String[] moduleNames = {"binary_implication.als",
	"property_structure.als"};
	final static String tempFolderPath = "tmp/kb";
	ImplicationLattic bil;
	ExpressionPropertyChecker epc;
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
		final String alloyTestCode = 
				"open util/ordering [A] \n"
						+ "sig B{c: B}\n"
						+ "sig A{r:B}\n"				
						+ "pred p1[r:univ->univ, left:univ]{one c}\n"
						+ "fact{\n " + BlocksExtractorByComments.ExtractExpression.BEGIN 
						+ "\n some r " + BlocksExtractorByComments.ExtractExpression.END + " \n}\n"
						+ "fact{\n "
						+ " p1[univ->univ, univ] and all a: A| some a.r " 
						+ "}\n"
						+ "run{} " +  BlocksExtractorByComments.ExtractScope.BEGIN
						+ " for 5" + BlocksExtractorByComments.ExtractScope.END;
		// @formatter:on

		Util.writeAll(AlloyTmpTestPath, alloyTestCode);	

		// Create the temp folder
		File tempFolder = new File(tempFolderPath);
		if (!tempFolder.exists())
			try{
				tempFolder.mkdirs();
			}catch(Exception e){
				e.printStackTrace();
			}
		// Copy the module file into the temp folder
		for (String module: moduleNames){
			File source = new File(sourceFolderPath, module);
			File dest = new File(tempFolder, module);
			Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}		

		bil = new BinaryImplicationLattic(tempFolderPath, moduleNames);
		epc = new ExpressionPropertyChecker(null, new File(AlloyTmpTestPath));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateImplicationRelationChekersSources() throws Err {
		GeneratedStorage<AlloyProcessingParam> result = new GeneratedStorage<>();
		epc.generateRelationalChekers(new HashSet<>(bil.getAllSources()),result);
	}

}
