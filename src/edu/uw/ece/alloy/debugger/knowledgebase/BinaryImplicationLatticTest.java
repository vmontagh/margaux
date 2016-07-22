/**
 * A test case for the ImplicationLattic class.
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.Err;
import edu.uw.ece.alloy.util.Utils;

/**
 * @author vajih
 *
 */
public class BinaryImplicationLatticTest {

	final String sourceFolderPath = "models/debugger/knowledge_base";
	final String[] moduleNames = { "binary_implication.als", "property_structure.als" };
	final String tempFolderPath = "tmp/kb";
	ImplicationLattic bil;

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

		bil = new BinaryImplicationLatticDeclarative(tempFolderPath, moduleNames);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		File tempFolder = new File(tempFolderPath);
		Utils.deleteRecursivly(tempFolder);
	}

	@Test
	public void testgetAllSources() {
		try {
			List<String> sources = bil.getAllSources();
			assertFalse(sources.isEmpty());

			for (String source : sources) {
				assertNotEquals("", source);
			}

			assertArrayEquals(sources.toArray(new String[] {}),
					new String[] { "empty", "totalOrder", "equivalence", "bijection" });

		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgetAllSinks() {
		try {
			List<String> sinks = bil.getAllSinks();
			assertFalse(sinks.isEmpty());

			for (String source : sinks) {
				assertNotEquals("", source);
			}

			assertArrayEquals(sinks.toArray(new String[] {}), new String[] { "irreflexive", "antisymmetric",
					"symmetric", "transitive", "weaklyConnected", "total", "functional" });

		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgetImply() {
		try {
			List<String> nexts = bil.getNextImpliedProperties("reflexive");
			assertFalse(nexts.isEmpty());

			for (String source : nexts) {
				assertNotEquals("", source);
			}

			assertArrayEquals(nexts.toArray(new String[] {}), new String[] { "total" });

		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgetAllImply() {
		try {
			List<String> nexts = bil.getAllImpliedProperties("totalOrder");
			assertFalse(nexts.isEmpty());

			for (String source : nexts) {
				assertNotEquals("", source);
			}

			assertArrayEquals(nexts.toArray(new String[] {}), new String[] { "complete", "preorder", "partialOrder",
					"rootedOne", "reflexive", "total", "weaklyConnected", "transitive", "antisymmetric" });

		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgetRevImply() {
		try {
			List<String> previouses = bil.getNextRevImpliedProperties("reflexive");
			assertFalse(previouses.isEmpty());

			for (String source : previouses) {
				assertNotEquals("", source);
			}

			System.out.println(previouses);

			assertArrayEquals(previouses.toArray(new String[] {}),
					new String[] { "preorder", "totalOrder", "equivalence", "partialOrder" });
		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	@Test
	public void testgetAllRevImply() {
		try {
			List<String> nexts = bil.getAllRevImpliedProperties("irreflexive");
			assertFalse(nexts.isEmpty());

			for (String source : nexts) {
				assertNotEquals("", source);
			}

			assertArrayEquals(nexts.toArray(new String[] {}), new String[] { "acyclic", "empty" });

		} catch (Err e1) {
			e1.printStackTrace();
			fail();
		}
	}

	static <T> void compareTwoUnsortedCollectionsOfStrings(Collection<T> collectionA, Collection<T> collectionB) {
		assertArrayEquals(collectionA.stream().sorted().toArray(size -> new String[size]),
				collectionB.stream().sorted().toArray(size -> new String[size]));
	}

	@Test
	public void testDeclarativeEqualsToImplrative() {
		BinaryImplicationLatticDeclarative bild = new BinaryImplicationLatticDeclarative();
		BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		try {
			compareTwoUnsortedCollectionsOfStrings(bild.getAllPatterns(), bili.getAllPatterns());

			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getAllImpliedProperties(pattern),
						bili.getAllImpliedProperties(pattern));
			}

			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getAllRevImpliedProperties(pattern),
						bili.getAllRevImpliedProperties(pattern));
			}

			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getNextImpliedProperties(pattern),
						bili.getNextImpliedProperties(pattern));
			}

			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getNextRevImpliedProperties(pattern),
						bili.getNextRevImpliedProperties(pattern));
			}

		} catch (Err e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testDeclarativeEqualsToImplrativeAllPatterns() {
		BinaryImplicationLatticDeclarative bild = new BinaryImplicationLatticDeclarative();
		BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		try {
			compareTwoUnsortedCollectionsOfStrings(bild.getAllPatterns(), bili.getAllPatterns());
		} catch (Err e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testDeclarativeEqualsToImplrativeAllImpliedProperties() {
		BinaryImplicationLatticDeclarative bild = new BinaryImplicationLatticDeclarative();
		BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		try {
			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getAllImpliedProperties(pattern),
						bili.getAllImpliedProperties(pattern));
			}
		} catch (Err e) {
			fail();
			e.printStackTrace();
		}

	}

	@Test
	public void testDeclarativeEqualsToImplrativeAllRevImpliedProperties() {
		BinaryImplicationLatticDeclarative bild = new BinaryImplicationLatticDeclarative();
		BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		try {
			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getAllRevImpliedProperties(pattern),
						bili.getAllRevImpliedProperties(pattern));
			}
		} catch (Err e) {
			fail();
			e.printStackTrace();
		}

	}

	/*
	 * TODO fix the BinaryImplicationLatticDeclarative. The next has a
	 * transitive behavior. If A=>B, B=>C, then
	 * bild.getNextImpliedProperties("A") returns {B,C} which is expected to be
	 * just B. Until getNextImpliedProperties and getNextRevImpliedProperties
	 * are fixed, should be deprecated.
	 */
	@Test
	public void testDeclarativeEqualsToImplrativeNextImpliedProperties() {
		BinaryImplicationLatticDeclarative bild = new BinaryImplicationLatticDeclarative();
		BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		try {
			for (String pattern : bild.getAllPatterns()) {
				System.out.println("pattern:->" + pattern);
				System.out.println("Implications from Declarative:->" + bild.getNextImpliedProperties(pattern));
				System.out.println("Implications from Imperative:->" + bili.getNextImpliedProperties(pattern));
				System.out.println("All Implications from Imperative:->" + bili.getAllImpliedProperties(pattern));
				compareTwoUnsortedCollectionsOfStrings(bild.getNextImpliedProperties(pattern),
						bili.getNextImpliedProperties(pattern));
			}
		} catch (Err e) {
			fail();
			e.printStackTrace();
		}

	}

	/*
	 * Fails due to the same reason as {@link
	 * #testDeclarativeEqualsToImplrativeNextImpliedProperties()
	 * testDeclarativeEqualsToImplrativeNextImpliedProperties}
	 */
	@Test
	public void testDeclarativeEqualsToImplrativeNextRevImpliedProperties() {
		BinaryImplicationLatticDeclarative bild = new BinaryImplicationLatticDeclarative();
		BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		try {
			for (String pattern : bild.getAllPatterns()) {
				compareTwoUnsortedCollectionsOfStrings(bild.getNextRevImpliedProperties(pattern),
						bili.getNextRevImpliedProperties(pattern));
			}
		} catch (Err e) {
			fail();
			e.printStackTrace();
		}

	}

}
