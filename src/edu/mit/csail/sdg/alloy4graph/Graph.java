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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import static edu.mit.csail.sdg.alloy4graph.Artist.getBounds;

/** Mutable; represents a graph.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public abstract strictfp class Graph {

   //================================ adjustable options ========================================================================//

   /** Minimum horizontal distance between adjacent nodes. */
   static final int xJump = 30;

   /** Minimum vertical distance between adjacent layers. */
   static final int yJump = 60;

   /** The horizontal distance between the first self-loop and the node itself. */
   static final int selfLoopA = 40;

   /** The horizontal padding to put on the left side of a self-loop's edge label. */
   static final int selfLoopGL = 2;

   /** The horizontal padding to put on the right side of a self-loop's edge label. */
   static final int selfLoopGR = 20;

   /** The maximum ascent and descent. We deliberately do NOT make this field "static" because only AWT thread can call Artist. */
   private final int ad = Artist.getMaxAscentAndDescent();

   //=============================== fields ======================================================================================//

   /** The default magnification. */
   final double defaultScale;

   /** The left edge. */
   private int left = 0;

   /** The top edge. */
   private int top = 0;

   /** The bottom edge. */
   private int bottom = 0;

   /** The total width of the graph; this value is computed by layout(). */
   private int totalWidth = 0;

   /** The total height of the graph; this value is computed by layout(). */
   private int totalHeight = 0;

   /** The height of each layer. */
   int[] layerPH = null;

   /** The list of layers;  must stay in sync with GraphNode.graph and GraphNode.layer
    * (empty iff there are no nodes; every node is always in exactly one layer, and appears exactly once in that layer)
    */
   final List<List<GraphNode>> layerlist = new ArrayList<List<GraphNode>>();

   /** The list of nodes;  must stay in sync with GraphNode.graph and GraphNode.pos
    * (every node is in exactly one graph's nodelist, and appears exactly once in that graph's nodelist)
    */
   final List<GraphNode> nodelist = new ArrayList<GraphNode>();

   /** The list of edges;  must stay in sync with GraphEdge.a.graph and GraphEdge.b.graph
    * (every edge is in exactly one graph's edgelist, and appears exactly once in that graph's edgelist)
    */
   final List<GraphEdge> edgelist = new ArrayList<GraphEdge>();

   /** An unmodifiable view of the list of nodes. */
   public final List<GraphNode> nodes = Collections.unmodifiableList(nodelist);

   /** An unmodifiable view of the list of edges. */
   public final List<GraphEdge> edges = Collections.unmodifiableList(edgelist);

   /** An unmodifiable empty list. */
   protected final List<GraphNode> emptyListOfNodes = Collections.unmodifiableList(new ArrayList<GraphNode>(0));

   //============================================================================================================================//

   /** Constructs an empty Graph object. */
   public Graph(double defaultScale)  { this.defaultScale = defaultScale; }

   /** Assuming layout() has been called, this returns the left edge. */
   public final int getLeft() { return left; }

   /** Assuming layout() has been called, this returns the top edge. */
   public final int getTop() { return top; }

   /** Assuming layout() has been called, this returns the total width. */
   public final int getTotalWidth() { return totalWidth; }

   /** Assuming layout() has been called, this returns the total height. */
   public final int getTotalHeight() { return totalHeight; }

   /** Returns an unmodifiable view of the list of nodes in the given layer (0..#layer-1); return an empty list if no such layer. */
   protected final List<GraphNode> layer(int i) {
      if (i>=0 && i<layerlist.size()) return Collections.unmodifiableList(layerlist.get(i));
      return emptyListOfNodes;
   }

   /** Return the number of layers; can be 0. */
   protected final int layers() { return layerlist.size(); }

   /** Swap the given two nodes in the given layer. */
   protected final void swapNodes(int layer, int node1, int node2) {
      List<GraphNode> list = layerlist.get(layer);
      GraphNode n1 = list.get(node1), n2 = list.get(node2);
      list.set(node1, n2);
      list.set(node2, n1);
   }

   /** Sort the list of nodes according to the order in the given list. */
   protected final void sortNodes(Iterable<GraphNode> newOrder) {
      // The nodes that are common to this.nodelist and newOrder are moved to the front of the list, in the given order.
      // The nodes that are in this.nodelist but not in newOrder are moved to the back in an unspecified order.
      // The nodes that are in newOrder but not in this.nodelist are ignored.
      int i=0, n=nodelist.size();
      again:
      for(GraphNode x:newOrder) for(int j=i; j<n; j++) if (nodelist.get(j)==x) {
         if (i!=j) { GraphNode tmp=nodelist.get(i); nodelist.set(i,x); nodelist.set(j,tmp); }
         i++;
         continue again;
      }
      i=0;
      for(GraphNode x: nodelist) { x.pos = i; i++; }
   }

   /** Sort the list of nodes in a given layer (0..#layer-1) using the given comparator. */
   protected final void sortLayer(int layer, Comparator<GraphNode> comparator) { Collections.sort(layerlist.get(layer), comparator); }

   /** A list of legends; each legend is an Object with the associated text label and color. */
   protected final SortedMap<Comparable<?>,Pair<String,Color>> legends = new TreeMap<Comparable<?>,Pair<String,Color>>();

   /** Add a legend with the given object and the associated text label; if color==null, that means we will
    * still add this legend into the list of legends, but this legend will be hidden.
    */
   public final void addLegend(Comparable<?> object, String label, Color color) { legends.put(object, new Pair<String,Color>(label,color)); }

   //============================================================================================================================//

   //============================================================================================================================//

   /** For each edge coming out of this layer of nodes, add bends to it if it currently overlaps some nodes inappropriately. */
   protected final void checkUpperCollision(List<GraphNode> top) {
      final int room=2; // This is how much we need to stay clear of a node's boundary
      for(int i=0; i<top.size(); i++) {
         GraphNode a = top.get(i); double left = a.x() - a.getWidth()/2, right = a.x() - a.getWidth()/2;
         for(GraphEdge e: a.outs) {
            GraphNode b = e.b();
            if (b.x()>=right) for(int j=i+1; j<top.size(); j++) { // This edge goes from top-left to bottom-right
               GraphNode c=top.get(j);
               if (c.shape()==null) continue; // You can intersect thru a dummy node
               double ctop=c.y()-c.getHeight()/2, cleft=c.x()-c.getWidth()/2, cbottom=c.y()+c.getHeight()/2;
               e.path().bendDown(cleft, ctop-room, cbottom+room, 3);
            }
            else if (b.x()<=left) for(int j=i-1; j>=0; j--) { // This edge goes from top-right to bottom-left
               GraphNode c=top.get(j);
               if (c.shape()==null) continue; // You can intersect thru a dummy node
               double ctop=c.y()-c.getHeight()/2, cright=c.x()+c.getWidth()/2, cbottom=c.y()+c.getHeight()/2;
               e.path().bendDown(cright, ctop-room, cbottom+room, 3);
            }
         }
      }
   }

   //============================================================================================================================//

   /** For each edge going into this layer of nodes, add bends to it if it currently overlaps some nodes inappropriately. */
   protected final void checkLowerCollision(List<GraphNode> bottom) {
      final int room=2; // This is how much we need to stay clear of a node's boundary
      for(int i=0; i<bottom.size(); i++) {
         GraphNode b=bottom.get(i); double left=b.x()-b.getWidth()/2, right=b.x()-b.getWidth()/2;
         for(GraphEdge e: b.ins) {
            GraphNode a=e.a();
            if (a.x()<=left) for(int j=i-1; j>=0; j--) { // This edge goes from top-left to bottom-right
               GraphNode c=bottom.get(j);
               if (c.shape()==null) continue; // You can intersect thru a dummy node
               double ctop=c.y()-c.getHeight()/2, cright=c.x()+c.getWidth()/2, cbottom=c.y()+c.getHeight()/2;
               e.path().bendUp(cright, ctop-room, cbottom+room, 3);
            }
            else if (a.x()>=right) for(int j=i+1; j<bottom.size(); j++) { // This edge goes from top-right to bottom-left
               GraphNode c=bottom.get(j);
               if (c.shape()==null) continue; // You can intersect thru a dummy node
               double ctop=c.y()-c.getHeight()/2, cleft=c.x()-c.getWidth()/2, cbottom=c.y()+c.getHeight()/2;
               e.path().bendUp(cleft, ctop-room, cbottom+room, 3);
            }
         }
      }
   }

   //============================================================================================================================//

   /** Returns true if a direct line between a and b will not intersect any other node. */
   protected final boolean free(GraphNode a, GraphNode b) {
      if (a.layer() > b.layer()) { GraphNode tmp=a; a=b; b=tmp; }
      Line2D.Double line = new Line2D.Double(a.x(), a.y(), b.x(), b.y());
      for(GraphNode n:nodes) if (n!=a && n!=b && a.layer()<n.layer() && n.layer()<b.layer() && n.shape()!=null) {
         if (line.intersects(n.getBoundingBox(10,10))) return false;
      }
      return true;
   }

   //============================================================================================================================//

   /** (Re-)perform the layout. */
   public abstract void layout();

   //============================================================================================================================//

   /** Re-establish top/left/width/height. */
   protected final void recalcBound(boolean fresh) {
      if (nodes.size()==0) { top=0; bottom=10; totalHeight=10; left=0; totalWidth=10; return; }
      if (fresh) { top=nodes.get(0).y()-nodes.get(0).getHeight()/2-5; bottom=nodes.get(0).y()+nodes.get(0).getHeight()/2+5; }
      // Find the leftmost and rightmost pixel
      int minX = nodes.get(0).x() - nodes.get(0).getWidth()/2 - 5;
      int maxX = nodes.get(0).x() + nodes.get(0).getWidth()/2 + nodes.get(0).getReserved() + 5;
      for(GraphNode n: nodes) {
         int min = n.x() - n.getWidth()/2                   - 5; if (minX>min) minX=min;
         int max = n.x() + n.getWidth()/2 + n.getReserved() + 5; if (maxX<max) maxX=max;
      }
      for(GraphEdge e: edges) if (e.getLabelW()>0 && e.getLabelH()>0) {
         int x1=e.getLabelX(), x2=x1+e.getLabelW()-1;
         if (minX>x1) minX=x1;
         if (maxX<x2) maxX=x2;
      }
      left = minX-20;
      totalWidth = maxX-minX+20;
      // Find the topmost and bottommost pixel
      for(int layer=layers()-1; layer>=0; layer--) {
         for(GraphNode n: layer(layer)) {
            int ytop = n.y()-n.getHeight()/2-5;    if (top > ytop) top=ytop;
            int ybottom = n.y()+n.getHeight()/2+5; if (bottom < ybottom) bottom=ybottom;
         }
      }
      totalHeight = bottom-top;
      int widestLegend = 0, legendHeight = 30;
      for(Pair<String,Color> e: legends.values()) {
         if (e.b==null) continue; // that means this legend is not visible
         int widthOfLegend = (int) getBounds(true, e.a).getWidth();
         if (widestLegend < widthOfLegend) widestLegend = widthOfLegend;
         legendHeight += ad;
      }
      if (widestLegend>0) {
         left -= (widestLegend+10);
         totalWidth += (widestLegend*2+10);
         if (totalHeight<legendHeight) { bottom=bottom+(legendHeight-totalHeight); totalHeight=legendHeight; }
      }
   }

   //============================================================================================================================//

   /** Assuming everything was laid out already, but at least one node just moved, this re-layouts ALL edges. */
   protected final void relayout_edges(boolean straighten) {
      // Move pairs of virtual nodes to straighten the lines if possible
      if (straighten) for(int i=0; i<5; i++) for(GraphNode n:nodes) if (n.shape()==null) {
         GraphEdge e1 = n.ins.get(0), e2 = n.outs.get(0);
         if (!free(e1.a(), e2.b())) continue;
         double slope = (e2.b().x()-e1.a().x()) / ((double)(e2.b().y()-e1.a().y()));
         double xx = (n.y()-e1.a().y())*slope + e1.a().x();
         n.setX((int)xx);
      }
      // Move the virtual nodes between endpoints to straighten the lines if possible
      if (straighten) for(GraphEdge e:edges) if (e.a().shape()!=null && e.b().shape()==null) {
         GraphNode a=e.a(), b;
         for(GraphEdge ee=e;;) {
            b=ee.b();
            if (b.shape()!=null) break;
            ee = b.outs.get(0);
         }
         if (!free(a,b)) continue;
         double slope = (b.x()-a.x()) / ((double)(b.y()-a.y()));
         for(GraphEdge ee=e;;) {
            b=ee.b();
            if (b.shape()!=null) break;
            double xx = (b.y()-a.y())*slope + a.x();
            b.setX((int)xx);
            ee = b.outs.get(0);
         }
      }
      // Now restore the invariant that nodes in each layer is ordered by x
      if (straighten) for(int i=0; i<layers(); i++) {
         sortLayer(i, new Comparator<GraphNode>() {
            public int compare(GraphNode o1, GraphNode o2) {
               if (o1.x()<o2.x()) return -1; else if (o1.x()>o2.x()) return 1;
               return 0;
            }
         });
         // Ensure that nodes are not bunched up together horizontally.
         List<GraphNode> layer=new ArrayList<GraphNode>(layer(i));
         for(int j=layer.size()/2; j>=0 && j<layer.size()-1; j++) {
            GraphNode a=layer.get(j), b=layer.get(j+1);
            int ax = a.shape()==null ? a.x() : (a.x()+a.getWidth()/2+a.getReserved());
            int bx = b.shape()==null ? b.x() : (b.x()-b.getWidth()/2);
            if (bx<=ax || bx-ax<5) b.setX(ax+5+b.getWidth()/2);
         }
         for(int j=layer.size()/2; j>0 && j<layer.size(); j--) {
            GraphNode a=layer.get(j-1), b=layer.get(j);
            int ax = a.shape()==null ? a.x() : (a.x()+a.getWidth()/2+a.getReserved());
            int bx = b.shape()==null ? b.x() : (b.x()-b.getWidth()/2);
            if (bx<=ax || bx-ax<5) a.setX(bx-5-a.getWidth()/2-a.getReserved());
         }
      }
      // Now layout the edges, initially as straight lines
      for(GraphEdge e:edges) e.resetPath();
      // Now, scan layer-by-layer to find edges that intersect nodes improperly, and bend them accordingly
      for(int layer=layers()-1; layer>0; layer--) {
         List<GraphNode> top=layer(layer), bottom=layer(layer-1);
         checkUpperCollision(top); checkLowerCollision(bottom); checkUpperCollision(top);
      }
      // Now, for each edge, adjust its arrowhead and label.
      AvailableSpace sp = new AvailableSpace();
      for(GraphNode n: nodes) if (n.shape()!=null) sp.add(n.x()-n.getWidth()/2, n.y()-n.getHeight()/2, n.getWidth()+n.getReserved(), n.getHeight());
      for(GraphEdge e: edges) { e.layout_arrowHead(); e.repositionLabel(sp); }
   }

   //============================================================================================================================//

   /** Assuming everything was laid out already, but nodes in layer[i] just moved horizontally, this re-layouts edges to+from layer i. */
   protected final void relayout_edges(int i) {
      if (nodes.size()==0) return; // The rest of the code assumes there is at least one node
      for(GraphNode n: layer(i)) for(GraphEdge e: n.selfs) { e.resetPath(); e.layout_arrowHead(); }
      if (i>0) {
         List<GraphNode> top=layer(i), bottom=layer(i-1);
         for(GraphNode n: top) for(GraphEdge e: n.outs) e.resetPath();
         checkUpperCollision(top); checkLowerCollision(bottom); checkUpperCollision(top);
      }
      if (i<layers()-1) {
         List<GraphNode> top=layer(i+1), bottom=layer(i);
         for(GraphNode n: top) for(GraphEdge e: n.outs) e.resetPath();
         checkUpperCollision(top); checkLowerCollision(bottom); checkUpperCollision(top);
      }
      // Now, for each edge, adjust its arrowhead and label.
      AvailableSpace sp = new AvailableSpace();
      for(GraphNode n:nodes) if (n.shape()!=null) sp.add(n.x()-n.getWidth()/2, n.y()-n.getHeight()/2, n.getWidth()+n.getReserved(), n.getHeight());
      for(GraphEdge e:edges) { e.layout_arrowHead(); e.repositionLabel(sp); }
   }

   //============================================================================================================================//

   /** Locates the node or edge at the given (X,Y) location. */
   public final Object find(double scale, int mouseX, int mouseY) {
      int h = getTop() + 10 - ad;
      double x = mouseX/scale + getLeft(), y = mouseY/scale + getTop();
      for(Map.Entry<Comparable<?>,Pair<String,Color>> e: legends.entrySet()) {
         if (e.getValue().b == null) continue;
         h = h + ad;
         if (y<h || y>=h+ad) continue;
         int w = (int) getBounds(true, e.getValue().a).getWidth();
         if (x>=getLeft()+10 && x<=getLeft()+10+w) return e.getKey();
      }
      for(GraphNode n: nodes) {
         if (n.shape()==null && Math.abs(n.x()-x)<10 && Math.abs(n.y()-y)<10) return n;
         if (n.contains(x,y)) return n;
      }
      for(GraphEdge e: edges) {
         if (e.a() != e.b()) {
            double dx;
            dx = e.path().getXatY(y, 0, 1, Double.NaN); if (!Double.isNaN(dx) && StrictMath.abs(x-dx)<12/scale) return e;
         } else {
            double dx;
            dx = e.path().getXatY(y, 0.25, 0.75, Double.NaN); if (!Double.isNaN(dx) && StrictMath.abs(x-dx)<12/scale) return e;
            dx = e.path().getXatY(y, 0,    0.25, Double.NaN); if (!Double.isNaN(dx) && StrictMath.abs(x-dx)<12/scale) return e;
            dx = e.path().getXatY(y, 0.75, 1,    Double.NaN); if (!Double.isNaN(dx) && StrictMath.abs(x-dx)<12/scale) return e;
         }
      }
      return null;
   }

   //============================================================================================================================//

   /** Assuming layout has been performed, this draws the graph with the given magnification scale. */
   protected final void draw(Artist gr, double scale, Object highlight, boolean showLegends) {
      if (nodes.size()==0) return; // The rest of this procedure assumes there is at least one node
      Object group = null;
      GraphNode highFirstNode = null, highLastNode = null;
      GraphEdge highFirstEdge = null, highLastEdge = null;
      if (highlight instanceof GraphEdge) {
         highFirstEdge = (GraphEdge)highlight;
         highLastEdge = highFirstEdge;
         group = highFirstEdge.group;
         while(highFirstEdge.a().shape() == null) highFirstEdge = highFirstEdge.a().ins.get(0);
         while(highLastEdge.b().shape() == null) highLastEdge = highLastEdge.b().outs.get(0);
         highFirstNode=highFirstEdge.a();
         highLastNode=highLastEdge.b();
      } else if (!(highlight instanceof GraphNode) && highlight!=null) {
         group = highlight;
      }
      // Since drawing an edge will automatically draw all segments if they're connected via dummy nodes,
      // we must make sure we only draw out edges from non-dummy-nodes
      int maxAscent = Artist.getMaxAscent();
      for(GraphNode n:nodes) if (n.shape()!=null) {
         for(GraphEdge e:n.outs)  if (e.group!=group) e.draw(gr, scale, highFirstEdge, group);
         for(GraphEdge e:n.selfs) if (e.group!=group) e.draw(gr, scale, highFirstEdge, group);
      }
      if (group!=null) {
         for(GraphNode n:nodes) if (n.shape()!=null) {
            for(GraphEdge e:n.outs)  if (e.group==group && e!=highFirstEdge) e.draw(gr, scale, highFirstEdge, group);
            for(GraphEdge e:n.selfs) if (e.group==group && e!=highFirstEdge) e.draw(gr, scale, highFirstEdge, group);
         }
         if (highFirstEdge!=null) highFirstEdge.draw(gr, scale, highFirstEdge, group);
      }
      for(GraphNode n:nodes) if (highFirstNode!=n && highLastNode!=n) n.draw(gr, scale, n==highlight);
      if (highFirstNode!=null) highFirstNode.draw(gr, scale, true);
      if (highLastNode!=null && highLastNode!=highFirstNode) highLastNode.draw(gr, scale, true);
      if (highFirstEdge!=null) highFirstEdge.drawLabel(gr, highFirstEdge.color(), new Color(255,255,255,160));
      // show legends?
      if (!showLegends || legends.size()==0) return;
      boolean groupFound=false;
      int y=0, maxWidth=0;
      for(Map.Entry<Comparable<?>,Pair<String,Color>> e:legends.entrySet()) {
         if (e.getValue().b==null) continue;
         if (group!=null && e.getKey()==group) groupFound=true;
         int w = (int) getBounds(true, e.getValue().a).getWidth();
         if (maxWidth<w) maxWidth=w;
         y = y + ad;
      }
      if (y==0) return; // This means no legends need to be drawn
      gr.setColor(Color.GRAY);
      gr.draw(new RoundRectangle2D.Double(5, 5, maxWidth+10, y+10, 5, 5), false);
      y=10;
      for(Map.Entry<Comparable<?>,Pair<String,Color>> e:legends.entrySet()) {
         Color color = e.getValue().b;
         if (color==null) continue;
         gr.setFont((groupFound && e.getKey()==group) || !groupFound);
         gr.setColor((!groupFound || e.getKey()==group) ? color : Color.GRAY);
         gr.drawString(e.getValue().a, 8, y+maxAscent);
         y = y + ad;
      }
   }

   //============================================================================================================================//

   /** Helper method that encodes a String for printing into a DOT file. */
   protected final static String esc(String name) {
      if (name.indexOf('\"') < 0) return name;
      StringBuilder out = new StringBuilder();
      for(int i=0; i<name.length(); i++) {
         char c=name.charAt(i);
         if (c=='\"') out.append('\\');
         out.append(c);
      }
      return out.toString();
   }

   //============================================================================================================================//

   /** Returns a DOT representation of this graph. */
   @Override public final String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("digraph \"graph\" {\n" + "graph [fontsize=12]\n" + "node [fontsize=12]\n" + "edge [fontsize=12]\n" + "rankdir=TB;\n");
      for (GraphEdge e: edges) sb.append(e);
      for (GraphNode n: nodes) sb.append(n);
      sb.append("}\n");
      return sb.toString();
   }
}
