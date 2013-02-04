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

package edu.mit.csail.sdg.alloy4graph;

import java.awt.Polygon;
import java.awt.geom.Path2D;

import javax.swing.Icon;

import edu.mit.csail.sdg.alloy4.OurUtil;

/** Immutable; this defines the set of possible node shapes (BOX, CIRCLE, ELLIPSE...)
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public enum DotShape {

   /** Ellipse            */ ELLIPSE("Ellipse", "ellipse") {
	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
},
   /** Box                */ BOX("Box", "box") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
//		poly.addPoint(-hw,-hh); poly.addPoint(hw,-hh); poly.addPoint(hw,hh); poly.addPoint(-hw,hh);
		Path2D box = new Path2D.Double();
		int hw = x[0]; int hh = x[1];
		box.moveTo(-hw, -hh);
		Path2D haircut = hair.render(-hw, -hh, hw);
		box.append(haircut, true); 
		
		double foo = haircut.getBounds2D().getMaxX();
		
		box.lineTo(foo, hh);
		box.lineTo(-hw, hh);
		box.lineTo(-hw, -hh);
		box.closePath();
		return box;
	}


},
   /** Circle             */ CIRCLE("Circle", "circle") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Egg                */ EGG("Egg", "egg") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Triangle           */ TRIANGLE("Triangle", "triangle") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Diamond            */ DIAMOND("Diamond", "diamond") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Trapezoid          */ TRAPEZOID("Trapezoid", "trapezium") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D trapezoid = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int ad = x[2];
// 		poly.addPoint(-hw,-hh); poly.addPoint(hw,-hh); poly.addPoint(hw+ad,hh); poly.addPoint(-hw-ad,hh);
		trapezoid.moveTo(-hw, -hh);
		Path2D haircut = hair.render(-hw, -hh, hw);
		trapezoid.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		trapezoid.lineTo(foo+ad, hh);
		trapezoid.lineTo(-hw-ad, hh);
		trapezoid.lineTo(-hw, -hh);
		
		trapezoid.closePath();
		return trapezoid;
	}
	
},
   /** Parallelogram      */ PARALLELOGRAM("Parallelogram", "parallelogram") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D parallelogram = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int ad = x[2];
//		poly.addPoint(-hw, -hh); poly.addPoint(hw+ad, -hh); poly.addPoint(hw, hh); poly.addPoint(-hw-ad, hh);
		parallelogram.moveTo(-hw, -hh);
		Path2D haircut = hair.render(-hw, -hh, hw+ad);
		parallelogram.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		parallelogram.lineTo(foo-ad, hh);
		parallelogram.lineTo(-hw-ad, hh);
		parallelogram.lineTo(-hw, -hh);
		
		
		parallelogram.closePath();
		return parallelogram;
	}
	
},
   /** House              */ HOUSE("House", "house") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Hexagon            */ HEXAGON("Hexagon", "hexagon") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D hexagon = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int ad = x[2];
//		poly.addPoint(-hw-ad, 0); poly.addPoint(-hw, -hh); poly.addPoint(hw, -hh);
//      poly.addPoint(hw+ad, 0); poly.addPoint(hw, hh); poly.addPoint(-hw, hh);
		hexagon.moveTo(-hw, -hh);
		Path2D haircut = hair.render(-hw, -hh, hw);
		hexagon.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		hexagon.lineTo(foo+ad, 0);
		hexagon.lineTo(foo, hh);
		hexagon.lineTo(-hw, hh);
		hexagon.lineTo(-hw-ad, 0);
		hexagon.lineTo(-hw, -hh);
		
		
		hexagon.closePath();
		return hexagon;
	}
	
},
   /** Octagon            */ OCTAGON("Octagon", "octagon") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D octagon = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int dx = x[2]; int dy = x[3];
