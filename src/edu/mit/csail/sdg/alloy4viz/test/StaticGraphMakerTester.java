package edu.mit.csail.sdg.alloy4viz.test;

import static org.junit.Assert.*;

import java.awt.List;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4graph.GraphNode;
import edu.mit.csail.sdg.alloy4graph.GraphViewer;
import edu.mit.csail.sdg.alloy4viz.AlloyAtom;
import edu.mit.csail.sdg.alloy4viz.AlloyInstance;
import edu.mit.csail.sdg.alloy4viz.AlloyModel;
import edu.mit.csail.sdg.alloy4viz.AlloyProjection;
import edu.mit.csail.sdg.alloy4viz.AlloyRelation;
import edu.mit.csail.sdg.alloy4viz.AlloySet;
import edu.mit.csail.sdg.alloy4viz.AlloyTuple;
import edu.mit.csail.sdg.alloy4viz.AlloyType;
import edu.mit.csail.sdg.alloy4viz.StaticGraphMaker;
import edu.mit.csail.sdg.alloy4viz.VizState;

public class StaticGraphMakerTester {

	Map map1 = new HashMap<AlloyAtom, ConstList<AlloySet>>();
	ArrayList<AlloyType> types = new ArrayList<AlloyType>();
	ArrayList<AlloyAtom> atoms = new ArrayList<AlloyAtom>();
	Map<AlloyType, Integer> numOfTypes = new HashMap<AlloyType, Integer>();
	HashMap<AlloyType, AlloyType> typeHier = new HashMap<AlloyType, AlloyType>();
	HashMap<AlloyRelation, Set<AlloyTuple>> rel2tuples = new HashMap<AlloyRelation, Set<AlloyTuple>>();
	Map<AlloyAtom,Set<AlloySet>> atom2sets = new HashMap<AlloyAtom, Set<AlloySet>>();

	@After
	public void reset()
	{
		types = new ArrayList<AlloyType>();
		map1 = new HashMap<AlloyAtom, ConstList<AlloySet>>();
		atoms = new ArrayList<AlloyAtom>();
		numOfTypes = new HashMap<AlloyType, Integer>();
		typeHier = new HashMap<AlloyType, AlloyType>();
		rel2tuples = new HashMap<AlloyRelation, Set<AlloyTuple>>();
		atom2sets = new HashMap<AlloyAtom, Set<AlloySet>>();		
	}
	
