package junit.MagicLayout;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.Helper.CSVRender;
import junit.Helper.RunVizTest;
import junit.Helper.HTMLRender;
import junit.Helper.ThemeFiles;
import junit.Helper.doDistanceMetric;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.mit.csail.sdg.alloy4viz.VizGUI;

@RunWith(value = Parameterized.class)
public class DistanceMetric {
	
/************ Global Variables ******************/	
	public static VizGUI ML, Expert = null;
	Map<String, Integer> projectedtypes = new TreeMap<String, Integer>();
	Boolean successfullyCreated = false;
	String filename, themefile;
	
/************ Constructor ****************/
	public DistanceMetric(String filename){
		ThemeFiles CSV = new ThemeFiles("ModelsWithExpert.csv");
		this.themefile = CSV.FindThemeFile(filename);
		this.filename = filename;
	}
	
/************ Code ***********************/	
	
	@AfterClass
//	public static void RenderHTML() throws IOException{
//		HTMLRender render = new HTMLRender("tmp/DistanceMetric3.html");
//		render.doHTMLRender(doDistanceMetric.DiffsList);
//	}
	
	public static void RenderCSV() throws IOException{
		CSVRender csvrender =  new CSVRender("tmp/DistanceMetric1.csv");
		csvrender.doCSVRender(doDistanceMetric.DiffsList);
		
		HTMLRender render = new HTMLRender("tmp/DistanceMetric1.html");
		render.doHTMLRender(doDistanceMetric.DiffsList);
	}
	
	@Before
	public void Compile(){
		String[] filenames = {filename};
		RunVizTest Compiler =  new RunVizTest();
		List<String> instancefiles = new ArrayList<String>();
		try{
			instancefiles = Compiler.Execute(filenames);
			for(String command : instancefiles){
				CreateMagicLayoutInstances(command);
				successfullyCreated= doDistanceMetric.Compute(ML, Expert);
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	private void CreateMagicLayoutInstances(String command){
			ML = new VizGUI(false, command, null);
			Expert = new VizGUI(false, command, null);
//			ML.doMagicLayout("yes"); 
			try{Expert.loadThemeFile(themefile);}
			catch(Exception e){System.out.println(e);};
	}
	
	@Test
	public void testMagic() {
		assertTrue("SuccessFully Created Distance Metric", successfullyCreated);
	}
/****************** List of Files **************************/	

	@Parameters
	public static Collection<Object[]> data(){
		return Arrays.asList(new Object[][] {
//				{"models/book/appendixE/p300-hotel.als"},
//				{"models/book/appendixE/p303-hotel.als"},
//				{"models/book/appendixE/p306-hotel.als"},
//				{"models/book/chapter2/addressBook1a.als"},
//				{"models/book/chapter2/addressBook1b.als"},
//				{"models/book/chapter2/addressBook1c.als"},
//				{"models/book/chapter2/addressBook1d.als"},
//				{"models/book/chapter2/addressBook1e.als"},
//				{"models/book/chapter2/addressBook1f.als"},
//				{"models/book/chapter2/addressBook1g.als"},
//				{"models/book/chapter2/addressBook1h.als"},
//				{"models/book/chapter2/addressBook2a.als"},
//				{"models/book/chapter2/addressBook2b.als"},
//				{"models/book/chapter2/addressBook2c.als"},
//				{"models/book/chapter2/addressBook2d.als"},
//				{"models/book/chapter2/addressBook2e.als"},
//				{"models/book/chapter2/addressBook3a.als"},
//				{"models/book/chapter2/addressBook3b.als"},
//				{"models/book/chapter2/addressBook3c.als"},
//				{"models/book/chapter2/addressBook3d.als"},
				{"models/book/chapter6/hotel1.als"},
//				{"models/book/chapter6/hotel2.als"},
//				{"models/book/chapter6/hotel3.als"},
//				{"models/book/chapter6/hotel4.als"},
//				{"models/book/chapter6/ringElection1.als"},
//				{"models/book/chapter6/ringElection2.als"},
//				{"models/examples/algorithms/dijkstra.als"},
//				{"models/examples/algorithms/messaging.als"},
//				{"models/examples/algorithms/opt_spantree.als"},
//				{"models/examples/algorithms/ringlead.als"},
//				{"models/examples/algorithms/stable_mutex_ring.als"},
//				{"models/examples/algorithms/stable_orient_ring.als"},
//				{"models/examples/algorithms/stable_ringlead.als"},
//				{"models/examples/case_studies/firewire.als"},
//				{"models/examples/puzzles/farmer.als"},
//				{"models/examples/puzzles/handshake.als"},
//				{"models/examples/puzzles/hanoi.als"},
//				{"models/examples/systems/file_system.als"},
//				{"models/examples/systems/lists.als"},
//				{"models/examples/toys/birthday.als"},
//				{"models/examples/toys/ceilingsAndFloors.als"},
//				{"models/examples/toys/genealogy.als"},
//				{"models/examples/toys/grandpa.als"},
//				{"models/examples/toys/life.als"},
//				{"models/examples/toys/railway.als"},
//				{"models/flashfs/concreteFilesystem.als"},
//				{"models/flashfs/refinement.als"},
//				{"models/flashfs/flash.als"},
//				{"models/aleks/mst.als"}
		});
	}
}