package junit.MagicLayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.Helper.RunVizTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.mit.csail.sdg.alloy4viz.AlloyType;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.alloy4viz.MagicUtil;

@RunWith(value = Parameterized.class)
public class NodeVisibility {
	VizGUI viz = null;
	Map<String, Integer> nodeVisible = new TreeMap<String, Integer>();
	String filename;
	Integer checksum;
	
	public NodeVisibility(String filename){
		this.filename = filename;
		this.checksum = 0;
	}
	
	@Before
	public void Compile(){
		String[] filenames = {filename};
		RunVizTest Compiler =  new RunVizTest();
		List<String> instancefiles = new ArrayList<String>();
		try{
			System.out.println(filenames);
			instancefiles = Compiler.Execute(filenames);
			nodeVisible = ProjectedTypes(instancefiles);
		}
		catch(Exception e){
			System.out.println(e);
			fail("Error: No instances to run");
		}
	}
	
	public Map<String,Integer> ProjectedTypes(List<String> instancefiles){
		for(String name: instancefiles){
			if (viz==null) {
	            viz = new VizGUI(false, name, null);
	        }
	        viz.loadXML(name, true);
	        viz.doMagicLayout("yes");
	        Set<AlloyType> topLevelTypes = MagicUtil.partiallyVisibleUserTopLevelTypes(viz.getVizState());	        
	        for(AlloyType t: topLevelTypes)
	        	if(MagicUtil.isActuallyVisible(viz.getVizState(), t)) nodeVisible.put(name, 1);
	        else nodeVisible.put(name, 0);
		}
        return nodeVisible;
	}
	
	
	@Test
	public void testMagic() {
		for(String name: nodeVisible.keySet()){
            System.out.println("HIT!!!"+nodeVisible);
			if(nodeVisible.get(name).intValue()==1){
				checksum++;
			}
		}
	}
	
	@Parameters
	public static Collection<Object[]> data(){
		return Arrays.asList(new Object[][] {
//				{"models/book/appendixA/addressBook1.als"},
//				{"models/book/appendixA/addressBook2.als"},
//				{"models/book/appendixA/barbers.als"},
//				{"models/book/appendixA/closure.als"},
//				{"models/book/appendixA/distribution.als"},
//				{"models/book/appendixA/phones.als"},
//				{"models/book/appendixA/prison.als"},
//				{"models/book/appendixA/properties.als"},
//				{"models/book/appendixA/ring.als"},
//				{"models/book/appendixA/spanning.als"},
//				{"models/book/appendixA/tree.als"},
//				{"models/book/appendixA/tube.als"},
//				{"models/book/appendixA/undirected.als"},
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
//				{"models/book/chapter4/filesystem.als"},
//				{"models/book/chapter4/grandpa1.als"},
//				{"models/book/chapter4/grandpa2.als"},
//				{"models/book/chapter4/grandpa3.als"},
//				{"models/book/chapter4/lights.als"},
//				{"models/book/chapter5/addressBook.als"},
//				{"models/book/chapter5/lists.als"},
//				{"models/book/chapter5/sets1.als"},
//				{"models/book/chapter5/sets2.als"},
//				{"models/book/chapter6/hotel1.als"},
//				{"models/book/chapter6/hotel2.als"},
//				{"models/book/chapter6/hotel3.als"},
//				{"models/book/chapter6/hotel4.als"},
//				{"models/book/chapter6/mediaAssets.als"},
//				{"models/book/chapter6/memory/abstractMemory.als"},
//				{"models/book/chapter6/memory/cacheMemory.als"},
//				{"models/book/chapter6/memory/checkCache.als"},
//				{"models/book/chapter6/memory/checkFixedSize.als"},
//				{"models/book/chapter6/memory/fixedSizeMemory.als"},
//				{"models/book/chapter6/memory/fixedSizeMemory_H.als"},
//				{"models/book/chapter6/ringElection1.als"},
//				{"models/book/chapter6/ringElection2.als"},
//				{"models/examples/algorithms/dijkstra.als"},
//				{"models/examples/algorithms/messaging.als"},
//				{"models/examples/algorithms/opt_spantree.als"},
//				{"models/examples/algorithms/peterson.als"},
//				{"models/examples/algorithms/ringlead.als"},
//				{"models/examples/algorithms/s_ringlead.als"},
//				{"models/examples/algorithms/stable_mutex_ring.als"},
//				{"models/examples/algorithms/stable_orient_ring.als"},
//				{"models/examples/algorithms/stable_ringlead.als"},
//				{"models/examples/case_studies/INSLabel.als"},
//				{"models/examples/case_studies/chord.als"},
//				{"models/examples/case_studies/chord2.als"},
//				{"models/examples/case_studies/chordbugmodel.als"},
//				{"models/examples/case_studies/com.als"},
//				{"models/examples/case_studies/firewire.als"},
//				{"models/examples/case_studies/ins.als"},
//				{"models/examples/case_studies/iolus.als"},
//				{"models/examples/case_studies/sync.als"},
//				{"models/examples/case_studies/syncimpl.als"},
//				{"models/examples/puzzles/farmer.als"},
//				{"models/examples/puzzles/handshake.als"},
//				{"models/examples/puzzles/hanoi.als"},
//				{"models/examples/systems/file_system.als"},
//				{"models/examples/systems/javatypes_soundness.als"},
//				{"models/examples/systems/lists.als"},
//				{"models/examples/systems/marksweepgc.als"},
//				{"models/examples/systems/views.als"},
//				{"models/examples/toys/birthday.als"},
//				{"models/examples/toys/ceilingsAndFloors.als"},
//				{"models/examples/toys/genealogy.als"},
//				{"models/examples/toys/grandpa.als"},
//				{"models/examples/toys/javatypes.als"},
//				{"models/examples/toys/life.als"},
//				{"models/examples/toys/numbering.als"},
//				{"models/examples/toys/railway.als"},
//				{"models/examples/toys/trivial.als"},
				{"models/examples/tutorial/farmer.als"},
//				{"models/flashfs/concreteFilesystem.als"},
//				{"models/flashfs/refinement.als"},
//				{"models/flashfs/abstractFilesystem.als"},
//				{"models/flashfs/flash.als"}

		});
	}

}