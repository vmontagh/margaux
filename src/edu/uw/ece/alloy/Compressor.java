package edu.uw.ece.alloy;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;

public class Compressor {

	public Compressor() {
		// TODO Auto-generated constructor stub
	}
	
	public enum STATE {COMPRESSED, DEOMPRESSED}; 
	
	final public static String ENCODING = "UTF-8";
	
	final public static byte[] EMPTY_1D = {};
	final public static byte[][] EMPTY_2D = {{}};
	final public static String EMPTY_STRING = "N";
	final public static File EMPTY_FILE = new File( EMPTY_STRING );
	final public static int EMPTY_PRIORITY = -1;
	final public static DBConnectionInfo EMPTY_DBCONNECTION = new DBConnectionInfo(EMPTY_STRING,EMPTY_STRING,EMPTY_STRING,EMPTY_STRING);
	final public static List EMPTY_LIST = Collections.unmodifiableList(Collections.emptyList());
	public final static DBConnectionInfo EMPTY_DBDONNECTIONINFO = new DBConnectionInfo(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING); 
	
	
	public static byte[] compress(final String text) throws Exception{
		return compress(text, ENCODING);
	}

	public static String decompress(byte[] bytes) throws Exception{
		return decompress(bytes,ENCODING);
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
		byte[] result = new byte[bytes.length *100];
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

}
