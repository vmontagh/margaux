package junit.Helper;

import java.util.List;
import java.util.Map;
import java.io.*;
import java.io.IOException;

public class HTMLRender {
	
	public FileWriter HTML;
	
	public HTMLRender(String filename){
		try{HTML = new FileWriter(filename);}
		catch(Exception e){System.out.println(e);}
	}
	
	public void doHTMLRender(Map<String, List<String>> TableSource) throws IOException{	
		writeDMHeading();
		writeDMTable(TableSource);
		writeFooter();
		HTML.close();
	}
	
	public void doTailoringRender(Map<String, List<Integer>> TailorBook, Integer maxWidth, Integer maxDepth )throws IOException{
		writeTailorHeading();
		writeTailorTable(TailorBook, maxWidth, maxDepth);
		writeFooter();
		HTML.close();
	}
	
	private void writeTailorHeading() throws IOException{
		HTML.write("<html>\n<head>\n<title>Model Depth Width</title>\n</head>\n<body>\n<table border=\"1\">\n<tr>\n<th>Model</th>\n<th>Width</th>\n<th>Depth</th>\n</tr>\n");
	}
	
	private void writeTailorTable(Map<String, List<Integer>> TailorBook, Integer maxWidth, Integer maxDepth) throws IOException{
		for (String Command: TailorBook.keySet()){
			HTML.append("<tr>\n<td>"+Command+"<td>\n<td>"+TailorBook.get(Command).get(1)+"<td>\n<td>"+TailorBook.get(Command).get(0)+"<td>\n<tr>\n");
		}
		HTML.append("<p>MaxWidth: "+maxWidth+"</p>\n"+
					"<p>MaxDepth: "+maxDepth+"</p>\n");
		
	}
	
	private void writeDMHeading() throws IOException {
		HTML.write("<html>\n<head>\n<title>Distance Metric Results</title>\n</head>\n<body>\n<table border=\"1\">\n<tr>\n<th>Model</th>\n<th>Distance</th>\n</tr>\n");
	}

	private void writeDMTable (Map<String, List<String>> Source )throws IOException {
		for(String Command : Source.keySet()){
			HTML.append("<tr>\n<td>"+Command+"<td>\n<td>"+Source.get(Command).toString()+"<td>\n<tr>\n");
		}
	}
	
	private void writeFooter() throws IOException{
		HTML.append("</body>\n</html>\n");
	}
	
}
