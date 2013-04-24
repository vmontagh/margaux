package edu.mit.csail.sdg.gen;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.mit.csail.sdg.alloy4.Err;

public class Util {

	public static synchronized void Logger(String reportFile, String... params){
		String out = "";
		try {
			out = edu.mit.csail.sdg.alloy4.Util.readAll(reportFile);
		} catch (FileNotFoundException e) {}
		catch (IOException e) {e.printStackTrace();}

		StringBuilder sb = new StringBuilder();
		sb.append(out);
		if(params.length > 0)
			sb.append("\n");
		for(String param:params){
			sb.append(param);
			sb.append(",");
		}
		if(params.length > 0)
			sb.deleteCharAt(sb.length()-1);
		try{
			edu.mit.csail.sdg.alloy4.Util.writeAll(reportFile, sb.toString());
		} catch (Err e) {
			e.printStackTrace();
		}

	}
	
}