//      poly.addPoint(-hw, -hh); poly.addPoint(-hw+dx, -hh-dy); poly.addPoint(hw-dx, -hh-dy); poly.addPoint(hw, -hh);
//      poly.addPoint(hw, hh); poly.addPoint(hw-dx, hh+dy); poly.addPoint(-hw+dx, hh+dy); poly.addPoint(-hw, hh);
		octagon.moveTo(-hw+dx, -hh-dy);
		Path2D haircut = hair.render(-hw+dx, -hh-dy, hw-dx);
		octagon.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		octagon.lineTo(foo+dx, -hh);
		octagon.lineTo(foo+dx, hh);
		octagon.lineTo(foo, hh+dy);
		octagon.lineTo(-hw+dx, hh+dy);
		octagon.lineTo(-hw, hh);
		octagon.lineTo(-hw, -hh);
		octagon.lineTo(-hw+dx, -hh-dy);
		
		
		octagon.closePath();
		return octagon;
		
		
	}
	
},
   /** Double Circle      */ DOUBLE_CIRCLE("Dbl Circle", "doublecircle") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Double Octagon     */ DOUBLE_OCTAGON("Dbl Octagon", "doubleoctagon") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D doubleoctagon = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int dx = x[2]; int dy = x[3]; int x1 = x[4]; int y1 = x[5];
//      poly.addPoint(-hw-5, -hh-y1); poly.addPoint(-hw+dx-x1, -hh-dy-5); poly.addPoint(hw-dx+x1, -hh-dy-5);
//      poly.addPoint(hw+5, -hh-y1); poly.addPoint(hw+5, hh+y1); poly.addPoint(hw-dx+x1, hh+dy+5);
//      poly.addPoint(-hw+dx-x1, hh+dy+5); poly.addPoint(-hw-5, hh+y1);
		doubleoctagon.moveTo(-hw+dx-x1, -hh-dy-5);
		Path2D haircut = hair.render(-hw+dx-x1, -hh-dy-5, hw-dx+x1);
		doubleoctagon.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		doubleoctagon.lineTo(foo+dx+5, -hh-y1);
		doubleoctagon.lineTo(foo+dx+5, hh+y1);
		doubleoctagon.lineTo(foo+x1, hh+dy+5);
		doubleoctagon.lineTo(-hw+dx-x1, hh+dy+5);
		doubleoctagon.lineTo(-hw-5, hh+y1);
		doubleoctagon.lineTo(-hw-5, -hh-y1);
		doubleoctagon.lineTo(-hw+dx-x1, -hh-dy-5);
		
		
		doubleoctagon.closePath();
		return doubleoctagon;
		
		
	}
	
},
   /** Triple Octagon     */ TRIPLE_OCTAGON("Tpl Octagon", "tripleoctagon") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D tripleoctagon = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int dx = x[2]; int dy = x[3]; int x1 = x[4]; int y1 = x[5];
//      poly.addPoint(-hw-10, -hh-y1); poly.addPoint(-hw+dx-x1, -hh-dy-10); poly.addPoint(hw-dx+x1, -hh-dy-10);
//      poly.addPoint(hw+10, -hh-y1); poly.addPoint(hw+10, hh+y1); poly.addPoint(hw-dx+x1, hh+dy+10);
//      poly.addPoint(-hw+dx-x1, hh+dy+10); poly.addPoint(-hw-10, hh+y1);
		tripleoctagon.moveTo(-hw+dx-x1, -hh-dy-10);
		Path2D haircut = hair.render(-hw+dx-x1, -hh-dy-10, hw-dx+x1);
		tripleoctagon.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		tripleoctagon.lineTo(foo+dx+10, -hh-y1);
		tripleoctagon.lineTo(foo+dx+10, hh+y1);
		tripleoctagon.lineTo(foo+x1, hh+dy+10);
		tripleoctagon.lineTo(-hw+dx-x1, hh+dy+10);
		tripleoctagon.lineTo(-hw-10, hh+y1);
		tripleoctagon.lineTo(-hw-10, -hh-y1);
		tripleoctagon.lineTo(-hw+dx-x1, -hh-dy-10);
		
		
		tripleoctagon.closePath();
		return tripleoctagon;
		
	}
	
},
   /** Inverted Triangle  */ INV_TRIANGLE("Inv Triangle", "invtriangle") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D invtriangle = new Path2D.Double();
		int hw = x[0]; int updown = x[1]; int dx = x[2];
//		poly.addPoint(0, updown); poly.addPoint(hw+dx, -updown); poly.addPoint(-hw-dx, -updown);
		invtriangle.moveTo(-hw-dx, -updown);
		Path2D haircut = hair.render(-hw-dx, -updown, hw+dx);
		invtriangle.append(haircut, true);
		invtriangle.lineTo(haircut.getBounds2D().getCenterX(), updown);
		invtriangle.lineTo(-hw-dx, -updown);
				
		
		invtriangle.closePath();
		return invtriangle;
	}
	
},
   /** Inverted House     */ INV_HOUSE("Inv House", "invhouse") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D invhouse = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int yShift = x[2]; int updown = x[3];
