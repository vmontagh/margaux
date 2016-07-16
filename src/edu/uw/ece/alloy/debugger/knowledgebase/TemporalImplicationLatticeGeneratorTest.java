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

import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Configuration;

public class TemporalImplicationLatticeGeneratorTest {

	public static String pathToLegend = Configuration.getProp("kb_temporal_legend");
	public static String pathToImplication = Configuration.getProp("kb_temporal_imply");
	public static String pathToIff = Configuration.getProp("kb_temporal_iff");

	File legendsFile = new File("tmp/legends.test.csv");
	File implicationsFile = new File("tmp/implications.test.csv");
	File iffsFile = new File("tmp/iffs.test.csv");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		final String legends = "1,A\n2,B\n3,C\n4,D\n5,E\n6,F\n7,G\n";
		final String implications = "2,4\n2,1\n2,5\n3,5\n3,6\n4,7\n1,7\n5,7\n6,7\n";
		final String iffs = "1,4\n4,1\n";
		Util.writeAll(legendsFile.getAbsolutePath(), legends);
		Util.writeAll(implicationsFile.getAbsolutePath(), implications);
		Util.writeAll(iffsFile.getAbsolutePath(), iffs);
	}

	@After
	public void tearDown() throws Exception {
		legendsFile.delete();
		implicationsFile.delete();
		iffsFile.delete();
	}

	@Test
	public void testAllreachablesOfB() {
		TemporalImplicationLatticeGenerator generator = new TemporalImplicationLatticeGenerator(
				legendsFile.getAbsolutePath(), implicationsFile.getAbsolutePath(), iffsFile.getAbsolutePath());

		assertArrayEquals(new String[] { "D", "E", "G" },
				generator.findAllReachable().get("B").toArray(new String[] {}));

	}

	@Test
	public void testAllrevReachablesOfG() {
		TemporalImplicationLatticeGenerator generator = new TemporalImplicationLatticeGenerator(
				legendsFile.getAbsolutePath(), implicationsFile.getAbsolutePath(), iffsFile.getAbsolutePath());
		assertTrue(
				Arrays.asList( "D", "B", "C", "E", "F" ).stream().sorted().collect(Collectors.toList()).equals(
				generator.findAllRevReachable().get("G").stream().sorted().collect(Collectors.toList()) ));

	}

}
