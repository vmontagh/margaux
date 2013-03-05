/* Alloy Analyzer 4 -- Copyright (c) 2007-2008, Derek Rayside
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4graph.DotColor;
import edu.mit.csail.sdg.alloy4graph.DotPalette;
import edu.mit.csail.sdg.alloy4graph.DotShape;
import edu.mit.csail.sdg.alloy4graph.HairCut;
import edu.mit.csail.sdg.alloy4graph.DotStyle;
import edu.mit.csail.sdg.alloy4viz.MagicLayout;

/** This class implements the automatic visualization inference.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

final class MagicColor {

   /** The VizState object that we're going to configure. */
   private final VizState vizState;
   public static List<HairCut> usedHair;
   
   /** Constructor. */
   private MagicColor(final VizState vizState) { this.vizState = vizState; usedHair = new ArrayList<HairCut>();}

   /** Main method to infer settings. */
   public static void magic(final VizState vizState) {
      vizState.setNodePalette(DotPalette.MARTHA);
      final MagicColor st = new MagicColor(vizState);
      st.nodeNames();
      st.nodeShape();
      st.nodeHair();
      st.nodeColour();
      st.skolemColour();
   }


   /** SYNTACTIC/VISUAL: Determine colours for nodes.
    *
    * when do we color things and what is the meaning of color
    * <ul>
    * <li> symmetry breaking: colors only matter up to recoloring (diff from
    * shape!)
    * <li> color substitutes for name/label
    * <li>
    * <li> Logic:
    * <li> If there are existential projections, Color by Sets 
    * <li> Otherwise color by type families
    * </ul>
    */
   private void nodeColour() { 
	   
	  if(isStateProjection(this.vizState)){doColourByRelation();}
	  else{doColourByType();}
   }

   /** SYNTACTIC/VISUAL: Determine colour highlighting for skolem constants. */
   private void skolemColour() {
      final Set<AlloySet> sets = vizState.getCurrentModel().getSets();
      for (final AlloySet s : sets) {
         // change the style
         vizState.nodeStyle.put(s, DotStyle.BOLD);
         // change the label
         String label = vizState.label.get(s);
         final int lastUnderscore = label.lastIndexOf('_');
         if (lastUnderscore >= 0) {
            label = label.substring(lastUnderscore+1);
         }
         vizState.label.put(s, label);
      }
   }

   /** SYNTACTIC/VISUAL: Determine shapes for nodes.
    * <ul>
    * <li> trapezoid, hexagon, rectangle, ellipse, circle, square -- no others
    * <li> actual shape matters -- do not break symmetry as with color
    * <li> ellipse by default
    * <li> circle if special extension of ellipse
    * <li> rectangle if lots of attributes
    * <li> square if special extension of rectangle
    * <li> when to use other shapes?
    *
    * </ul>
    */
   private void nodeShape() {
	  Set<DotShape> blacklist = new LinkedHashSet<DotShape>(); blacklist = fillBlacklist(blacklist);
      final Set<AlloyType> topLevelTypes = MagicUtil.partiallyVisibleUserTopLevelTypes(vizState);
      final LinkedHashMap<AlloyType, DotShape> Type_Shape = MakeMap(topLevelTypes, blacklist);

      for (final AlloyType t : Type_Shape.keySet()) {
          vizState.shape.put(t, Type_Shape.get(t));
          //Should Implement different hairs by checking used hairs.
       }
   }

   private LinkedHashMap<AlloyType, DotShape> MakeMap(Set<AlloyType> topLevelTypes, Set<DotShape> blacklist) {
	   	final LinkedHashMap<AlloyType, DotShape> Top_Shape = new LinkedHashMap<AlloyType, DotShape>();
	   	final LinkedHashMap<AlloyType, DotShape> Type_Shape = new LinkedHashMap<AlloyType, DotShape>();
	   	final Set<AlloyType> interestingType = new LinkedHashSet<AlloyType>();
        final List<DotShape> usedShapes = new ArrayList<DotShape>();

	   	
	   	// Collect Types to Consider
	   	interestingType.addAll(topLevelTypes);
	   	for(AlloyType t: topLevelTypes){
	   		final List<AlloyType> subTypes = vizState.getOriginalModel().getSubTypes(t);
	   		for(AlloyType st: subTypes){
	   			interestingType.add(st);
	   		}
	   	}
	   	// Make Type_Shape Map
	   	for(AlloyType t: interestingType){
	   		if(topLevelTypes.contains(t)){
	   			for(DotShape shape: DotShape.values()){
		           	 if(!usedShapes.contains(shape) && !blacklist.contains(shape)){
		           		 usedShapes.add(shape);
		           		 Type_Shape.put(t, shape);
		           		 break;
		           	 }
	            }
	   		}else if(t.isAbstract){
	   			for(DotShape shape: DotShape.values()){
		           	 if(!usedShapes.contains(shape) && !blacklist.contains(shape)){
		           		 usedShapes.add(shape);
		           		 Type_Shape.put(t, shape);
		           		 break;
		           	 }
	            }
	   		}
	   	} 
	    // User Type_Shape Map to Assign Shapes
	   	Set<AlloyType> ParentTypes= new LinkedHashSet<AlloyType>();
	   	ParentTypes = Top_Shape.keySet();
	   	for(AlloyType t: ParentTypes){
	   		Type_Shape.put(t, Top_Shape.get(t));
	   		for(AlloyType st: MagicUtil.visibleSubTypes(vizState, t)){
	   			Type_Shape.put(st, Top_Shape.get(t));
	   		}
	   	}
	return Type_Shape;
}