	@Test
	public void testForCompSink()
	{
		String commandName = "Check NoBadEntry for 5 but 3 Room, 3 Guest, 5 Time, 4 Event";
		String fileName = "";
		
		//----------------0----------1-----------2---------3-----------4--------5-------6-------7-------8---------9
		types = getTypes("Checkin", "Checkout", "Entry", "FrontDesk", "Guest", "Key", "Room", "Time", "ko/Ord", "to/Ord");
		//Abstract types.
		AlloyType event = new AlloyType("Event", false, true, false, false, false, false);
		AlloyType roomKeyEvent = new AlloyType("RoomKeyEvent", false, true, false, false, false, false);
		
		numOfTypes.put(types.get(0), 2);
		numOfTypes.put(types.get(1), 1);
		numOfTypes.put(types.get(2), 1);
		numOfTypes.put(types.get(3), 1);
		numOfTypes.put(types.get(4), 2);
		numOfTypes.put(types.get(5), 5);
		numOfTypes.put(types.get(6), 1);
		numOfTypes.put(types.get(7), 5);
		numOfTypes.put(types.get(8), 1);
		numOfTypes.put(types.get(9), 1);
		atoms = this.getAtoms(numOfTypes);
		
		for (AlloyAtom atom : atoms)
			atom2sets.put(atom, new HashSet<AlloySet>());
		
		Set<AlloySet> alloySets = new HashSet<AlloySet>();
		AlloySet set0 = new AlloySet("$NoBadEntry_e'", false, false, types.get(2));
		alloySets.add(set0);
		
		atom2sets.get(atoms.get(3)).add(set0);
		
		getRel2Tuples(
				getRelation("keys", false, false, types.get(6), types.get(5)),
				getTuple("Room_0", "Key_2"),
				getTuple("Room_0", "Key_3"),
				getTuple("Room_0", "Key_4"));
		getRel2Tuples(
				getRelation("currentKey", false, false, types.get(6), types.get(5), types.get(7)),
				getTuple("Room_0", "Key_2", "Time_0"),
				getTuple("Room_0", "Key_2", "Time_1"),
				getTuple("Room_0", "Key_2", "Time_2"),
				getTuple("Room_0", "Key_2", "Time_3"),
				getTuple("Room_0", "Key_3", "Time_4"));
		getRel2Tuples(
				getRelation("lastKey", false, false, types.get(3), types.get(6), types.get(5), types.get(7)),
				getTuple("FrontDesk_0", "Room_0", "Key_2", "Time_0"),
				getTuple("FrontDesk_0", "Room_0", "Key_3", "Time_1"),
				getTuple("FrontDesk_0", "Room_0", "Key_3", "Time_2"),
				getTuple("FrontDesk_0", "Room_0", "Key_4", "Time_3"),
				getTuple("FrontDesk_0", "Room_0", "Key_4", "Time_4"));
		getRel2Tuples(
				getRelation("occupant", false, false, types.get(3), types.get(6), types.get(4), types.get(7)),
				getTuple("FrontDesk_0", "Room_0", "Guest_0", "Time_1"),
				getTuple("FrontDesk_0", "Room_0", "Guest_1", "Time_3"),
				getTuple("FrontDesk_0", "Room_0", "Guest_1", "Time_4"));
		getRel2Tuples(
				getRelation("keys", false, false, types.get(4), types.get(5), types.get(7)),
				getTuple("Guest_0", "Key_3", "Time_1"),
				getTuple("Guest_0", "Key_3", "Time_2"),
				getTuple("Guest_0", "Key_3", "Time_3"),
				getTuple("Guest_0", "Key_3", "Time_4"),
				getTuple("Guest_1", "Key_4", "Time_3"),
				getTuple("Guest_1", "Key_4", "Time_4")
				);
		getRel2Tuples(
				getRelation("pre", false, false, event, types.get(7)),
				getTuple("Checkin_0", "Time_0"),
				getTuple("Checkin_1", "Time_2"),
				getTuple("Checkout_0", "Time_1"),
				getTuple("Entry_0", "Time_3")
				);
		getRel2Tuples(
				getRelation("post", false, false, event, types.get(7)),
				getTuple("Checkin_0", "Time_1"),
				getTuple("Checkin_1", "Time_3"),
				getTuple("Checkout_0", "Time_2"),
				getTuple("Entry_0", "Time_4")
				);
		getRel2Tuples(
				getRelation("guest", false, false, event, types.get(4)),
				getTuple("Checkin_0", "Guest_0"),
				getTuple("Checkin_1", "Guest_1"),
				getTuple("Checkout_0", "Guest_0"),
				getTuple("Entry_0", "Guest_0")
				);
		getRel2Tuples(
				getRelation("room", false, false, roomKeyEvent, types.get(6)),
				getTuple("Checkin_0", "Room_0"),
				getTuple("Checkin_1", "Room_0"),
				getTuple("Entry_0", "Room_0")
				);
		getRel2Tuples(
				getRelation("key", false, false, roomKeyEvent, types.get(5)),
				getTuple("Checkin_0", "Key_3"),
				getTuple("Checkin_1", "Key_4"),
				getTuple("Entry_0", "Key_3")
				);
		getRel2Tuples(
				getRelation("First", true, false, types.get(9), types.get(7)),
				getTuple("to/Ord_0", "Time_0")
				);
		getRel2Tuples(
				getRelation("Next", true, false, types.get(9), types.get(7), types.get(7)),
				getTuple("to/Ord_0", "Time_0", "Time_1"),
				getTuple("to/Ord_0", "Time_1", "Time_2"),
				getTuple("to/Ord_0", "Time_2", "Time_3"),
				getTuple("to/Ord_0", "Time_3", "Time_4")
				);
		getRel2Tuples(
				getRelation("First", true, false, types.get(8), types.get(5)),
				getTuple("ko/Ord_0", "Key_0")
				);
		getRel2Tuples(
				getRelation("Next", true, false, types.get(8), types.get(5), types.get(5)),
				getTuple("ko/Ord_0", "Key_0", "Key_1"),
				getTuple("ko/Ord_0", "Key_1", "Key_2"),
				getTuple("ko/Ord_0", "Key_2", "Key_3"),
				getTuple("ko/Ord_0", "Key_3", "Key_4")
				);
		getRel2Tuples(
				getRelation("$NoBadEntry_e", false, false, types.get(7), event),
				getTuple("Time_0", "Checkin_0"),
				getTuple("Time_1", "Checkout_0"),
				getTuple("Time_2", "Checkin_1"),
				getTuple("Time_3", "Entry_0")
				);
		
		Set<AlloyType> setTypes = new HashSet<AlloyType>();
		setTypes.addAll(types);
		setTypes.add(event);
		setTypes.add(roomKeyEvent);
		setTypes.add(AlloyType.INT);
		setTypes.add(AlloyType.STRING);
		setTypes.add(AlloyType.UNIV);
		setTypes.add(AlloyType.SEQINT);
		
		String hierarchy = "Checkin=RoomKeyEvent Checkout=Event Entry=RoomKeyEvent Event=univ "
		+ "FrontDesk=univ Guest=univ "
		+ "Int=univ "
		+ "Key=univ "
		+ "Room=univ "
		+ "RoomKeyEvent=Event "
		+ "String=univ "
		+ "Time=univ "
		+ "ko/Ord=univ "
		+ "seq/Int=Int "
		+ "to/Ord=univ";
		this.fillHierarchy(hierarchy, setTypes);
		
		AlloyModel model = new AlloyModel(setTypes, alloySets, rel2tuples.keySet(), typeHier);
		
		AlloyInstance instance = new AlloyInstance(null, fileName, commandName, model, atom2sets, rel2tuples, false);
		
		VizState view = new VizState(instance);
		Map<AlloyType, AlloyAtom> projectionMap = new HashMap<AlloyType, AlloyAtom>();
		//projectionMap.put(types.get(6), atoms.get(12));
		//projectionMap.put(types.get(7), atoms.get(13));
		AlloyProjection projection = new AlloyProjection(projectionMap);
		try {
			GraphViewer gv = StaticGraphMaker.produceGraph(instance, view, projection);
			projectionMap.put(types.get(6), atoms.get(12));
			projection = new AlloyProjection(projectionMap);
			gv = StaticGraphMaker.produceGraph(instance, view, projection);
			projectionMap.put(types.get(7), atoms.get(13));
			projection = new AlloyProjection(projectionMap);
			gv = StaticGraphMaker.produceGraph(instance, view, projection);
			java.util.List<GraphNode> graphNodes = gv.getGraphNodes();

			int k = 0;
			int i = k;
		} catch (ErrorFatal e) {
			fail("Could not produce graph");
		}
	}

