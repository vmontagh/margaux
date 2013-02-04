package junit.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.mit.csail.sdg.alloy4viz.AlloyElement;
import edu.mit.csail.sdg.alloy4viz.AlloyRelation;
import edu.mit.csail.sdg.alloy4viz.AlloySet;
import edu.mit.csail.sdg.alloy4viz.AlloyType;
import edu.mit.csail.sdg.alloy4viz.MagicUtil;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.alloy4viz.VizState;
import edu.mit.csail.sdg.alloy4graph.DotColor;
import edu.mit.csail.sdg.alloy4graph.DotShape;
import edu.mit.csail.sdg.alloy4graph.DotStyle;

public class doDistanceMetric {
	private static boolean success = false;
	private static List<String> Diffs;
	public static Map<String, List<String>> DiffsList = new TreeMap<String, List<String>>(); 
	
	public static boolean Compute(VizGUI ML, VizGUI Expert){
		try{
		Diffs = new ArrayList<String>();
		checkProjection(ML, Expert);
		checkNodeVisibility(ML, Expert);
		checkSpine(ML, Expert);
		checkAttribute(ML, Expert);
		checkEdgelabels(ML, Expert);
		
		checkShape(ML, Expert);
		checkColour(ML, Expert);
		checkNodeStyle(ML, Expert);
		
		System.out.println(Diffs);
		
		String[] name = ML.getXMLfilename().split("/");
		DiffsList.put(name[name.length-1].replace(".xml", ""), Diffs);
		
		success = true;
		return success;
		} catch (Exception e){ System.out.println(e); return success;}
	}
	
	private static void checkProjection(VizGUI ML, VizGUI Expert){
		SetComputation sets = new SetComputation();
		if(ML.getVizState().getProjectedTypes().equals(Expert.getVizState().getProjectedTypes())){}
		else{
			if(sets.isNonEmptySubset(ML.getVizState().getProjectedTypes(), Expert.getVizState().getProjectedTypes())){
				Diffs.add("Projection(Subset)");
			}else if(sets.isIntersection(ML.getVizState().getProjectedTypes(), Expert.getVizState().getProjectedTypes())){
				Diffs.add("Projection(Intersection)");
			}else{
				Diffs.add("Projection");
			}
		}
 	}
	
	private static void checkNodeVisibility(VizGUI ML, VizGUI Expert){
		SetComputation sets = new SetComputation();
		Set<AlloyType> ML_visible = computeVisible(ML.getVizState());
		Set<AlloyType> Expert_visible = computeVisible(Expert.getVizState());
		
		if(ML_visible.equals(Expert_visible)){}
		else{
			if(sets.isNonEmptySubset(ML_visible, Expert_visible)){
				Diffs.add("NodeVisibility(Subset)");
			}else if(sets.isIntersection(ML_visible, Expert_visible)){
				Diffs.add("NodeVisibility(Intersection)");
			}else{
				Diffs.add("NodeVisibility");
			}
		}
	}
	
	/** This feature is not tested for set computations because chances of them happenening atleast for sample models is provably none*/
	private static void checkSpine(VizGUI ML, VizGUI Expert){
		if(!(ML.getVizState().constraint.isEmpty()) || !(Expert.getVizState().constraint.isEmpty() 
				|| !(ML.getVizState().layoutBack.isEmpty()) || !(Expert.getVizState().layoutBack.isEmpty()))){
			if(ML.getVizState().constraint.equals(Expert.getVizState().constraint) ||
				ML.getVizState().layoutBack.equals(Expert.getVizState().layoutBack)	){}
			else{Diffs.add("Spine");}
		}
	}
	
	private static void checkAttribute(VizGUI ML, VizGUI Expert){
		SetComputation sets = new SetComputation();
		if(!(ML.getVizState().attribute.isEmpty()) || !(Expert.getVizState().attribute.isEmpty())){	
			if(ML.getVizState().attribute.equals(Expert.getVizState().attribute)){}
			else{
				if(sets.isNonEmptySubset(ML.getVizState().attribute.entryset(), Expert.getVizState().attribute.entryset())){
					Diffs.add("Attribute(Subset)");
				}else if(sets.isIntersection(ML.getVizState().attribute.entryset(), Expert.getVizState().attribute.entryset())){
					Diffs.add("Attribute(Intersection)");
				}else{
					Diffs.add("Attribute");
				}
			}
		}
	}
	
