package edu.uw.ece.alloy.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import edu.mit.csail.sdg.alloy4.Err;
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

	public static void appendFile(final String path, final String content){
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)))) {
		    out.println(content);
		}catch (IOException e) {
			throw new RuntimeException("CAnnot append to the file " + path);
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

	public static void moveFiles(final File[] files, final File dest){
		assert dest.isDirectory();
		System.out.println(files.length);
		for(final File file: files){

			try {
				Files.move(file.toPath(), 
						dest.toPath().resolve(file.toPath().getParent().relativize(file.toPath())), StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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

	public static void deleteRecursivly(final File root) throws IOException{
		// precondition
		assert root.isDirectory() : "not a directory: " + root;

		Files.walkFileTree(root.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});

	}

	public static DirectoryStream<Path> filesStream(final File dir, final String regex) {
		assert dir.isDirectory() : "not a directory: " + dir;
		final Pattern p = Pattern.compile(regex);
		final DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			public boolean accept(Path file) throws IOException {
				return (p.matcher(file.getFileName().toString()).matches());
			}
		};

		DirectoryStream<Path> stream = null;
		try {
			stream = Files.newDirectoryStream(dir.toPath(), filter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assert stream != null;
		return stream;

	}



	public static void replaceTextFiles(final File dir, final String regexFileName, final String resultFile, final Map<String, String> replaceMapping) throws Err{

		assert dir.isDirectory() : "not a directory: " + dir;

		Function<String,String> mapper = replaceMapping.entrySet().stream()
				.reduce(Function.identity(),
						(func, entry) -> {
							String key = entry.getKey();
							String value = entry.getValue();
							return func.compose(s -> s.replaceAll(key, value));
						},
						Function::compose);

		String resultString = Arrays.stream(files(dir.getAbsolutePath(),regexFileName)).
				//parallelStream().
				//stream().
				map( p ->  Utils.readFile(p.getAbsolutePath()).trim().concat("\n")  ).
				map( mapper ).
				collect(Collectors.joining());

		//System.out.println(resultString);
		//System.out.println(files(dir,regex)[0].getAbsoluteFile().getParent() );
		edu.mit.csail.sdg.alloy4.Util.writeAll( (new File(dir, resultFile)).getAbsolutePath(), resultString);

	}


	public static void readFile( final File file, final Consumer<List<String>> toMaps){

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.lines()
			.skip(1)
			.map(line -> Arrays.asList(line.split(",")))
			.forEach( toMaps);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void setFinalStatic(Field field, Object newValue) throws Exception {
	    field.setAccessible(true);

	    // remove final modifier from field
	    Field modifiersField = Field.class.getDeclaredField("modifiers");
	    modifiersField.setAccessible(true);
	    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

	    field.set(null, newValue);
	}
	
}
