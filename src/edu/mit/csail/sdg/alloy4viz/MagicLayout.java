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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4graph.DotColor;

/** This class implements the automatic visualization inference.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

final class MagicLayout {

   /** The VizState object that we're going to configure. */
   private final VizState vizState;

   private Set<AlloyType> enumerationTypes = new LinkedHashSet<AlloyType>();
   private Set<AlloyType> singletonTypes = new LinkedHashSet<AlloyType>();
   private AlloyType projectionType = null;
   private Set<AlloyType> projectedtypes = new LinkedHashSet<AlloyType>();
   private Set<AlloyRelation> spineRelations = Collections.emptySet();
   public static Set<AlloyType> Existential = new LinkedHashSet<AlloyType>();

   /** Constructor. */
   public MagicLayout(final VizState vizState) { 
	   this.vizState = vizState; 
	   enumerationTypes = new LinkedHashSet<AlloyType>();
	   singletonTypes = new LinkedHashSet<AlloyType>();
	   projectionType = null;
	   projectedtypes = new LinkedHashSet<AlloyType>();
	   spineRelations = Collections.emptySet();
	   Existential = new LinkedHashSet<AlloyType>();
   }

   /** Main method to infer settings. */
   public static void magic(final VizState vizState) {
      vizState.resetTheme();
      final MagicLayout st = new MagicLayout(vizState);
      st.identifyEnumerationTypes();
      st.projection();
      st.nodeVisibility();
      st.spine();
      st.attributes();
      st.edgeLabels();
   }

   /** SYNTACTIC: An enumeration follows the pattern "abstract sig Colour; one sig Red; one sig Blue".
    */
   private void identifyEnumerationTypes() {
      final AlloyModel model = vizState.getCurrentModel();
      final Set<AlloyType> types = model.getTypes();
      for (final AlloyType t : types) {
         if (enumerationTypes.contains(t)) continue; // we've already checked this one, don't muck with it now
         if (t.isOne) singletonTypes.add(t);
         if (!t.isBuiltin && t.isAbstract) {
            List<AlloyType> subTypes = model.getSubTypes(t);
            int numberOfSingletonSubtypes = 0;
            for (AlloyType st: subTypes) {
               if (st.isOne) { numberOfSingletonSubtypes++; singletonTypes.add(st); }
            }
            if (subTypes.size() == numberOfSingletonSubtypes) { // we have a winner!
               enumerationTypes.add(t);
               enumerationTypes.addAll(subTypes);
               for (final AlloyType st: subTypes) {
                  // all of the subtypes in the enumeration should have visibility inherited
                  // so that the user only needs to make the abstract supertype visible if we made a mistake hiding these things
                  vizState.nodeVisible.put(st, null);
               }
               // hide unless these are the source of some relation
               boolean visible = false;
               for (AlloyRelation r : model.getRelations()) {
                  AlloyType sourceType = r.getTypes().get(0);
                  if (t.equals(sourceType) || subTypes.contains(sourceType)) { visible = true;  break; }
               }
               vizState.nodeVisible.put(t, visible); // log("VizInference: visible status of enumeration type " + t + " " + visible);
            }
            
         }
      }

   }

   /** SEMANTIC/LAYOUT: Determine Types to Project over.
    *
    * Projects over one or more Existential Types or Lone Singletons and at most ONE ternary type
    * <ul>
	* <li> Current Algorithm:
	* <li> Gets list of types in current model
	* <li> for each type, checks if the type qualifies as a Lone Singleton
	* <li> checks if the type qualifies as an Existential Type
	* <li> ELSE, checks to see if the type is a ternary type, and assigns a score
	* <li> Ternary wrapper types get higher score
	* <li> Project over all Lone Singleton and Existential types.
	* <li> and Project over the winner of ternary type
	* <li> End of Algorithm
	* <li>
    * <li> position in relation (always end or always beginning)
    * <li> should we try to catch projections such as the one over birthday
    * books?
    * <li> pattern match (b,b') to catch transition relations
    * <li> add combo box in GUI (?)
    * </ul>
    */
   /**
 * 
 */
