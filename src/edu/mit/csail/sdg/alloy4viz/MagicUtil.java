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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** This class implements the automatic visualization inference.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public class MagicUtil {

   /** Constructor.
    */
   private MagicUtil() {}

   static void trimLabelBeforeLastSlash(final VizState vizState, final AlloyElement x) {
      vizState.label.put(x, trimBeforeLastSlash(vizState.label.get(x)));
   }

   static String trimBeforeLastSlash(final String label) {
      final int lastSlash = label.lastIndexOf('/');
      if (lastSlash >= 0) {
         return label.substring(lastSlash+1);
      } else {
         return label;
      }
   }


   /** Determines whether a type is actually visible -- ie, if it has an inherited value,
    * looks up the hierarchy until that is resolved. NB: abstract types are not actually visible.
    * @param t
    * @return true if this type will be shown to the user, false if this type will be hidden from the user
    */
   public static boolean isActuallyVisible(final VizState vizState, final AlloyType t) {
      if (t.isAbstract) return false;

      // search up hierarchy
      final AlloyModel model = vizState.getCurrentModel();
      AlloyType current = t;
      while (current != null) {
    	  
    	 //returns null if the "current" is a nonTopLevel abstract type and is not the subject type 't', because otherwise subtypes of 
    	 // 'current' would be invisible
    	 final Boolean pV;
    	 if(secondaryAbstract(vizState, current) && t != current)pV = null;
    	 else pV = vizState.nodeVisible.get(current);
   	 
         final Boolean pH = vizState.hideUnconnected.get(current);
         
         if (pV != null) {
        	 if (pH != null) {
        		 // real pV, real pH
        		 return (pV && !pH);
        	 } else {
        		 // real pV, no pH
        		 return pV;
        	 }
         } else {
        	 if (pH != null) {
        		 // no pV, real pH
        		 return !pH;
        	 } else {
        		 // no pV, no pH
        		 // do nothing, continue up hierarchy
        	 }
         }
         
    	 // keep going up
    	 current = model.getSuperType(current);
      }
     // made it to univ without finding a real setting
     return true;
   }

   private static boolean secondaryAbstract(VizState vizState, AlloyType t) {
	   System.out.println(topLevelTypes(vizState));
	   if(t.isAbstract && !topLevelTypes(vizState).contains(t)) return true;
	   return false;
}

static boolean isActuallyVisible(final VizState vizState, final AlloySet s) {
      final Boolean V = vizState.nodeVisible.get(s);
      if (V != null) return V;

      return isActuallyVisible(vizState, s.getType());
   }

   /** Returns all of the visible user-types in the current model.
    * @param vizState
    */
   static Set<AlloyType> visibleUserTypes(final VizState vizState) {
      final Set<AlloyType> result = new LinkedHashSet<AlloyType>();
      final AlloyModel model = vizState.getCurrentModel();
      for (final AlloyType t : model.getTypes()) {
         if (!t.isBuiltin && MagicUtil.isActuallyVisible(vizState, t)) {
            result.add(t);
         }
      }
      return Collections.unmodifiableSet(result);
   }

   /** Returns all of the top-level types in the original model.
    * @param vizState
    */
   static Set<AlloyType> topLevelTypes(final VizState vizState) {
      final Set<AlloyType> result = new LinkedHashSet<AlloyType>();
      final AlloyModel model = vizState.getOriginalModel();
      for (final AlloyType t : model.getTypes()) {
         if (vizState.isTopLevel(t)) {
            result.add(t);
         }
      }
      return Collections.unmodifiableSet(result);
   }

   /** Returns every top-level user type that is itself visible or has a visible subtype.
    * @param vizState
    */
   public static Set<AlloyType> partiallyVisibleUserTopLevelTypes(final VizState vizState) {
      final AlloyModel model = vizState.getOriginalModel();
      final Set<AlloyType> visibleUserTypes = visibleUserTypes(vizState);
      //final Set<AlloyType> topLevelTypes = topLevelTypes(vizState);

      final Set<AlloyType> result = new LinkedHashSet<AlloyType>();

      for (final AlloyType t : visibleUserTypes) {
         if (visibleUserTypes.contains(t)) {
            result.add(model.getTopmostSuperType(t));
         }
      }

      return Collections.unmodifiableSet(result);
   }
   
   public static Set<AlloyType> getvisibleSubTypes(final VizState vizState, final AlloyType type){
	   return visibleSubTypes(vizState, type);
   }

   /** Returns the set of visible subtypes for the given type.
    * @param vizState
    * @param type
    */
   static Set<AlloyType> visibleSubTypes(final VizState vizState, final AlloyType type) {
      final AlloyModel model = vizState.getCurrentModel();
      final List<AlloyType> subTypes = model.getSubTypes(type);
      final Set<AlloyType> visibleUserTypes = visibleUserTypes(vizState);
      final Set<AlloyType> result = new LinkedHashSet<AlloyType>();

      for (final AlloyType st : subTypes) {
         if (visibleUserTypes.contains(st)) {
            result.add(st);
         }
      }

      return Collections.unmodifiableSet(result);
   }

}