	@Test
	public void testForCompType()
	{
		String commandName = "Check NoBadEntry for 5 but 3 Room, 3 Guest, 5 Time, 4 Event";
		String fileName = "";
		
		//----------------0----------1-----------2---------3-----------4--------5-------6-------7----
		types = getTypes("Checkin", "Checkout", "Entry", "FrontDesk", "Guest", "Key", "Room", "Time");
		//Abstract types.
		AlloyType event = new AlloyType("Event", false, true, false, false, false, false);
		AlloyType roomKeyEvent = new AlloyType("RoomKeyEvent", false, true, false, false, false, false);
		
		AlloyType koOrd = new AlloyType("ko/Ord", true, false, false, true, false, false);
		AlloyType toOrd = new AlloyType("to/Ord", true, false, false, true, false, false);
		types.add(koOrd);
		types.add(toOrd);
		
		numOfTypes.put(types.get(0), 2);
		numOfTypes.put(types.get(1), 1);
		numOfTypes.put(types.get(2), 1);
		numOfTypes.put(types.get(3), 1);
		numOfTypes.put(types.get(4), 2);
		numOfTypes.put(types.get(5), 5);
		numOfTypes.put(types.get(6), 1);
		numOfTypes.put(types.get(7), 5);
		numOfTypes.put(types.get(8), 1);
		numOfTypes.put(types.get(9), 1);
		atoms = this.getAtoms(numOfTypes);
		
		
		for (AlloyAtom atom : atoms)
			atom2sets.put(atom, new HashSet<AlloySet>());
		
		Set<AlloySet> alloySets = new HashSet<AlloySet>();
		AlloySet set0 = new AlloySet("$NoBadEntry_e'", false, false, types.get(2));
		alloySets.add(set0);
		
		atom2sets.get(atoms.get(3)).add(set0);
		
		getRel2Tuples(
				getRelation("keys", false, false, types.get(6), types.get(5)),
				getTuple("Room_0", "Key_2"),
				getTuple("Room_0", "Key_3"),
				getTuple("Room_0", "Key_4"));
		getRel2Tuples(
				getRelation("currentKey", false, false, types.get(6), types.get(5), types.get(7)),
				getTuple("Room_0", "Key_2", "Time_0"),
				getTuple("Room_0", "Key_2", "Time_1"),
				getTuple("Room_0", "Key_2", "Time_2"),
				getTuple("Room_0", "Key_2", "Time_3"),
				getTuple("Room_0", "Key_3", "Time_4"));
		getRel2Tuples(
				getRelation("lastKey", false, false, types.get(3), types.get(6), types.get(5), types.get(7)),
				getTuple("FrontDesk_0", "Room_0", "Key_2", "Time_0"),
				getTuple("FrontDesk_0", "Room_0", "Key_3", "Time_1"),
				getTuple("FrontDesk_0", "Room_0", "Key_3", "Time_2"),
				getTuple("FrontDesk_0", "Room_0", "Key_4", "Time_3"),
				getTuple("FrontDesk_0", "Room_0", "Key_4", "Time_4"));
		getRel2Tuples(
				getRelation("occupant", false, false, types.get(3), types.get(6), types.get(4), types.get(7)),
				getTuple("FrontDesk_0", "Room_0", "Guest_0", "Time_1"),
				getTuple("FrontDesk_0", "Room_0", "Guest_1", "Time_3"),
				getTuple("FrontDesk_0", "Room_0", "Guest_1", "Time_4"));
		getRel2Tuples(
				getRelation("keys", false, false, types.get(4), types.get(5), types.get(7)),
				getTuple("Guest_0", "Key_3", "Time_1"),
				getTuple("Guest_0", "Key_3", "Time_2"),
				getTuple("Guest_0", "Key_3", "Time_3"),
				getTuple("Guest_0", "Key_3", "Time_4"),
				getTuple("Guest_1", "Key_4", "Time_3"),
				getTuple("Guest_1", "Key_4", "Time_4")
				);
		getRel2Tuples(
				getRelation("pre", false, false, event, types.get(7)),
				getTuple("Checkin_0", "Time_0"),
				getTuple("Checkin_1", "Time_2"),
				getTuple("Checkout_0", "Time_1"),
				getTuple("Entry_0", "Time_3")
				);
		getRel2Tuples(
				getRelation("post", false, false, event, types.get(7)),
				getTuple("Checkin_0", "Time_1"),
				getTuple("Checkin_1", "Time_3"),
				getTuple("Checkout_0", "Time_2"),
				getTuple("Entry_0", "Time_4")
				);
		getRel2Tuples(
				getRelation("guest", false, false, event, types.get(4)),
				getTuple("Checkin_0", "Guest_0"),
				getTuple("Checkin_1", "Guest_1"),
				getTuple("Checkout_0", "Guest_0"),
				getTuple("Entry_0", "Guest_0")
				);
		getRel2Tuples(
				getRelation("room", false, false, roomKeyEvent, types.get(6)),
				getTuple("Checkin_0", "Room_0"),
				getTuple("Checkin_1", "Room_0"),
				getTuple("Entry_0", "Room_0")
				);
		getRel2Tuples(
				getRelation("key", false, false, roomKeyEvent, types.get(5)),
				getTuple("Checkin_0", "Key_3"),
				getTuple("Checkin_1", "Key_4"),
				getTuple("Entry_0", "Key_3")
				);
		getRel2Tuples(
				getRelation("First", true, false, types.get(9), types.get(7)),
				getTuple("to/Ord_0", "Time_0")
				);
		getRel2Tuples(
				getRelation("Next", true, false, types.get(9), types.get(7), types.get(7)),
				getTuple("to/Ord_0", "Time_0", "Time_1"),
				getTuple("to/Ord_0", "Time_1", "Time_2"),
				getTuple("to/Ord_0", "Time_2", "Time_3"),
				getTuple("to/Ord_0", "Time_3", "Time_4")
				);
		getRel2Tuples(
				getRelation("First", true, false, types.get(8), types.get(5)),
				getTuple("ko/Ord_0", "Key_0")
				);
		getRel2Tuples(
				getRelation("Next", true, false, types.get(8), types.get(5), types.get(5)),
				getTuple("ko/Ord_0", "Key_0", "Key_1"),
				getTuple("ko/Ord_0", "Key_1", "Key_2"),
				getTuple("ko/Ord_0", "Key_2", "Key_3"),
				getTuple("ko/Ord_0", "Key_3", "Key_4")
				);
		getRel2Tuples(
				getRelation("$NoBadEntry_e", false, false, types.get(7), event),
				getTuple("Time_0", "Checkin_0"),
				getTuple("Time_1", "Checkout_0"),
				getTuple("Time_2", "Checkin_1"),
				getTuple("Time_3", "Entry_0")
				);
		
		Set<AlloyType> setTypes = new HashSet<AlloyType>();
		setTypes.addAll(types);
		setTypes.add(event);
		setTypes.add(roomKeyEvent);
		setTypes.add(AlloyType.INT);
		setTypes.add(AlloyType.STRING);
		setTypes.add(AlloyType.UNIV);
		setTypes.add(AlloyType.SEQINT);
		
		String hierarchy = "Checkin=RoomKeyEvent Checkout=Event Entry=RoomKeyEvent Event=univ "
		+ "FrontDesk=univ Guest=univ "
		+ "Int=univ "
		+ "Key=univ "
		+ "Room=univ "
		+ "RoomKeyEvent=Event "
		+ "String=univ "
		+ "Time=univ "
		+ "ko/Ord=univ "
		+ "seq/Int=Int "
		+ "to/Ord=univ";
		this.fillHierarchy(hierarchy, setTypes);
		
		AlloyModel model = new AlloyModel(setTypes, alloySets, rel2tuples.keySet(), typeHier);
		
		AlloyInstance instance = new AlloyInstance(null, fileName, commandName, model, atom2sets, rel2tuples, false);
		
		VizState view = new VizState(instance);
		Map<AlloyType, AlloyAtom> projectionMap = new HashMap<AlloyType, AlloyAtom>();
		AlloyProjection projection = new AlloyProjection(projectionMap);
		try {
			GraphViewer gv = StaticGraphMaker.produceGraph(instance, view, projection);
			projectionMap.put(types.get(6), atoms.get(12));
			projection = new AlloyProjection(projectionMap);
			gv = StaticGraphMaker.produceGraph(instance, view, projection);
			projectionMap.put(types.get(5), atoms.get(7));
			projection = new AlloyProjection(projectionMap);
			gv = StaticGraphMaker.produceGraph(instance, view, projection);
			java.util.List<GraphNode> graphNodes = gv.getGraphNodes();

			int k = 0;
			int i = k;
		} catch (ErrorFatal e) {
			fail("Could not produce graph");
		}
	}
	