	private static void checkEdgelabels(VizGUI ML, VizGUI Expert){
		SetComputation sets = new SetComputation();
		if(!(ML.getVizState().label.isEmpty()) || !(Expert.getVizState().label.isEmpty())){
			Map<AlloyRelation, String> ML_labels = new TreeMap<AlloyRelation, String>();
			Map<AlloyRelation, String> Expert_labels = new TreeMap<AlloyRelation, String>();
			
			for(AlloyRelation r: ML.getVizState().getCurrentModel().getRelations()){
				ML_labels.put(r, ML.getVizState().label.get(r));
			}
			for(AlloyRelation r: Expert.getVizState().getCurrentModel().getRelations()){
				Expert_labels.put(r, Expert.getVizState().label.get(r));
			}
			if(ML_labels.equals(Expert_labels)){}
			else{
				if(sets.isNonEmptySubset(ML_labels.entrySet(), Expert_labels.entrySet())){
					Diffs.add("Edge Labels/Node Names(Subset)");
				}else if(sets.isIntersection(ML_labels.entrySet(), Expert_labels.entrySet())){
					Diffs.add("Edge Labels/Node Names(Intersection)");
				}else{
					Diffs.add("Edge Labels/Node Names");
				}
			}			
		}
	}
	
	private static void checkShape(VizGUI ML, VizGUI Expert){
		if(!(ML.getVizState().shape.isEmpty()) || !(Expert.getVizState().shape.isEmpty())){
			Map<AlloyElement,String> ML_Shape = new TreeMap<AlloyElement, String>();
			Map<AlloyElement,String> Expert_Shape = new TreeMap<AlloyElement, String>();
			
			for(AlloyType t: ML.getVizState().getCurrentModel().getTypes()){
				if(MagicUtil.isActuallyVisible(ML.getVizState(), t) && !t.getName().contains("/Ord") && !t.isBuiltin){
					if(ML.getVizState().shape.get(t)==null){ML_Shape.put(t, "Inherit");}
					else{ML_Shape.put(t, ML.getVizState().shape.get(t).toString());}
				}
			}
			if(!ML.getVizState().getCurrentModel().getSets().isEmpty()){
				for(AlloySet t: ML.getVizState().getCurrentModel().getSets()){
					if(!t.getType().getName().contains("/Ord") && !t.getType().isBuiltin ){
						if(ML.getVizState().shape.get(t)==null){ML_Shape.put(t, "Inherit");}
						else{ML_Shape.put(t, ML.getVizState().shape.get(t).toString());}
					}
				}
			}
			
			for(AlloyType t: Expert.getVizState().getCurrentModel().getTypes()){
				if(MagicUtil.isActuallyVisible(Expert.getVizState(), t) && !t.getName().contains("/Ord") && !t.isBuiltin){
					if(Expert.getVizState().shape.get(t)==null){Expert_Shape.put(t, "Inherit");}
					else{Expert_Shape.put(t, Expert.getVizState().shape.get(t).toString());}
				}
			}
			if(!Expert.getVizState().getCurrentModel().getSets().isEmpty()){
				for(AlloySet t: Expert.getVizState().getCurrentModel().getSets()){
					if(!t.getType().getName().contains("/Ord") && !t.getType().isBuiltin ){
						if(Expert.getVizState().shape.get(t)==null){Expert_Shape.put(t, "Inherit");}
						else{Expert_Shape.put(t, Expert.getVizState().shape.get(t).toString());}
					}
				}
			}
			
//			System.out.println("Expert: "+Expert_Shape);
//			System.out.println("ML: "+ML_Shape);
			
			Set<String> ML_Set = new HashSet<String>();
			Set<String> Expert_Set = new HashSet<String>();

			
			for(String s: ML_Shape.values()){
				ML_Set.add(s);
			}
			for(String s: Expert_Shape.values()){
				Expert_Set.add(s);
			}
			
//			if(ML_Set.size()!=Expert_Set.size()){
//				Diffs.add("Shape");
//			}
			if(ML_Shape.size()!=Expert_Shape.size()){Diffs.add("Shape");}
			else{	
				boolean Jury = true;
				System.out.println(ML_Shape);
				for(String c: ML_Set){
					Set<AlloyElement> temp = new TreeSet<AlloyElement>();
					for(AlloyElement e: ML_Shape.keySet()){
							if(ML_Shape.get(e).equals(c)){
								temp.add(e);
							}	
					}
					if(expertHasAllElements(temp, Expert_Shape.keySet())){
						boolean firstmatch = true;
						String thisShape = DotShape.CIRCLE.toString();
						for(AlloyElement exp_e: Expert_Shape.keySet()){
							for(AlloyElement ml_e: temp){
								if(exp_e.equals(ml_e)){
									if(firstmatch){
											thisShape = Expert_Shape.get(exp_e); firstmatch = false;											
									}
									else{
										if(!Expert_Shape.get(exp_e).equals(thisShape)){
											System.out.println("Kaboum!!"+exp_e+":"+Expert_Shape.get(exp_e)+":"+thisShape);
											Jury = false;
										}
									}
								}
							}
						}
					}
					else Jury = false;

					
				}
				if(!Jury)Diffs.add("Shape");
			}	
		}
	}
	