//      poly.addPoint(-hw,yShift-hh); poly.addPoint(hw,yShift-hh); poly.addPoint(hw,yShift+hh);
//      poly.addPoint(0,updown); poly.addPoint(-hw,yShift+hh);
		invhouse.moveTo(-hw, yShift-hh);
		Path2D haircut = hair.render(-hw, yShift-hh, hw);
		invhouse.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		invhouse.lineTo(foo, yShift+hh);
		invhouse.lineTo(haircut.getBounds2D().getCenterX(), updown);
		invhouse.lineTo(-hw, yShift+hh);
		invhouse.lineTo(-hw, yShift-hh);
		
		
		invhouse.closePath();
		return invhouse;
	}
	
},
   /** Inverted Trapezoid */ INV_TRAPEZOID("Inv Trapezoid", "invtrapezium") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D inv_trapezoid = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int ad = x[2];
//		poly.addPoint(-hw-ad, -hh); poly.addPoint(hw+ad, -hh); poly.addPoint(hw, hh); poly.addPoint(-hw, hh);
		inv_trapezoid.moveTo(-hw-ad, -hh);
		Path2D haircut = hair.render(-hw-ad, -hh, hw+ad);
		inv_trapezoid.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		inv_trapezoid.lineTo(hw, hh);
		inv_trapezoid.lineTo(-hw, hh);
		inv_trapezoid.lineTo(-hw-ad, -hh);
		
		
		inv_trapezoid.closePath();
		return inv_trapezoid;
	}
	
},
   /** Lined Diamond      */ M_DIAMOND("Lined Diamond", "Mdiamond") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
},
   /** Lined Square       */ M_SQUARE("Lined Square", "Msquare") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		Path2D msquare = new Path2D.Double();
		int hw = x[0]; int hh = x[1]; int side = x[2];
//      poly.addPoint(-hw-4,-hh-4); poly.addPoint(hw+4,-hh-4); poly.addPoint(hw+4,hh+4); poly.addPoint(-hw-4,hh+4);
		msquare.moveTo(-hw-4, -hh-4);
		Path2D haircut = hair.render(-hw-4, -hh-4, hw+4);
		msquare.append(haircut, true);
		double foo = haircut.getBounds2D().getMaxX();
		msquare.lineTo(foo, hh+4);
		msquare.lineTo(-hw-4, hh+4);
		msquare.lineTo(-hw-4, -hh-4);
		
		msquare.moveTo(-side, side-8); msquare.lineTo(-side+8, side); 
		msquare.moveTo(foo, side-8); msquare.lineTo(foo-8, side);
//        gr.drawLine(-side, side-8, -side+8, side); gr.drawLine(side, side-8, side-8, side);
		
		
		msquare.closePath();
		return msquare;
	}
	
},
   /** Lined Circle       */ M_CIRCLE("Lined Circle", "Mcircle") {

	@Override
	public Path2D render(HairCut hair, Integer... x) {
		// TODO Auto-generated method stub
		return null;
	}
	
};

   /** The description of this line style. */
   private final String name;

   /** The icon for this line style. */
   private final Icon icon;

   /** The corresponding DOT attribute. */
   private final String dotName;

   /** Constructs a DotShape object. */
   private DotShape(String name, String dotName) {
      this.name = name;
      this.icon = OurUtil.loadIcon("icons/ShapeIcons/" + dotName + ".gif");
      this.dotName = dotName;
   }

/** Returns the String that will be displayed in the GUI to represent this value. */
   public String getDisplayedText() { return name; }

   /** Returns the String that should be written into the dot file for this value, when used with the given palette. */
   public String getDotText() { return dotName; }

   /** Returns the Icon that will be displayed in the GUI to represent this value, when used with the given palette. */
   public Icon getIcon() { return icon; }
   
   public abstract Path2D render(HairCut hair, Integer... x);

   /** This method is used in parsing the XML value into a valid Shape; returns null if there is no match. */
   public static DotShape parse(String x) {
      if (x != null) for(DotShape d: values()) if (d.name.equals(x)) return d;
      return null;
   }

   
   /** This value is used in writing XML. */
   @Override public String toString() { return name; }
}
