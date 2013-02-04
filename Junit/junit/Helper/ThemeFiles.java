package junit.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class ThemeFiles {
	Map<String, String> ThemeMap;
	
	public ThemeFiles(String csvname){
		ThemeMap = new TreeMap<String, String>();
		ThemeMap = makeThemeMap(csvname);	
	}
	
   public String FindThemeFile(String instancename){
	   	return ThemeMap.get(instancename); 
   }
   
   private Map<String, String> makeThemeMap(String csvname){
	   
	   try{
		   File file = new File(csvname);
		   BufferedReader bufRdr = new BufferedReader(new FileReader(file));
		   String line = null;
		   
		   while((line = bufRdr.readLine()) != null){
			   StringTokenizer st = new StringTokenizer(line,",");
			   		String Key = st.nextToken();
			   		String Val = st.nextToken();
			   		ThemeMap.put(Key, Val);	   
		   }
		   bufRdr.close();
		   
	   }catch(Exception e){System.out.println(e);}
	   return ThemeMap;
   }
   

   //"/home/atulan/alloyvizexamples/Logging_spreadsheet/ModelsWithExpert.csv"
}