/** Assigns nodeHair by recursively calling AssignNodeHair() on top level types*/
private void nodeHair(){
	   final Set<AlloyType> topLevelTypes = MagicUtil.partiallyVisibleUserTopLevelTypes(vizState);
	   
	   for (final AlloyType t : topLevelTypes) {
		   final Set<AlloyType> subTypes = MagicUtil.visibleSubTypes(vizState, t);
	       final boolean isTvisible = MagicUtil.isActuallyVisible(vizState, t);
	       final int size = subTypes.size() + (isTvisible ? 1 : 0);
	       
	       if(size > HairCut.values().length){
	    	   assignNodeHair(t);
	       } else{
    	   // Temporarily does the same thing as the other option, until a more dynamic approach is created
	    	   assignNodeHair(t);	    	   
	       }
	   }
   }
   
   private Set<DotShape> fillBlacklist(Set<DotShape> blacklist){
	   blacklist.add(DotShape.CIRCLE);blacklist.add(DotShape.DIAMOND); blacklist.add(DotShape.DOUBLE_CIRCLE); blacklist.add(DotShape.EGG); blacklist.add(DotShape.ELLIPSE); blacklist.add(DotShape.HOUSE);
	   blacklist.add(DotShape.M_CIRCLE); blacklist.add(DotShape.M_DIAMOND); blacklist.add(DotShape.TRIANGLE); blacklist.add(DotShape.DOUBLE_OCTAGON); blacklist.add(DotShape.TRIPLE_OCTAGON);
	   
	   return blacklist;
   }
   
   /** Helper for nodeShape(). */
   private void assignNodeShape(final AlloyType t, final Set<AlloyType> subTypes, final boolean isTvisible, final DotShape shape) {
      // hair for t, if visible
      if (isTvisible) {
         vizState.shape.put(t, shape);
      }
      // hair for visible subtypes
      for (final AlloyType subt : subTypes) {
         vizState.shape.put(subt, shape);
         //Should Implement different hairs by checking used hairs.
      }
   }
   
   /** Assigns node hair for Supertypes and Subtypes.
    * - Subtypes never get bald hair cut
    * - Used hair cuts for subtypes are kept track of in usedHair*/
   	
   
   private void  assignNodeHair(final AlloyType t){
	   boolean isTvisible = MagicUtil.isActuallyVisible(vizState, t);
	   ConstList<AlloyType> subTypes = vizState.getCurrentModel().getSubTypes(t);
	   
	   /** assignNodeHair for top level types. Recursively call assignNode Hair for subtypes*/
	   if(vizState.isTopLevel(t)){
				  usedHair = new ArrayList<HairCut>();
			      if (isTvisible) {
			         vizState.haircut.put(t, HairCut.Bald);
			         usedHair.add(HairCut.Bald);
			      } else{
			    	 usedHair.add(HairCut.Bald);
			      }
			      for (final AlloyType subt : subTypes) {
			    	  assignNodeHair(subt);
			      }  
		/** If it has subtypes, assign shapes and then recurse.
		 * else only assign shape */	      
	   }else{
		   if(vizState.getCurrentModel().getSubTypes(t).isEmpty()){
			   if(isTvisible){
				   for(HairCut hair: HairCut.values()){ 
				    	 if(!usedHair.contains(hair)){
				    		 usedHair.add(hair); 
				    		 vizState.haircut.put(t, hair);
				    		 break;
				    	 }
				   }  
			   }	   
		   }else{
			   if(isTvisible){
				   for(HairCut hair: HairCut.values()){ 
				    	 if(!usedHair.contains(hair)){
				    		 usedHair.add(hair); 
				    		 vizState.haircut.put(t, hair);
				    		 break;
				    	 }
				   }  
			   }
			   
			   	for(AlloyType subt: vizState.getCurrentModel().getSubTypes(t)){
			   		assignNodeHair(subt);
			   	}
		   }
	   }
	      // shapes for visible subtypes
   }

   /** SYNTACTIC/VISUAL: Should the names of nodes be displayed on them?
    *
    * when should names be used?
    * <ul>
    * <li> not when only a single sig (e.g. state machine with only one 'node' sig)
    * <li> not when only a single relation
    * <li> check for single things _after_ hiding things by default
    * </ul>
    */
   private void nodeNames() {
      final Set<AlloyType> visibleUserTypes = MagicUtil.visibleUserTypes(vizState);
      // trim names
      for (final AlloyType t : visibleUserTypes) {
         // trim label before last slash
         MagicUtil.trimLabelBeforeLastSlash(vizState, t);
      }
      // hide names if there's only one node type visible
      if (1 == visibleUserTypes.size()) {
         vizState.label.put(visibleUserTypes.iterator().next(), "");
      }
   }
   
   private boolean isStateProjection(VizState model){
	   for(AlloyType t: model.projectedTypes){
		   if(MagicLayout.hasLikelyProjectionTypeName(t.getName()))return true;
	   }
	   return false;
   }
   
   private List<AlloyType> getStateProjection(VizState model){
	   List<AlloyType> stateType = new ArrayList<AlloyType>();
	   for(AlloyType t: model.projectedTypes){
		   if(MagicLayout.hasLikelyProjectionTypeName(t.getName())) stateType.add(t);
	   }
	   return stateType;
   }
   
   /** Populates the nodeColor Map with Type Families and their assigned colors*/
   private void doColourByType(){
	   final Set<AlloyType> visibleUserTypes = MagicUtil.visibleUserTypes(vizState);
	      final Set<AlloyType> uniqueColourTypes;
	      if (visibleUserTypes.size() <= 5) {
	         // can give every visible user type its own shape
	         uniqueColourTypes = visibleUserTypes;
	      } else {
	         // give every top-level visible user type its own shape
	         uniqueColourTypes = MagicUtil.partiallyVisibleUserTopLevelTypes(vizState);
	      }
	      int index = 0;
	      for (final AlloyType t : uniqueColourTypes) {	
	         vizState.nodeColor.put(t, (DotColor) DotColor.valuesWithout(DotColor.YELLOW, DotColor.MAGIC)[index]);
	         index = (index + 1) % DotColor.valuesWithout(DotColor.YELLOW, DotColor.MAGIC).length;
	      }
   }
   
   /**Returns the list of relations that are connected to the Atoms of the selected Type*/
   private List<AlloyRelation> getSubjectRelations(AlloyType domState){
	   List<AlloyRelation> subrelations = new ArrayList<AlloyRelation>();
	   for(AlloyRelation rel: vizState.getOriginalModel().getRelations()){
	    	if(rel.getTypes().contains(domState)) subrelations.add(rel); 
	   }
	   return subrelations;
   }
   
   /**Returns a list of Sets that correspond to the list of given relations*/
   private List<AlloySet> getSets(List<AlloyRelation> SubRelation){
	   List<AlloySet> ProjectSets = new ArrayList<AlloySet>();
	   Set<AlloySet> CurrentSets = new TreeSet<AlloySet>(vizState.getCurrentModel().getSets());

	   for(AlloyRelation r: SubRelation){
		   for(AlloySet s : CurrentSets){
			   if(r.getName().equals(s.getName())){
				   ProjectSets.add(s);
			   }
		   }
	   }
		   
	   return ProjectSets;
   }
   
   
   /** Populates nodeColor with the AlloySets according to the algorithm:
    *  - Get list of Existential projection types
    *  - Get the dominant projection type
    *  - Get relations corresponding to the dominant projection type
    *  - Retrieve sets from the subject relations
    *  - Color the model according the sets */
   private void doColourByRelation(){
	   AlloyType domState = MagicLayout.Existential.iterator().next();
	   List<AlloyRelation> SubRelation = getSubjectRelations(domState);
	   List<AlloySet> ProjectSets = getSets(SubRelation);
	   
	   int index = 0;
	   for(AlloySet s: ProjectSets){
		   System.out.println((DotColor) DotColor.valuesWithout(DotColor.YELLOW, DotColor.MAGIC)[index]);
		   vizState.nodeColor.put(s, (DotColor) DotColor.valuesWithout(DotColor.YELLOW, DotColor.MAGIC)[index]);
	       index = (index + 1) % DotColor.valuesWithout(DotColor.YELLOW, DotColor.MAGIC).length;
	   }
   }
}