	private void fillHierarchy(String hierarchy, Set<AlloyType> setTypes)
	{
		Scanner sc = new Scanner(hierarchy);
		while(sc.hasNext())
		{
			String token = sc.next();
			int index = token.indexOf('=');
			String key = token.substring(0, index);
			String value = token.substring(index+1);
			AlloyType targetType = null;
			AlloyType targetValue = null;
			for (AlloyType type : setTypes)
			{
				if (type.getName().equals(key))
				{
					targetType = type;
				}
				else if (type.getName().equals(value))
				{
					targetValue = type;
				}
			}
			if (targetType!=null&&targetValue!=null)
				typeHier.put(targetType, targetValue);
		}
	}
	
	private AlloyTuple getTuple(String... atomsString)
	{
		ArrayList<AlloyAtom> tuple = new ArrayList<AlloyAtom>();
		for (String atom : atomsString)
		{
			String num = atom.substring(atom.indexOf("_")+1);
			String type = atom.substring(0, atom.indexOf("_"));
			int index = 0;
			for (AlloyType alType : types)
			{
				if (type.equals(alType.getName()))
				{
					Integer n = numOfTypes.get(alType);
					if (n==null||n==0) continue;
					index+=Integer.parseInt(num);
					break;
				}
				else
				{
					index+=numOfTypes.get(alType);
				}
			}
			tuple.add(atoms.get(index));
		}
		return new AlloyTuple(tuple);
	}
	
