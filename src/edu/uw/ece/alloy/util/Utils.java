package edu.uw.ece.alloy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import edu.mit.csail.sdg.alloy4.Pos;

public class Utils {
	private Utils() {
		throw new UnsupportedOperationException();
	}

	
	public static String appendFileName(final String folderName, final String fileName){
		return folderName + 
				(( folderName != null && folderName.substring(folderName.length() - 1).equals(File.separator) ) ? "" : File.separator ) 
				+ fileName;
	}
	
	/**
	 * Read input file to String.
	 * @param inputFileName
	 * @return
	 */
	public static String readFile(final String inputFileName) {
		try {
			final File f = new File(inputFileName);
			final long length = f.length();
			if (length < 1000000) {
				final FileReader fr = new FileReader(inputFileName);
				final BufferedReader br = new BufferedReader(fr);
				final char[] c = new char[(int)length];
				br.read(c);
				return new String(c);
			} else {
				throw new RuntimeException("File is too big! " + inputFileName);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Read the input file, and return the code snippet from (x,y)->(x1,y1)
	 * @param inputFileName
	 * @param pos
	 * @return
	 */
	public static String readSnippet(final Pos pos){
		
		final StringBuilder snippet = new StringBuilder(); 
				
		try {
			
			final BufferedReader bufferedReader = new BufferedReader(new FileReader(pos.filename));
			
			String line = bufferedReader.readLine();
			for(int i = 1; line != null && i <= pos.y2 ;++i){
				
				if( i == pos.y && i == pos.y2 ){
					snippet.append( line.substring( pos.x-1 , pos.x2));
				}else if( i == pos.y && i < pos.y2  ){
					snippet.append( line.substring( pos.x-1 )).append("\n");
				}else if(i > pos.y && i < pos.y2 ){
					snippet.append( line).append("\n");
				}else if(i > pos.y && i == pos.y2){
					snippet.append( line.substring( 0, pos.x2));
				}
				
				line = bufferedReader.readLine();
			}
			
			bufferedReader.close();
			
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		return snippet.toString();
		
	}

	/**
	 * Read non-blank lines from file into a list of strings.
	 * @param inputFileName
	 * @return
	 */
	// TODO: remove these methods from Utils351
	public static List<String> readFileLines(final String inputFileName) {
		try {
			
			final BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileName));
			final List<String> lines = new ArrayList<String>();
			
			String line = null;
			while ((line= bufferedReader.readLine()) != null) {
				if (line.trim().length() > 0) {
					lines.add(line);
				}
			}
			bufferedReader.close();
			
			return Collections.unmodifiableList(lines);
			
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static File[] files(final String dirName, final String regex) {
		final File dir = new File(dirName);
		assert dir.isDirectory() : "not a directory: " + dir;
		final Pattern p = Pattern.compile(regex);
		final File[] result = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File f, final String s) {
				final boolean m = p.matcher(s).matches();
				return m;
			}
		});
		assert result != null;
		Arrays.sort(result);
		return result;

	}

	public static File[] filesR(final String dirName, final String regex) {
		// preconditions
		final File dir = new File(dirName);
		assert dir.isDirectory() : "not a directory: " + dir;
		// get all subdirs (recursively)
		final File[] subdirs = subdirsR(dir);
		// get all matching files in all subdirs
		final List<File> result = new ArrayList<File>();
		for (final File d : subdirs) {
			final File[] files = files(d.getAbsolutePath(), regex);
			for (final File f : files) { result.add(f); }
		}
		return result.toArray(new File[]{});
	}
	
	public static File[] subdirs(final File dir) {
		// precondition
		assert dir.isDirectory() : 
			"not a directory: " + dir;
		// subdirs
		final File[] subdirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File f) {
				final boolean b = f.isDirectory();
				return b;
			}
		});
		return subdirs;
	}

	public static File[] subdirsR(final File root) {
		// precondition
		assert root.isDirectory() : "not a directory: " + root;
		// storage
		final List<File> result = new ArrayList<File>();
		result.add(root);
		final Stack<File> worklist = new Stack<File>();
		worklist.push(root);
		// subdirs
		while (!worklist.isEmpty()) {
			final File d = worklist.pop();
			final File[] subdirs = subdirs(d);
			for (final File s : subdirs) { 
				result.add(s);
				worklist.push(s); 
			}
		}
		Collections.sort(result);
		return result.toArray(new File[]{});
	}

}