private void projection() {
      // only fiddle with this if it hasn't already been set somewhere else
      final Set<AlloyType> winners = new LinkedHashSet<AlloyType>();
      final Set<AlloyType> LoneSingleton = new LinkedHashSet<AlloyType>();
      final Set<AlloyType> TernaryTypes = new LinkedHashSet<AlloyType>();
      final Set<AlloyType> FlaggedTypes = new LinkedHashSet<AlloyType>();
      
      if (projectionType == null && vizState.getProjectedTypes().isEmpty()) {
         AlloyModel model = vizState.getCurrentModel();
         //final Set<AlloyType> candidateTypes = new HashSet<AlloyType>();
         Map<AlloyType,Integer> scores = new LinkedHashMap<AlloyType,Integer>();
         for (AlloyType t : model.getTypes()) {
        	 if (isLoneSingleton(model, t)) LoneSingleton.add(t);
        	 else if(hasLikelyProjectionTypeName(t.getName())) Existential.add(t);
        	 else{
	        	scores.put(t, 0);
		        if(hasTernary(model, t)){ 
		        	if(isTernaryWrapperType(model, t)) scores.put(t, scores.get(t)+1);
		        	scores.put(t, scores.get(t)+1);
		        }
        	 }			     
         }
			     // now we have the scores, see who the winners are:
			     int max = 0;
			     for (final Map.Entry<AlloyType,Integer> e : scores.entrySet()) {
			        if (e.getValue() == max) TernaryTypes.add(e.getKey());
			        if (e.getValue() > max) { max = e.getValue();  TernaryTypes.clear();  TernaryTypes.add(e.getKey()); }
			     }
			     if (max == 0) {
			    	 TernaryTypes.clear();
			        // no winner, don't project
			        // log("VizInference: no candidate type to project on.");
			     }
			     //If all visible nodes are projected then flush the ternary projections
			     if(ProjectedEverything(LoneSingleton, TernaryTypes, Existential, model)) TernaryTypes.clear();     
			   
			     
			   winners.addAll(LoneSingleton);  
			   winners.addAll(Existential);
			   
			   
			   if(!TernaryTypes.isEmpty()){
					   
						   for(AlloyType e: TernaryTypes){
							   if(withExistential(model, e, Existential)){
								   FlaggedTypes.add(e);
							   }
							   
						   }
						   for(AlloyType t: TernaryTypes){
							   if(!FlaggedTypes.contains(t) && MagicUtil.isActuallyVisible(vizState, t) && !t.getName().endsWith("/Ord")){
							   		winners.add(t);
							   		break;
							   }
						   }
				   }
				   
			   }

			   // Project over more that one Type
			   		for(AlloyType winner : winners){
			   			projectedtypes.add(winner);
			   			projectionType = winner;
				        vizState.project(projectionType);
			   		}
      }
   

   private boolean withExistential(AlloyModel model, AlloyType e, Set<AlloyType> existential) {
	   boolean verdict = false;
	     for (AlloyRelation r : model.getRelations()) {
	               if (r.getArity() > 2 && r.getTypes().contains(e)){ 
	            	   	for(AlloyType t : existential){
	            	   		if(r.getTypes().contains(t)){
	            	   			verdict = true;
	            	   			break;
	            	   		}
	            	   	}
	               }
	     }
	     return verdict;
	
}

