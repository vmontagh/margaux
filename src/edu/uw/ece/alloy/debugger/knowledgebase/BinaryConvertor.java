/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.io.Files;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.MyReporter;
import edu.uw.ece.alloy.debugger.exec.A4CommandExecuter;

/**
 * The class is used to generate the csv file for implication and inconsistency
 * graphs
 * 
 * @author vajih
 *
 */
public class BinaryConvertor {

	static void convertFRomAlloyToCSV() {
		final File pathToKB = new File("models/debugger/knowledge_base/strutural_kb/csv");
		if (!pathToKB.exists())
			pathToKB.mkdirs();

		final File pathToLegends = new File(pathToKB, "legends.csv");
		final File pathToIffs = new File(pathToKB, "iff.csv");
		final File pathToImplies = new File(pathToKB, "implies.csv");
		final File pathToIncons = new File(pathToKB, "incons.csv");

		final File tmpFolder = new File("tmp");
		if (!tmpFolder.exists())
			tmpFolder.mkdirs();

		final File relationalPatterns = new File(Configuration.getProp("relational_properties_tagged"));
		final File tmpRelationalPatterns = new File(tmpFolder, relationalPatterns.getName());
		final String relationDef = "sig S{r: set S}\n";
		final File path = new File(tmpFolder, "binary.als");
		try {
			Util.writeAll(path.getAbsolutePath(), relationDef);

			final BinaryImplicationLatticDeclarative bil = new BinaryImplicationLatticDeclarative();
			final List<String> allBinaryProperties = Collections
					.unmodifiableList(bil.getAllPatterns().stream().sorted().collect(Collectors.toList()));
			System.out.printf("%s number of binary patterns!\n", allBinaryProperties.size());

			PatternToProperty convertor = new PatternToProperty(relationalPatterns,
					new File(Configuration.getProp("temporal_properties_tagged")), path, "r");

			assert allBinaryProperties.stream().allMatch(p -> !convertor.getProperty(p, "r").equals(""));
			System.out.println("All patterns are converted to the pattern format!");

			final Map<String, Integer> coder = new HashMap<>();
			final Map<Integer, String> decoder = new HashMap<>();

			for (int i = 0; i < allBinaryProperties.size(); ++i) {
				coder.put(allBinaryProperties.get(i), i);
				decoder.put(i, allBinaryProperties.get(i));
			}
			assert allBinaryProperties.size() == coder.keySet().size();
			assert allBinaryProperties.size() == decoder.keySet().size();

			final StringBuilder tobeWritten = new StringBuilder();

			// write legends
			tobeWritten.append("\n");
			allBinaryProperties.forEach(p -> tobeWritten.append(coder.get(p)).append(',').append(p).append('\n'));
			Util.writeAll(pathToLegends.getAbsolutePath(), tobeWritten.toString());
			tobeWritten.setLength(0);
			System.out.println("Legends are written and its size is " + pathToLegends.length());

			// write implications
			tobeWritten.append("\n");
			for (String patternA : allBinaryProperties) {
				for (String patternB : bil.getAllImpliedProperties(patternA)) {
					tobeWritten.append(coder.get(patternA)).append(',').append(coder.get(patternB)).append('\n');
				}
			}
			Util.writeAll(pathToImplies.getAbsolutePath(), tobeWritten.toString());
			tobeWritten.setLength(0);
			System.out.println("Implications are written and its size is " + pathToImplies.length());

			// write iffs
			tobeWritten.append("\n");
			for (String patternA : allBinaryProperties) {
				for (String patternB : bil.getAllImpliedProperties(patternA)) {
					if (patternA.equals(patternB)) {
						tobeWritten.append(coder.get(patternA)).append(',').append(coder.get(patternB)).append('\n');
					}
				}
			}
			Util.writeAll(pathToIffs.getAbsolutePath(), tobeWritten.toString());
			tobeWritten.setLength(0);
			System.out.println("Iffs are written and its size is " + pathToIffs.length());

			// write incons
			tobeWritten.append("\n");
			final String checkingFormat = "open " + relationalPatterns.getName().replace(".als", "") + "\n"
					+ relationDef + "\n" + "pred inc{some r and %s and %s}\n" + "run inc";
			Files.copy(relationalPatterns, tmpRelationalPatterns);

			for (String patternA : allBinaryProperties) {
				for (String patternB : allBinaryProperties) {
					if (patternA.equals(patternB))
						continue;
					final File inconCheckFile = new File(tmpFolder, "incon_" + patternA + "_" + patternB + ".als");
					Util.writeAll(inconCheckFile.getAbsolutePath(), String.format(checkingFormat,
							convertor.getProperty(patternA, "r"), convertor.getProperty(patternB, "r")));

					MyReporter rep = new MyReporter();
					A4CommandExecuter.getInstance().runThenGetAnswers(inconCheckFile.getAbsolutePath(), rep, "inc");

					if (rep.sat != 1 && rep.sat != -1)
						throw new RuntimeException("The analyzing result is not expected. rep.sat=" + rep.sat);

					if (rep.sat == -1) {
						tobeWritten.append(coder.get(patternA)).append(',').append(coder.get(patternB)).append('\n');
					}
					inconCheckFile.deleteOnExit();
				}
			}
			Util.writeAll(pathToIncons.getAbsolutePath(), tobeWritten.toString());
			tobeWritten.setLength(0);
			System.out.println("Incons are written and its size is " + pathToIncons.length());

		} catch (Err | IOException e) {
			e.printStackTrace();
		} finally {
			path.deleteOnExit();
			tmpRelationalPatterns.deleteOnExit();
		}

	}