	private AlloyRelation getRelation(String relName, boolean isPrivate, boolean isMeta, AlloyType... types)
	{
		ArrayList<AlloyType> setTypes = new ArrayList<AlloyType>();
		for (AlloyType type : types)
			setTypes.add(type);
		return new AlloyRelation(relName, isPrivate, isMeta, setTypes);
	}
	
	private void getRel2Tuples(AlloyRelation relation, AlloyTuple... tuples)
	{
		Set<AlloyTuple> setTuples = new HashSet<AlloyTuple>();
		for (AlloyTuple tuple : tuples)
			setTuples.add(tuple);
		rel2tuples.put(relation, setTuples);	
	}
	
	private ArrayList<AlloyAtom> getAtoms(Map<AlloyType, Integer> numAtoms)
	{
		ArrayList<AlloyAtom> atoms = new ArrayList<AlloyAtom>();
		int count = 0;
		for (AlloyType type : types)
		{
			Integer numberOfAtoms = numAtoms.get(type);
			if (numberOfAtoms==null||numberOfAtoms==0) continue;
			for (int i = 0;i<numberOfAtoms;i++)
			{
				atoms.add(new AlloyAtom(type, i, type.getName()+i));
				count++;
			}
		}
				
		return atoms;
	}
	
	private ArrayList<AlloyType> getTypes(String... list)
	{
		ArrayList<AlloyType> types = new ArrayList<AlloyType>();
		for (String typeName : list)
		{
			AlloyType type = new AlloyType(typeName, false, false, false, false, false, false);
			types.add(type);
		}
		return types;
	}
	
	private AlloyInstance instanceGen(ArrayList<AlloyAtom> atoms, Map<AlloyRelation, Set<AlloyTuple>> relMap, Map<AlloyType, AlloyType> hierarchy)
	{
		Set<String> stringAtoms = new HashSet<String>();
		Collection<AlloyType> types = new ArrayList<AlloyType>();
		Map<AlloyAtom,Set<AlloySet>> atom2sets = new HashMap<AlloyAtom, Set<AlloySet>>();
		for (AlloyAtom atom : atoms)
		{
			stringAtoms.add(atom.toString());
			AlloyType type = atom.getType();
			
			Set<AlloySet> set = new HashSet<AlloySet>();
			String setName = atom.getVizName(null, true);
			set.add(new AlloySet(setName, false, false, atom.getType()));
			atom2sets.put(atom, set);
			if (!types.contains(type))
			{
				types.add(type);
			}
		}

		Collection<AlloySet> sets = new ArrayList<AlloySet>();
		Collection<AlloyRelation> relations = relMap.keySet();
		AlloyModel model = new AlloyModel(types, sets, relations, hierarchy);
		
		
		AlloyInstance instance = new AlloyInstance(null, "", "", model, atom2sets, relMap, false);
		return instance;
	}
	
	
}
