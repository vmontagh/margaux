package junit.Helper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVRender {
	public FileWriter CSV;
	
	public CSVRender(String filename){
		try{CSV = new FileWriter(filename);}
		catch(Exception e){System.out.println(e);}
	}
	
	
	public void doCSVRender(Map<String, List<String>> TableSource) throws IOException{	
		for(String Command : TableSource.keySet()){
			CSV.write(Command+","+getDistance(TableSource.get(Command))+"\n");
		}
		CSV.close();
	}


	private String getDistance(List<String> list) {
		int Distance = 0;
		for(String val : list){
			if(val == "Projection") Distance = Distance + 8;
			else if(val == "Projection(Subset)" ) Distance = Distance + 5;
			else if(val == "Projection(Intersection)") Distance = Distance + 6;
			else if(val == "NodeVisibility")Distance = Distance + 8;
			else if(val == "NodeVisibility(Subset)")Distance = Distance + 6;
			else if(val == "NodeVisibility(Intersection)")Distance = Distance + 7;
			else if(val == "Spine")Distance = Distance + 0;
			else if(val == "Attribute")Distance = Distance + 5;
			else if(val == "Attribute(Subset)")Distance = Distance + 3;
			else if(val == "Attribute(Intersection)")Distance = Distance + 4;
			else if(val == "Edge Labels/Node Names")Distance = Distance + 0;
			else if(val == "Edge Labels/Node Names(Subset)")Distance = Distance + 0;
			else if(val == "Edge Labels/Node Names(Intersection)")Distance = Distance + 0;
			else if(val == "Shape")Distance = Distance + 3;
			else if(val == "Node Colour")Distance = Distance + 6;
			else if(val == "Node Style")Distance = Distance + 0;
			else Distance = Distance + 0;
		}
		
		return Integer.toString(Distance);
	}
	
	
}