	/** Checks if colours are equal
	 * - Checks if same size
	 * - if not same size, label as different
	 * - if same size, then checks if atleast the right nodes are coloured for ML and Expert,
	 * 	 although the specific colours could be different.*/
	
	private static void checkColour(VizGUI ML, VizGUI Expert){		
		if(!(ML.getVizState().nodeColor.isEmpty()) || !(Expert.getVizState().nodeColor.isEmpty())){
				
			Map<AlloyElement,String> ML_Colour = new TreeMap<AlloyElement, String>();
			Map<AlloyElement,String> Expert_Colour = new TreeMap<AlloyElement, String>();
			
			
			for(AlloyType t: ML.getVizState().getCurrentModel().getTypes()){
				if(MagicUtil.isActuallyVisible(ML.getVizState(), t) && !t.getName().contains("/Ord") && !t.isBuiltin){
					if(ML.getVizState().nodeColor.get(t)==null){ML_Colour.put(t, "Inherit");}
					else{ML_Colour.put(t, ML.getVizState().nodeColor.get(t).toString());}
				}
			}
			
//			if(!ML.getVizState().getCurrentModel().getSets().isEmpty()){
//				for(AlloySet t: ML.getVizState().getCurrentModel().getSets()){
//					if(!t.getType().getName().contains("/Ord") && !t.getType().isBuiltin ){
//						if(ML.getVizState().nodeColor.get(t)==null){ML_Colour.put(t, "Inherit");}
//						else{ML_Colour.put(t, ML.getVizState().nodeColor.get(t).toString());}
//					}
//				}
//			}
						
			for(AlloyType t: Expert.getVizState().getCurrentModel().getTypes()){
				if(MagicUtil.isActuallyVisible(Expert.getVizState(), t) && !t.getName().contains("/Ord") && !t.isBuiltin){
					if(Expert.getVizState().nodeColor.get(t)==null){Expert_Colour.put(t, "Inherit");}
					else{Expert_Colour.put(t, Expert.getVizState().nodeColor.get(t).toString());}
				}
			}
			
//			if(!Expert.getVizState().getCurrentModel().getSets().isEmpty()){
//				for(AlloySet t: Expert.getVizState().getCurrentModel().getSets()){
//					if(!t.getType().getName().contains("/Ord") && !t.getType().isBuiltin ){
//						if(Expert.getVizState().nodeColor.get(t)==null){Expert_Colour.put(t, "Inherit");}
//						else{Expert_Colour.put(t, Expert.getVizState().nodeColor.get(t).toString());}
//					}
//				}
//			}
			
			Set<String> ML_Set = new HashSet<String>();
			Set<String> Expert_Set = new HashSet<String>();
			
			for(String c: ML_Colour.values()){
				ML_Set.add(c);
			}
			for(String c: Expert_Colour.values()){
				Expert_Set.add(c);
			}
			
//			System.out.println("ExpertNODE:"+Expert.getVizState().nodeColor);
//			System.out.println("MLNODE:"+ML.getVizState().nodeColor);
//			
			System.out.println("Expert: "+Expert_Colour);
			System.out.println("ML: "+ML_Colour);
			
			
			
			if(ML_Set.size()!=Expert_Set.size()){Diffs.add("Node Color");}
			else{	
				boolean Jury = true;
				for(String c: ML_Set){
					Set<AlloyElement> temp = new TreeSet<AlloyElement>();
					for(AlloyElement e: ML_Colour.keySet()){
						if(c == null && ML_Colour.get(e) == null){temp.add(e);}
						else{
							if(ML_Colour.get(e).equals(c)){
								temp.add(e);
							}
						}
					}
					if(expertHasAllElements(temp, Expert_Colour.keySet())){
						boolean firstmatch = true;
						String thisColor = DotColor.nodeDefault().toString();
						for(AlloyElement exp_e: Expert_Colour.keySet()){
							for(AlloyElement ml_e: temp){
								if(exp_e.equals(ml_e)){
									if(firstmatch){
											thisColor = Expert_Colour.get(exp_e); firstmatch = false;											
									}
									else{
										
										if(!Expert_Colour.get(exp_e).equals(thisColor)){
											System.out.println("Kaboum!!"+exp_e+":"+Expert_Colour.get(exp_e)+":"+thisColor);
											Jury = false;
										}
									}
								}
							}
						}
					}
					else Jury = false;

					
				}
				if(!Jury)Diffs.add("Node Color");
			}
		}
	}
	
