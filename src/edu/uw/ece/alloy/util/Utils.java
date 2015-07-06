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
			.substream(1)
			.map(line -> Arrays.asList(line.split(",")))
			.forEach( toMaps);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static byte[] compress(final String text, final String encoding) throws Exception{
		byte[] output = new byte[text.length()+3];
		Deflater compresser = new Deflater();
		compresser.setInput(text.getBytes(encoding));
		compresser.finish();
		int compressedDataLength = compresser.deflate(output);
		byte[] dest = new byte[compressedDataLength];
		System.arraycopy(output, 0, dest, 0, compressedDataLength);
		return dest;
	}

	public static String decompress(byte[] bytes, final String encoding) throws Exception{
		Inflater decompresser = new Inflater();
		decompresser.setInput(bytes, 0, bytes.length);
		byte[] result = new byte[bytes.length *10];
		int resultLength = decompresser.inflate(result);
		decompresser.end();
		// Decode the bytes into a String
		String outputString = new String(result, 0, resultLength, encoding);
		return outputString;
	}

	
	public static void main(String ...args) throws Exception{
		String input = "There are several methodsnanfn.l";//available in the Reflection API that can be used to retrieve annotations. The behavior of the methods that return a single annotation, such as AnnotatedElement.getAnnotationByType(Class<T>), are unchanged in that they only return a single annotation if one annotation of the requested type is present. ";
		
		byte[] compressed = compress(input, "UTF-8");
		String uncompressed = decompress(compressed, "UTF-8");
		
		System.out.println(input);
		System.out.println(input.length());
		System.out.println(compressed.length);
		System.out.println(uncompressed);
		System.out.println(input.equals(uncompressed));
		
		
		
		try {
		     // Encode a String into bytes
		     String inputString = "There are several methodsnanfn.l";
		     byte[] input2 = inputString.getBytes("UTF-8");

		     // Compress the bytes
		     byte[] output = new byte[100];
		     Deflater compresser = new Deflater();
		     compresser.setInput(input2);
		     compresser.finish();
		     int compressedDataLength = compresser.deflate(output);

		     // Decompress the bytes
		     Inflater decompresser = new Inflater();
		     decompresser.setInput(output, 0, compressedDataLength);
		     byte[] result = new byte[100];
		     int resultLength = decompresser.inflate(result);
		     decompresser.end();

		     // Decode the bytes into a String
		     String outputString = new String(result, 0, resultLength, "UTF-8");
		     System.out.println(inputString);
		     System.out.println(outputString);
		 } catch(java.io.UnsupportedEncodingException ex) {
		     // handle
		 } catch (java.util.zip.DataFormatException ex) {
		     // handle
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