private final static ConstList<String> LIKELY_PROJECTION_TYPE_NAMES = Util.asList("State", "TrainState", "Time", "Tick", "TimeStep");

   public static boolean hasLikelyProjectionTypeName(final String n) {
      for(String s: LIKELY_PROJECTION_TYPE_NAMES) if (n.startsWith(s) || n.endsWith(s)) return true;
      return false;
   }

   /** SEMANTIC/LAYOUT: Determine some relations to be the spine (ie, influence
    * the layout).
    *
    * Which relations should be used to layout? all? none? clever?
    * <ul>
    * <li> interesting example: 2d game grid
    * <li> ex: toplogical sort -- layout tree and list, not cnxn between them
    * <li> look for homogenius binary relation (a -&gt; a)
    * <li> may be several relations defining the spine
    * </ul>
    *
    */
   private void spine() {
      AlloyModel model = vizState.getCurrentModel();
      Set<AlloyRelation> relations = model.getRelations();
      if (!relations.isEmpty()) {
         // only mess with the relations if there are some
         // only binary relations are candidates
         Set<AlloyRelation> spines = new LinkedHashSet<AlloyRelation>();
         for (AlloyRelation r : relations) {
            if (r.getArity() == 2) {
               List<AlloyType> rtypes = r.getTypes();
               AlloyType targetType = rtypes.get(1);
               // only a spine if the target is not an enumeration type
               if (!enumerationTypes.contains(targetType)) { spines.add(r); }
               // however, binary relations named parent should be layed out backwards
               if (r.getName().equals("parent")) { vizState.layoutBack.put(r, true); }
            }
         }
         // do we have any spines? if so, use them, if not use all relations
         spineRelations = spines.isEmpty() ? relations : spines;
      }
      // set everything to not influence layout
      for (AlloyRelation r : relations) {
         vizState.constraint.put(r, false);
         vizState.edgeColor.put(r, DotColor.GRAY);
      }
      // set spines to influence layout
      for (AlloyRelation s : spineRelations) {
         vizState.constraint.put(s, null);
         // inherit the default color, which should be black
         vizState.edgeColor.put(s, null);
      }

   }

   /** SEMANTIC/LAYOUT: Determine whether non-projection, non-spine relations
    * should be shown as attributes or edges.
    *
    * <ul>
    * <li> binary vs. higher arity -- only make binary attributes
    * <li> use attributes on-demand to reduce clutter, not blindly
    * <li> functional relations should be attributes (what about a tree?)
    * <li> never make something an edge and an attribute
    *
    * </ul>
    *
    */
   private void attributes() {
      AlloyModel model = vizState.getCurrentModel();
      Set<AlloyRelation> visRelation = new LinkedHashSet<AlloyRelation>();
      visRelation = getVisRelations(model);
      
      
      for (AlloyRelation r : model.getRelations()) {
    	  if(visRelation.size()>1){
         List<AlloyType> rTypes = r.getTypes();
         
         if (r.getArity()==2 && !rTypes.contains(projectionType)){
            // it's binary, non-projection and non-spine
            AlloyType targetType = rTypes.get(1);
            if (enumerationTypes.contains(targetType) || (isLonely(targetType) && isSilent(targetType))) {
               // target is an enumeration: we have an attribute
               vizState.attribute.put(r, true);
               visRelation.remove(r);
               vizState.edgeVisible.put(r, false);
               if(!targetType.isAbstract){
            	   vizState.nodeVisible.put(targetType, false);
               }
            }
         }
      }
      }
   }


   private Set<AlloyRelation> getVisRelations(AlloyModel model) {
	  Set<AlloyRelation> tower = new LinkedHashSet<AlloyRelation>();
	  for(AlloyRelation r: model.getRelations()){
		  boolean flag = true;
		  for(AlloyType t: r.getTypes()){
			  if(!ProjectableTypes(model).contains(t)){
				  flag = false;
			  }
		  }
		  if(flag) tower.add(r);
	  }
	   return tower;   
}

private boolean isSilent(AlloyType targetType) {
	   int strike = 0;
	for(AlloyRelation r: vizState.getCurrentModel().getRelations()){
		if(r.getTypes().get(0)==targetType && ProjectableTypes(vizState.getCurrentModel()).contains(r.getTypes().get(1))){
			if(strike == 0){
//				strike = 1;
				return false;
			}
//			else if(strike == 1){
//				return false;
//			}
		}
	}
	return true;
}

private boolean isLonely(AlloyType targetType) {
	int strike = 0;
	for(AlloyRelation r: vizState.getCurrentModel().getRelations()){
		if(r.getTypes().get(1)==targetType && ProjectableTypes(vizState.getCurrentModel()).contains(r.getTypes().get(0))){
			if(strike == 0){
				strike = 1;
			}else if(strike == 1){
				return false;
			}
		}
	}
	return true;
	
}