	private static boolean expertHasAllElements(Set<AlloyElement> temp, Set<AlloyElement> refKeySet ){
		for(AlloyElement e: temp){
			if(!refKeySet.contains(e)) return false;
		}
		return true;
	}
	
	private static void checkNodeStyle(VizGUI ML, VizGUI Expert){
		if(!(ML.getVizState().nodeStyle.isEmpty()) || !(Expert.getVizState().nodeStyle.isEmpty())){
			
			Map<AlloyElement, DotStyle> ML_Style = new LinkedHashMap<AlloyElement, DotStyle>();
			Map<AlloyElement, DotStyle> Expert_Style = new LinkedHashMap<AlloyElement, DotStyle>();
			
			for(AlloyType t: ML.getVizState().getCurrentModel().getTypes()){
				if(MagicUtil.isActuallyVisible(ML.getVizState(), t) && !t.getName().contains("/Ord") && !t.isBuiltin){
					ML_Style.put(t, ML.getVizState().nodeStyle.get(t));
				}
			}
			
			if(!ML.getVizState().getCurrentModel().getSets().isEmpty()){
				for(AlloySet t: ML.getVizState().getCurrentModel().getSets()){
					if(!t.getType().getName().contains("/Ord") && !t.getType().isBuiltin ){
					ML_Style.put(t, ML.getVizState().nodeStyle.get(t));}
				}
			}
			
			for(AlloyType t: Expert.getVizState().getCurrentModel().getTypes()){
				if(MagicUtil.isActuallyVisible(Expert.getVizState(), t) && !t.getName().contains("/Ord") && !t.isBuiltin){
					Expert_Style.put(t,Expert.getVizState().nodeStyle.get(t));
				}
			}
			
			if(!Expert.getVizState().getCurrentModel().getSets().isEmpty()){
				for(AlloySet t: Expert.getVizState().getCurrentModel().getSets()){
					if(!t.getType().getName().contains("/Ord") && !t.getType().isBuiltin ){
					Expert_Style.put(t, Expert.getVizState().nodeStyle.get(t));}
				}
			}
			
			if(!ML_Style.equals(Expert_Style)){
//				String mode = checkSet(ML_Style, Expert_Style);
				
				Diffs.add("Node Style");
			}
		}
	}
	
	private static <K,V> String checkSet(Map<K, V> ml_Style,
			Map<K, V> expert_Style) {
		
			final Set<Map.Entry<K,V>> ml_Eset = ml_Style.entrySet();
			final Set<Map.Entry<K,V>> expert_Eset = expert_Style.entrySet();
		
		SetComputation x = new SetComputation();
		
		if(x.isNonEmptySubset(ml_Eset, expert_Eset)){
			return "Subset";
		}else if(x.isIntersection(ml_Eset, expert_Eset)){
			return "isIntersect";
		}else{
			return "noIntersect";
		}
	}

	private static Set<AlloyType> computeVisible(VizState model){
		Set<AlloyType> visibles = new TreeSet<AlloyType>();
		for(AlloyType t: model.getOriginalModel().getTypes()){
			if((MagicUtil.isActuallyVisible(model, t) )&& !t.getName().contains("/Ord") && !t.isBuiltin) visibles.add(t); 
		}
		return visibles;
	}
}
	