	static void convertFromCSVToLatexImplication() {
		final BinaryImplicationLatticImperative bili = new BinaryImplicationLatticImperative();

		final Queue<String> patterns = new LinkedList<>();
		final Set<String> visitedPatterns = new HashSet<>();
		final StringBuilder result = new StringBuilder();
		result.append("\\begin{table}[h]\n"+
				"\t\\center\n" +
				"\t\\begin{tabular}{|c|c|}\n" +
				"\t\t\\hline\n" +
				"\t\\rowcolor{black!25}\n" +
				"\t\t\\code{From} & \\code{To} \\\\\n"+
				"\t\t\\hline\n"
				);
		try {
			final Set<String> sinks = new HashSet<>(bili.getAllSinks());
			final Set<String> sources = new HashSet<>(bili.getAllSources());

			patterns.addAll(sources.stream().sorted().collect(Collectors.toList()));
			while (!patterns.isEmpty()) {
				final String pattern = patterns.poll();
				if (visitedPatterns.contains(pattern)) {
					continue;
				}
				visitedPatterns.add(pattern);
				Collection<String> impliedPatterns = bili.getNextImpliedProperties(pattern).stream().sorted()
						.collect(Collectors.toList());
				patterns.addAll(impliedPatterns);
				if (impliedPatterns.isEmpty()) {
					sinks.add(pattern);
				} else {
					final AtomicInteger i = new AtomicInteger(1);
					final String implied = impliedPatterns.stream()
							.map(s->sinks.contains(s)? "\\sout{\\code{"+s+"}}" : "\\code{"+s+"}")
							.map(s -> i.getAndIncrement()%2 == 0 ? s+"\\\\":s)
							.collect(Collectors.joining(", ", "\\specialcell{", "}"));
					result.append("\t\t").append(sources.contains(pattern)? "\\textbf{\\code{"+pattern+"}" : "\\code{"+pattern).append("}&").append(implied).append("\\\\ \\hline\n");
				}
			}
			
			result.append("\t\\end{tabular}\n"+
						"\\end{table}\n");
			
			System.out.println(result);
		} catch (Err e) {
			e.printStackTrace();
		}
	}
	
	static public void convertCSVToLatexInconsistencies(){
		BinaryInconsistencyGraph big = new BinaryInconsistencyGraph();
		for (String pattern: big.getAllPatterns()){
			System.out.println(pattern + "->"+big.getAllInconsistecies(pattern));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// convertFRomAlloyToCSV();
		//convertFromCSVToLatexImplication();
		convertCSVToLatexInconsistencies();
	}

}
