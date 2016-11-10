/**
 * 
 */
package edu.uw.ece.alloy.debugger.knowledgebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;

/**
 * Converting temporal properties to other formats, such as Latex
 * @author vajih
 *
 */
public class TemporalConvertor {

	static final Path pathPropsName = Paths.get("/USers/vajih/Documents/Papers/papers/thesis/doc/rampd/code", "props.csv");

	/**
	 * 
	 */
	public TemporalConvertor() {
		// TODO Auto-generated constructor stub
	}

	static List<List<String>> readRecords(Path path, String separator, boolean hasHeader) {
		try(Reader source = Files.newBufferedReader(
				path, Charset.forName("UTF-8"))){
			try (BufferedReader reader = new BufferedReader(source)) {
				return reader.lines()
						.substream(hasHeader? 1 : 0)
						.map(line -> Arrays.asList(line.split(separator)))
						.collect(Collectors.toList());
			}} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
	}  

	public static String convertToLatex() throws Err{
		ImplicationLattic implication =  new TernaryImplicationLatticImperative();
		Set<String> sources = new HashSet<>();
		Set<String> sinks = new HashSet<>();
		try {
			sources.addAll(implication.getAllSources());
			sinks.addAll(implication.getAllSinks());
		} catch (Err e) {
			e.printStackTrace();
		}

		StringBuilder result = new StringBuilder();
		List<String> toBeDone = new ArrayList<>(sources);
		Set<String> done = new HashSet<>();

		//ListIterator<String> toBeDoneIt = toBeDone.listIterator();

		List<String> tables  = new ArrayList<>();
		
		final int rows = 38;
		int rowcount = 1;
		
		boolean firstTable = true;
		
		while (  toBeDone.size() > 0 ){
	
			String pattern1 = toBeDone.remove(0);
			if (done.contains(pattern1))
				continue;
			String patternA = sources.contains(pattern1) ? "\\Overline{{\\textbf{\\code{"+newName(pattern1)+"}}}}": "\\code{"+newName(pattern1)+"}";
			
			List<Pair<String, Integer>> dest = new ArrayList<>();
			for (String pattern2: implication.getNextImpliedProperties(pattern1)){
				String patternB = sinks.contains(pattern2) ? "\\underline{{\\emph{\\code{"+newName(pattern2)+"}}}}": "\\code{"+newName(pattern2)+"}";
				dest.add(new Pair<>( patternB,newName(pattern2).length()));
				if (!sinks.contains(pattern2))
					toBeDone.add(pattern2);
				
			}
			
			String destString = "\\specialcell{";
			int lenSofar = newName(pattern1).length();
			for (int i = 1; i <= dest.size(); ++i){
				lenSofar += dest.get(i-1).b;
				
				if (i == dest.size()){
					destString += dest.get(i-1).a + "}";
					++rowcount;
				} else {
					destString += dest.get(i-1).a + ", ";
					if (lenSofar > 60){
						destString += "\\\\";
						lenSofar = newName(pattern1).length();
						++rowcount;
					}
				}
				
			}
			
			result.append(patternA).append(" & ").append(destString).append("\\\\\\hline\n");
			final int rowsLimit = firstTable ? rows - 5 : rows;
			if (rowcount%(rowsLimit-4) == 0 || rowcount%(rowsLimit-3) == 0 || rowcount%(rowsLimit-2) == 0 || rowcount%(rowsLimit-1) == 0 || rowcount%rowsLimit == 0){
				tables.add(result.toString());
				rowcount = 1;
				result = new StringBuilder();
				firstTable = false;
			}
			done.add(pattern1);
		}

		
		String firstCaption = "\\begin{table}\n"
				+ "\\caption[Ternary Implication Lattice]{The Ternary Implication Lattice comprises 160 patterns, each represented as a node.\n"
				+ "An edge of the lattice encodes an implication relation between two patterns.\n"
				+ "The strongest patterns, i.e. sources in the lattice, are distinguished in bold with overline.\n"
				+ "Patterns in italic with underline on them are the weakest patterns or sinks of the lattice.}\n\\label{tbl:ternary:lattice}\n";
		String restCpation = "\\begin{table}\n\\caption{Continue Table~\\ref{tbl:ternary:lattice}}\n";
		
		String tablesLatex = "";
		
		String hearder = "\\scriptsize\n\\center\n{\\renewcommand{\\arraystretch}{1.5}%\n\\begin{tabular}{|c|c|}\n\\hline\n\\rowcolor{black!25}\n\\code{From} & \\code{To} \\\\\n\\hline\\hline\n";
		String footer = "\\end{tabular}\n}%end of scope for arraystretch\n\\end{table}";
		
		firstTable = true;
		for (String table: tables){
			tablesLatex = tablesLatex + "\n" + (firstTable ? firstCaption : restCpation ) + hearder + table + footer;
			firstTable = false;
		}
		
		return tablesLatex;

	}

	static String newName(String name){
		String newNames = name.toString();
		for (List<String> map: readRecords(pathPropsName, ",", false)){
			String key = map.get(0);
			String value = map.size() == 2? map.get(1) : "";
			newNames = newNames.replaceAll(key, value);
		}
		return newNames.replaceAll("_", "\\\\_");
	}
	
	/**
	 * @param args
	 * @throws Err 
	 */
	public static void main(String[] args) throws Err {
		Util.writeAll("/Users/vajih/Documents/Papers/papers/thesis/doc/rampd/lib-ternary-implication.tex", convertToLatex());

	}

}