/** PRESENTATIONAL: Labels for edges. */
   private void edgeLabels() {
      AlloyModel model = vizState.getCurrentModel();
      int relationsAsEdges = 0;
      AlloyRelation visibleRelation = null;
      for (AlloyRelation r : model.getRelations()) {
         Boolean v = vizState.edgeVisible.get(r);
         if (v == null || v.booleanValue()) {
            // it's visible
            relationsAsEdges++;
            visibleRelation = r;
            // remove text before last slash
            MagicUtil.trimLabelBeforeLastSlash(vizState, r);
         }
      }
      // If there's only one relation visible as an edge, and it's binary, then no need to label it.
      if (1 == relationsAsEdges && visibleRelation.getArity()==2) {
         vizState.label.put(visibleRelation, "");
      }
   }


   /** SYNTACTIC/VISUAL: Hide some things. */
   private void nodeVisibility() {
      AlloyModel model = vizState.getCurrentModel();
      Set<AlloyType> types = model.getTypes();
      for (AlloyType t: types) if (!t.isBuiltin && MagicUtil.isActuallyVisible(vizState, t) && t.getName().endsWith("/Ord")) {
         vizState.nodeVisible.put(t, false);
      }
      for (AlloySet s: model.getSets()) if (MagicUtil.isActuallyVisible(vizState, s) && s.getName().endsWith("/Ord")) {
         vizState.nodeVisible.put(s, false);
      }
   }
  
   /** Function to check whether the subject type is member of relation(s) with arity > 2 */
   private boolean hasTernary(AlloyModel currentmodel, AlloyType t){
	     boolean verdict = false;
	     for (AlloyRelation r : currentmodel.getRelations()) {

	               if (r.getArity() > 2 && r.getTypes().contains(t)){ 
	            	   	verdict = true;
	               }
	     }
	     return verdict;
	}
   
   /** Function to check whether the subject type is a Singleton with the following properties
    * - Has no subtypes
    * - Has no supertypes
    * - is member of ternary relations */
   
   private boolean isLoneSingleton(AlloyModel currentmodel ,AlloyType t){
	   return(t.isOne && currentmodel.getSubTypes(t).isEmpty() && currentmodel.getSuperType(t).toString().equals("univ") && hasTernary(currentmodel, t) && ProjectableTypes(currentmodel).contains(t)) ? true : false;	
   } 
   
   /**Function to check whether the subject type is the wrapper type {r.getTypes().get(0)} of a ternary relation*/
   private boolean isTernaryWrapperType(AlloyModel currentmodel ,AlloyType t){
	   boolean verdict = false;
	   for (AlloyRelation r : currentmodel.getRelations()){
		   if (r.getArity() > 2 && r.getTypes().contains(t)){
			   if (r.getTypes().get(0).equals(t)){
				   if (!t.isBuiltin && !t.getName().endsWith("/Ord")){
					   verdict = true;
				   }
			   }
		   }
		   
	   }
	   return verdict;
   }
   
   /**Gets a set of the Projectable Types for the current model*/
   private Set<AlloyType> ProjectableTypes(AlloyModel model){
	   final Set<AlloyType> PTypes = new LinkedHashSet<AlloyType>();
	   for(AlloyType t : model.getTypes()){
		   if(!t.isBuiltin && MagicUtil.isActuallyVisible(vizState, t) && !t.getName().endsWith("/Ord")){
			   PTypes.add(t);
		   }
	   }
	   return PTypes;
   }
   
   /**Checks if no nodes are visible due to projecting on all possible types */
   private boolean ProjectedEverything (Set<AlloyType> LoneSingleton, Set<AlloyType> TernaryTypes, Set<AlloyType> Existential, AlloyModel model){
	   final Set<AlloyType> AllProjections = new LinkedHashSet<AlloyType>();
	   AllProjections.addAll(LoneSingleton);
	   AllProjections.addAll(Existential);
	   AllProjections.addAll(TernaryTypes);
	   if(AllProjections.equals(ProjectableTypes(model))) return true;
	   return false;
   }
	  
}
