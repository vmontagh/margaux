/* Alloy Analyzer 4 -- Copyright (c) 2006-2009, Felix Chang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.mit.csail.sdg.alloy4viz;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JPanel;
import edu.mit.csail.sdg.alloy4.ErrorFatal;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4graph.DotColor;
import edu.mit.csail.sdg.alloy4graph.DotDirection;
import edu.mit.csail.sdg.alloy4graph.DotPalette;
import edu.mit.csail.sdg.alloy4graph.DotShape;
import edu.mit.csail.sdg.alloy4graph.DotStyle;
import edu.mit.csail.sdg.alloy4graph.GraphEdge;
import edu.mit.csail.sdg.alloy4graph.Graph;
import edu.mit.csail.sdg.alloy4graph.GraphNode;
import edu.mit.csail.sdg.alloy4graph.GraphViewer;

/** This utility class generates a graph for a particular index of the projection.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public final class StaticGraphMaker {

   /** The theme customization. */
   private final VizState view;

   /** The projected instance for the graph currently being generated. */
   private final AlloyInstance instance;

   /** The projected model for the graph currently being generated. */
   private final AlloyModel model;

   /** The map that contains all edges and what the AlloyTuple that each edge corresponds to. */
   private final Map<GraphEdge,AlloyTuple> edges = new LinkedHashMap<GraphEdge,AlloyTuple>();

   /** The map that contains all nodes and what the AlloyAtom that each node corresponds to. */
   private final Map<GraphNode,AlloyAtom> nodes = new LinkedHashMap<GraphNode,AlloyAtom>();

   /** This maps each atom to the node representing it; if an atom doesn't have a node, it won't be in the map. */
   private final Map<AlloyAtom,GraphNode> atom2node = new LinkedHashMap<AlloyAtom,GraphNode>();

   /** This stores a set of additional labels we want to add to an existing node. */
   private final Map<GraphNode,Set<String>> attribs = new LinkedHashMap<GraphNode,Set<String>>();

   /** The resulting graph. */
   private final Graph graph;
      
   /** The list of node positions for new frame */
   private List<GraphNode> oldGraphNodes = new ArrayList<GraphNode>();
   
   /** Contains a list of lists of all labels associated with the atom through the projection. */
   private static Map<AlloyAtom, List<List<String>>> atomLabels = new HashMap<AlloyAtom, List<List<String>>>();
   
   private static enum LayoutScheme {compSink, compType, repSink, repType}
   
   /** Produces a single Graph from the given Instance and View and choice of Projection.*/
   public static GraphViewer produceGraph(AlloyInstance instance, VizState view, AlloyProjection proj) throws ErrorFatal {
         resetProjectionAnalysis();
	     if (proj.getProjectedTypes().size()==0)
         {
        	 return produceRepGraph(instance, view, proj, Graph.LayoutStrat.BySink);
         }
		 Map<AlloyType, AlloyAtom> m = generateMap(proj);
	     AnalyzeProjection(instance, view, m, proj.getProjectedTypes(), 0, Graph.LayoutStrat.BySink);
	     GraphViewer comp = produceCompositeGraph(instance, new VizState(view), proj, Graph.LayoutStrat.BySink);
	     GraphViewer compType = produceCompositeGraph(instance, new VizState(view), proj, Graph.LayoutStrat.ByType);
	     GraphViewer rep = produceRepGraph(instance, new VizState(view), proj, Graph.LayoutStrat.BySink);
	     GraphViewer repType = produceRepGraph(instance, new VizState(view), proj, Graph.LayoutStrat.ByType);
         int compRead = calculateReadability(instance, new VizState(view), m, proj.getProjectedTypes(), 0, comp);
         int compTypeRead = calculateReadability(instance, new VizState(view), m, proj.getProjectedTypes(), 0, compType);
         int repRead = calculateReadability(instance, new VizState(view), m, proj.getProjectedTypes(), 0, rep);
         int repTypeRead = calculateReadability(instance, new VizState(view), m, proj.getProjectedTypes(), 0, repType);
         Map<StaticGraphMaker.LayoutScheme, Integer> readMap = new EnumMap<StaticGraphMaker.LayoutScheme, Integer>(StaticGraphMaker.LayoutScheme.class);
         readMap.put(StaticGraphMaker.LayoutScheme.compSink, compRead);
         readMap.put(StaticGraphMaker.LayoutScheme.compType, compTypeRead);
         readMap.put(StaticGraphMaker.LayoutScheme.repSink, repRead);
         readMap.put(StaticGraphMaker.LayoutScheme.repType, repTypeRead);
         StaticGraphMaker.LayoutScheme best = getLowestReadScore(readMap);
         if (best == StaticGraphMaker.LayoutScheme.compSink)
         {
        	 return produceCompositeGraph(instance, view, proj, Graph.LayoutStrat.BySink);
         }
         else if (best == StaticGraphMaker.LayoutScheme.compType)
         {
        	 return produceCompositeGraph(instance, view, proj, Graph.LayoutStrat.ByType);
         }
         else if (best == StaticGraphMaker.LayoutScheme.repSink)
         {
        	 return produceRepGraph(instance, view, proj, Graph.LayoutStrat.BySink);
         }
         else //if (best == StaticGraphMaker.layoutScheme.repType)
         {
        	 return produceRepGraph(instance, view, proj, Graph.LayoutStrat.ByType);
         }
   }
   
   private static GraphViewer produceRepGraph(AlloyInstance instance, VizState view, AlloyProjection proj, Graph.LayoutStrat strat) throws ErrorFatal
   {
	    if (proj == null) proj = new AlloyProjection();
	    if (proj.getProjectedTypes().size()==0)
	    {
	    	Graph tempGraph = new Graph(1, strat);
	    	new StaticGraphMaker(tempGraph, instance, view, instance, null);
	    	return new GraphViewer(tempGraph);
	    }
	    Graph graph = getNewGraph(view);
	    AlloyInstance projInstance = StaticProjector.project(instance, proj, true);
	    Graph tempGraph = new Graph(1, strat);
	    Map<AlloyType, AlloyAtom> m = generateMap(proj);
	    GraphViewer gv = getMaxReadability(instance, view, m, proj.getProjectedTypes(), 0, -1, strat, null);
	    return produceFrame(instance, view, proj, gv);
   }
   
   private static GraphViewer produceCompositeGraph(AlloyInstance instance, VizState view, AlloyProjection proj, Graph.LayoutStrat strat) throws ErrorFatal
   {
	   if (proj == null) proj = new AlloyProjection();
	      Graph graph = getNewGraph(view);
	      AlloyInstance projInstance = StaticProjector.project(instance, proj, false);
	      Graph tempGraph = new Graph(1, strat);
	      new StaticGraphMaker(tempGraph, instance, view, projInstance, null);
	      GraphViewer temp = new GraphViewer(tempGraph);
	      if (graph.nodes.size()==0) new GraphNode(graph, "", null, "Due to your theme settings, every atom is hidden.", "Please click Theme and adjust your settings.");
	      return produceFrame(instance, view, proj, temp);
   }
   
   private static void resetProjectionAnalysis()
   {
	   atomLabels.clear();
   }
   
   private static void AnalyzeProjection(AlloyInstance instance, VizState view, Map<AlloyType, AlloyAtom> map, Collection<AlloyType> types, int recNum, Graph.LayoutStrat strat) throws ErrorFatal
   {
	   AlloyType type = types.toArray(new AlloyType[1])[recNum];
	   List<AlloyAtom> atoms = instance.type2atoms(type);
	   for (int i = 0;i<atoms.size();i++)
	   {
		   map.remove(type);
		   map.put(type, atoms.get(i));
		   if (recNum<types.size()-1)
		   {
			   AnalyzeProjection(instance, view, map, types, recNum+1, strat);
		   }
		   else
		   {
			   Graph g = new Graph(1, strat);
			   AlloyInstance projInst = StaticProjector.project(instance, new AlloyProjection(map), true);
			   new StaticGraphMaker(g, instance, view, projInst, null);
			   GraphViewer graph = new GraphViewer(g);
			   for (GraphNode node : graph.getGraphNodes())
			   {
				   if (!atomLabels.keySet().contains(node.getAtom()))
				   {
					   atomLabels.put(node.getAtom(), new ArrayList<List<String>>());
				   }
				   List<List<String>> listOfLists = atomLabels.get(node.getAtom());
				   List<String> newList = new ArrayList<String>();
				   
				   if (node.getLabels()!=null)for (int j = 0;j<node.getLabels().size();j++)
				   {
					   String label = node.getLabels().get(j);
					   newList.add(label);
				   }
				   listOfLists.add(newList);
				   atomLabels.remove(node.getAtom());
				   atomLabels.put(node.getAtom(), listOfLists);
			   }
		   }
	   }
   }
   
   private static GraphViewer getMaxReadability(AlloyInstance instance, VizState view, Map<AlloyType, AlloyAtom> map, Collection<AlloyType> types, int recNum, int max, Graph.LayoutStrat strat, GraphViewer gv) throws ErrorFatal
   {
	   int readability = 0;
	   AlloyType type = types.toArray(new AlloyType[1])[recNum];
	   List<AlloyAtom> atoms = instance.type2atoms(type);
	   int numOfTypes = types.size();
	   GraphViewer graphViewer = gv;
	   for (int i = 0;i<atoms.size();i++)
	   {
		   map.remove(type);
		   map.put(type, atoms.get(i));
		   if (recNum<types.size()-1)
		   {
			   GraphViewer graph = getMaxReadability(instance, view, map, types, recNum+1, max, strat, gv);
			   int maximum = graph.calculateEdgeCrossings();
			   return graph;
		   }
		   else
		   {
			   Graph g = new Graph(1, strat);
			   AlloyInstance projInst = StaticProjector.project(instance, new AlloyProjection(map), true);
			   new StaticGraphMaker(g, instance, view, projInst, null);
			   GraphViewer graph = new GraphViewer(g);
			   if (max==-1||max>(graph.calculateEdgeCrossings()^2))
			   {
				   graphViewer = graph;
				   max=graph.calculateEdgeCrossings();
			   }
		   }
	   }
	   return graphViewer;
   }
   
   private static int calculateReadability(AlloyInstance instance, VizState view, Map<AlloyType, AlloyAtom> map, Collection<AlloyType> types, int recNum, GraphViewer gv) throws ErrorFatal
   {
	   int readability = 0;
	   AlloyType type = types.toArray(new AlloyType[1])[recNum];
	   List<AlloyAtom> atoms = instance.type2atoms(type);
	   int numOfTypes = types.size();
	   for (int i = 0;i<atoms.size();i++)
	   {
		   map.remove(type);
		   map.put(type, atoms.get(i));
		   if (recNum<types.size()-1)
		   {
			   readability += calculateReadability(instance, view, map, types, recNum+1, gv)^2;
		   }
		   else
		   {
			  int crossingsScore = produceFrame(instance, view, new AlloyProjection(map), gv).calculateEdgeCrossings();
			  readability += crossingsScore;
		   }
	   }
	   return readability;
   }
   
   private static Map<AlloyType, AlloyAtom> generateMap(AlloyProjection proj)
   {
	   Map<AlloyType, AlloyAtom> map = new HashMap<AlloyType, AlloyAtom>();
	   Collection<AlloyType> types = proj.getProjectedTypes();
	   for (AlloyType t : types)
	   {
		   map.put(t, null);
	   }
	   return map;
   }
   
   private static StaticGraphMaker.LayoutScheme getLowestReadScore(Map<StaticGraphMaker.LayoutScheme, Integer> map)
   {
	   Set<StaticGraphMaker.LayoutScheme> gvs = map.keySet();
	   StaticGraphMaker.LayoutScheme [] gvsArr = gvs.toArray(new StaticGraphMaker.LayoutScheme[1]);
	   int min = -1;
	   StaticGraphMaker.LayoutScheme layScheme = null;
	   for (int i = 0;i<gvs.size();i++)
	   {
		   if (min==-1 || map.get(gvsArr[i])<min)
		   {
			   layScheme = gvsArr[i];
			   min = map.get(gvsArr[i]);
		   }
	   }
	   return layScheme;
   }
   
   /** Produces a another frame of the projection based on the given GraphViewer. */
   public static GraphViewer produceFrame(AlloyInstance instance, VizState view, AlloyProjection proj, GraphViewer gv) throws ErrorFatal 
   {
	   if (proj==null) proj = new AlloyProjection();
	   Graph graph = getNewGraph(view);
	   AlloyInstance projInstance = StaticProjector.project(instance, proj, true);
	   new StaticGraphMaker(graph, instance, view, projInstance, gv);
	   if (graph.nodes.size()==0) new GraphNode(graph, "", null, "Due to your theme settings, every atom is hidden.", "Please click Theme and adjust your settings.");
	   return new GraphViewer(graph, true, gv.getLeftMostPos(), gv.getTopMostPos());
   }
   
   private static Graph getNewGraph(VizState view)
   {
	  view = new VizState(view);
	  return new Graph(view.getFontSize() / 12.0D, Graph.LayoutStrat.ByType);
   }
   
   /** The list of colors, in order, to assign each legend. */
   private static final List<Color> colorsClassic = Util.asList(
         new Color(228,26,28)
         ,new Color(166,86,40)
         ,new Color(255,127,0)
         ,new Color(77,175,74)
         ,new Color(55,126,184)
         ,new Color(152,78,163)
   );

   /** The list of colors, in order, to assign each legend. */
   private static final List<Color> colorsStandard = Util.asList(
         new Color(227,26,28)
         ,new Color(255,127,0)
         ,new Color(251*8/10,154*8/10,153*8/10)
         ,new Color(51,160,44)
         ,new Color(31,120,180)
   );

   /** The list of colors, in order, to assign each legend. */
   private static final List<Color> colorsMartha = Util.asList(
         new Color(231,138,195)
         ,new Color(252,141,98)
         ,new Color(166,216,84)
         ,new Color(102,194,165)
         ,new Color(141,160,203)
   );

   /** The list of colors, in order, to assign each legend. */
   private static final List<Color> colorsNeon = Util.asList(
         new Color(231,41,138)
         ,new Color(217,95,2)
         ,new Color(166,118,29)
         ,new Color(102,166,30)
         ,new Color(27,158,119)
         ,new Color(117,112,179)
   );
   
   /** The constructor takes an Instance and a View, then insert the generate graph(s) into a blank cartoon. */
   private StaticGraphMaker (Graph graph, AlloyInstance originalInstance, VizState view, AlloyInstance projectedInstance, GraphViewer gv) throws ErrorFatal {
      //If this is just another frame of the projection, then get the old graph nodes.
	  if (gv!=null)
      {
    	  oldGraphNodes = gv.getGraphNodes();
      }
      final boolean hidePrivate = view.hidePrivate();
      final boolean hideMeta = view.hideMeta();
      final Map<AlloyRelation,Color> magicColor = new TreeMap<AlloyRelation,Color>();
      final Map<AlloyRelation,Integer> rels = new TreeMap<AlloyRelation,Integer>();
      this.graph = graph;
      this.view = view;
      instance = projectedInstance;	
      model = instance.model;
      for (AlloyRelation rel: model.getRelations()) {
         rels.put(rel, null);
      }
      List<Color> colors;
      if (view.getEdgePalette() == DotPalette.CLASSIC) colors = colorsClassic;
      else if (view.getEdgePalette() == DotPalette.STANDARD) colors = colorsStandard;
      else if (view.getEdgePalette() == DotPalette.MARTHA) colors = colorsMartha;
      else colors = colorsNeon;
      int ci = 0;
      for (AlloyRelation rel: model.getRelations()) {
         DotColor c = view.edgeColor.resolve(rel);
         Color cc = (c==DotColor.MAGIC) ? colors.get(ci) : c.getColor(view.getEdgePalette());
         int count = ((hidePrivate && rel.isPrivate) || !view.edgeVisible.resolve(rel)) ? 0 : edgesAsArcs(hidePrivate, hideMeta, rel, colors.get(ci));
         rels.put(rel, count);
         magicColor.put(rel, cc);
         if (count>0) ci = (ci+1)%(colors.size());
      }
      for (AlloyAtom atom: instance.getAllAtoms()) {
         List<AlloySet> sets = instance.atom2sets(atom);
         if (sets.size()>0) {
            for (AlloySet s: sets)
               if (view.nodeVisible.resolve(s) && !view.hideUnconnected.resolve(s))
               {createNode(hidePrivate, hideMeta, atom); break;}
         } else if (view.nodeVisible.resolve(atom.getType()) && !view.hideUnconnected.resolve(atom.getType())) {
            createNode(hidePrivate, hideMeta, atom);
         }
      }
      for (AlloyRelation rel: model.getRelations())
         if (!(hidePrivate && rel.isPrivate))
            if (view.attribute.resolve(rel))
               edgesAsAttribute(rel);
      for(Map.Entry<GraphNode,Set<String>> e: attribs.entrySet()) {
         Set<String> set = e.getValue();
         if (set!=null) for(String s: set) if (s.length() > 0) e.getKey().addLabel(s);
      }
      for(Map.Entry<AlloyRelation,Integer> e: rels.entrySet()) {
         Color c = magicColor.get(e.getKey());
         if (c==null) c = Color.BLACK;
         int n = e.getValue();
         if (n>0) graph.addLegend(e.getKey(), e.getKey().getName()+": "+n, c); else graph.addLegend(e.getKey(), e.getKey().getName(), null);
      }
   }

   /** Return the node for a specific AlloyAtom (create it if it doesn't exist yet).
    * @return null if the atom is explicitly marked as "Don't Show".
    */
   private GraphNode createNode(final boolean hidePrivate, final boolean hideMeta, final AlloyAtom atom) {
	  GraphNode node;
      node = atom2node.get(atom);
      if (node!=null) return node;
      if ( (hidePrivate && atom.getType().isPrivate)
            || (hideMeta    && atom.getType().isMeta)
            || !view.nodeVisible(atom, instance)) return null;
	  //If the graph nodes are layed out from previous frame of projection.
      for (GraphNode n : oldGraphNodes)
      {
    	  if (atom!=null&&n.getAtom()!=null&&atom.equals(n.getAtom()))
    	  {
        	  node = new GraphNode(graph, atom, atom, n.x(), n.y(), n.layer(), n.getPos(), atomname(atom, false)).set(n.shape()).set(n.getColor()).set(n.getStyle());
        	  //node.setProjTextHeight(n.getProjTextHeight());
        	  //node.setProjTextWidth(n.getProjTextWidth());
    	  }
      }
	  if (oldGraphNodes.isEmpty()||node==null)
	  {
          // Make the node
          DotColor color = view.nodeColor(atom, instance);
          DotStyle style = view.nodeStyle(atom, instance);
          DotShape shape = view.shape(atom, instance);
          String label = atomname(atom, false);
          node = new GraphNode(graph, atom, atom, label).set(shape).set(color.getColor(view.getNodePalette())).set(style);
	  }
	  
	  if (atomLabels!=null&&atomLabels.get(node.getAtom())!=null&&!atomLabels.get(node.getAtom()).isEmpty())
	  {
		  node.setSizeLabels(atomLabels.get(node.getAtom()));
	  }
	// Get the label based on the sets and relations
      String setsLabel="";
      boolean showLabelByDefault = view.showAsLabel.get(null);
      for (AlloySet set: instance.atom2sets(atom)) {
         String x = view.label.get(set); if (x.length()==0) continue;
         Boolean showLabel = view.showAsLabel.get(set);
         if ((showLabel==null && showLabelByDefault) || (showLabel!=null && showLabel.booleanValue()))
            setsLabel += ((setsLabel.length()>0?", ":"")+x);
      }
      if (setsLabel.length()>0) {
         Set<String> list = attribs.get(node);
         if (list==null) attribs.put(node, list=new TreeSet<String>());
         list.add("("+setsLabel+")");
      }
      nodes.put(node,atom);
      atom2node.put(atom,node);
      return node;
   }

   /** Create an edge for a given tuple from a relation (if neither start nor end node is explicitly invisible) */
   private boolean createEdge(final boolean hidePrivate, final boolean hideMeta, AlloyRelation rel, AlloyTuple tuple, boolean bidirectional, Color magicColor) {
      // This edge represents a given tuple from a given relation.
      //
      // If the tuple's arity==2, then the label is simply the label of the relation.
      //
      // If the tuple's arity>2, then we append the node labels for all the intermediate nodes.
      // eg. Say a given tuple is (A,B,C,D) from the relation R.
      // An edge will be drawn from A to D, with the label "R [B, C]"
      if ((hidePrivate && tuple.getStart().getType().isPrivate)
            ||(hideMeta    && tuple.getStart().getType().isMeta)
            || !view.nodeVisible(tuple.getStart(), instance)) return false;
      if ((hidePrivate && tuple.getEnd().getType().isPrivate)
            ||(hideMeta    && tuple.getEnd().getType().isMeta)
            || !view.nodeVisible(tuple.getEnd(), instance)) return false;
      GraphNode start = createNode(hidePrivate, hideMeta, tuple.getStart());
      GraphNode end = createNode(hidePrivate, hideMeta, tuple.getEnd());
      if (start==null || end==null) return false;
      boolean layoutBack = view.layoutBack.resolve(rel);
      String label = view.label.get(rel);
      if (tuple.getArity() > 2) {
         StringBuilder moreLabel = new StringBuilder();
         List<AlloyAtom> atoms=tuple.getAtoms();
         for (int i=1; i<atoms.size()-1; i++) {
            if (i>1) moreLabel.append(", ");
            moreLabel.append(atomname(atoms.get(i),false));
         }
         if (label.length()==0) { /* label=moreLabel.toString(); */ }
         else { label=label+(" ["+moreLabel+"]"); }
      }
      DotDirection dir = bidirectional ? DotDirection.BOTH : (layoutBack ? DotDirection.BACK : DotDirection.FORWARD);
      DotStyle style = view.edgeStyle.resolve(rel);
      DotColor color = view.edgeColor.resolve(rel);
      int weight = view.weight.get(rel);
      GraphEdge e = new GraphEdge((layoutBack ? end : start), (layoutBack ? start : end), tuple, label, rel);
      if (color == DotColor.MAGIC && magicColor != null) e.set(magicColor); else e.set(color.getColor(view.getEdgePalette()));
      e.set(style);
      e.set(dir!=DotDirection.FORWARD, dir!=DotDirection.BACK);
      e.set(weight<1 ? 1 : (weight>100 ? 10000 : 100*weight));
      edges.put(e, tuple);
      return true;
   }

   /** Create edges for every visible tuple in the given relation. */
   private int edgesAsArcs(final boolean hidePrivate, final boolean hideMeta, AlloyRelation rel, Color magicColor) {
      int count = 0;
      if (!view.mergeArrows.resolve(rel)) {
         // If we're not merging bidirectional arrows, simply create an edge for each tuple.
         for (AlloyTuple tuple: instance.relation2tuples(rel)) if (createEdge(hidePrivate, hideMeta, rel, tuple, false, magicColor)) count++;
         return count;
      }
      // Otherwise, find bidirectional arrows and only create one edge for each pair.
      Set<AlloyTuple> tuples = instance.relation2tuples(rel);
      Set<AlloyTuple> ignore = new LinkedHashSet<AlloyTuple>();
      for (AlloyTuple tuple: tuples) {
         if (!ignore.contains(tuple)) {
            AlloyTuple reverse = tuple.getArity()>2 ? null : tuple.reverse();
            // If the reverse tuple is in the same relation, and it is not a self-edge, then draw it as a <-> arrow.
            if (reverse!=null && tuples.contains(reverse) && !reverse.equals(tuple)) {
               ignore.add(reverse);
               if (createEdge(hidePrivate, hideMeta, rel, tuple, true, magicColor)) count = count + 2;
            } else {
               if (createEdge(hidePrivate, hideMeta, rel, tuple, false, magicColor)) count = count + 1;
            }
         }
      }
      return count;
   }

   /** Attach tuple values as attributes to existing nodes. */
   private void edgesAsAttribute(AlloyRelation rel) {
      // If this relation wants to be shown as an attribute,
      // then generate the annotations and attach them to each tuple's starting node.
      // Eg.
      //   If (A,B) and (A,C) are both in the relation F,
      //   then the A node would have a line that says "F: B, C"
      // Eg.
      //   If (A,B,C) and (A,D,E) are both in the relation F,
      //   then the A node would have a line that says "F: B->C, D->E"
      // Eg.
      //   If (A,B,C) and (A,D,E) are both in the relation F, and B belongs to sets SET1 and SET2,
      //   and SET1's "show in relational attribute" is on,
      //   and SET2's "show in relational attribute" is on,
      //   then the A node would have a line that says "F: B (SET1, SET2)->C, D->E"
      //
      Map<GraphNode,String> map = new LinkedHashMap<GraphNode,String>();
      for (AlloyTuple tuple: instance.relation2tuples(rel)) {
         GraphNode start=atom2node.get(tuple.getStart());
         if (start==null) continue; // null means the node won't be shown, so we can't show any attributes
         String attr="";
         List<AlloyAtom> atoms=tuple.getAtoms();
         for (int i=1; i<atoms.size(); i++) {
            if (i>1) attr+="->";
            attr+=atomname(atoms.get(i),true);
         }
         if (attr.length()==0) continue;
         String oldattr=map.get(start);
         if (oldattr!=null && oldattr.length()>0) attr=oldattr+", "+attr;
         if (attr.length()>0) map.put(start,attr);
      }
      for (Map.Entry<GraphNode,String> e: map.entrySet()) {
         GraphNode node = e.getKey();
         Set<String> list = attribs.get(node);
         if (list==null) attribs.put(node, list=new TreeSet<String>());
         String attr = e.getValue();
         if (view.label.get(rel).length()>0) attr = view.label.get(rel) + ": " + attr;
         list.add(attr);
      }
   }

   /** Return the label for an atom.
    * @param atom - the atom
    * @param showSets - whether the label should also show the sets that this atom belongs to
    *
    * <p> eg. If atom A is the 3rd atom in type T, and T's label is "Person",
    *      then the return value would be "Person3".
    *
    * <p> eg. If atom A is the only atom in type T, and T's label is "Person",
    *      then the return value would be "Person".
    *
    * <p> eg. If atom A is the 3rd atom in type T, and T's label is "Person",
    *      and T belongs to the sets Set1, Set2, and Set3.
    *      However, only Set1 and Set2 have "show in relational attribute == on",
    *      then the return value would be "Person (Set1, Set2)".
    */
   private String atomname(AlloyAtom atom, boolean showSets) {
      String label = atom.getVizName(view, view.number.resolve(atom.getType()));
      if (!showSets) return label;
      String attr = "";
      boolean showInAttrByDefault = view.showAsAttr.get(null);
      for (AlloySet set: instance.atom2sets(atom)) {
         String x = view.label.get(set); if (x.length()==0) continue;
         Boolean showAsAttr = view.showAsAttr.get(set);
         if ((showAsAttr==null && showInAttrByDefault) || (showAsAttr!=null && showAsAttr))
            attr += ((attr.length()>0?", ":"")+x);
      }
      if (label.length()==0) return (attr.length()>0) ? ("("+attr+")") : "";
      return (attr.length()>0) ? (label+" ("+attr+")") : label;
   }

   static String esc(String name) {
      if (name.indexOf('\"') < 0) return name;
      StringBuilder out = new StringBuilder();
      for(int i=0; i<name.length(); i++) {
         char c=name.charAt(i);
         if (c=='\"') out.append('\\');
         out.append(c);
      }
      return out.toString();
   }
}
