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
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.Util;

/** Mutable; represents a graph.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public final strictfp class SugiyamaGraph extends Graph {

   //============================================================================================================================//

   /** Constructs an empty Graph object. */
   public SugiyamaGraph(double defaultScale)  { super(defaultScale); }

   //============================================================================================================================//

   /** Layout step #1: assign a total order on the nodes. */
   private void layout_assignOrder() {
      // This is an implementation of the GR algorithm described by Peter Eades, Xuemin Lin, and William F. Smyth
      // in "A Fast & Effective Heuristic for the Feedback Arc Set Problem"
      // in Information Processing Letters, Volume 47, Number 6, Pages 319-323, 1993
      final int num = nodes.size();
      if ((Integer.MAX_VALUE-1)/2 < num) throw new OutOfMemoryError();
      // Now, allocate 2n+1 bins labeled -n .. n
      // Note: inside this method, whenever we see #in and #out, we ignore repeated edges.
      // Note: since Java ArrayList always start at 0, we'll index it by adding "n" to it.
      final List<List<GraphNode>> bins = new ArrayList<List<GraphNode>>(2*num+1);
      for(int i=0; i<2*num+1; i++) bins.add(new LinkedList<GraphNode>());
      // For each N, figure out its in-neighbors and out-neighbors, then put it in the correct bin
      ArrayList<LinkedList<GraphNode>> grIN=new ArrayList<LinkedList<GraphNode>>(num);
      ArrayList<LinkedList<GraphNode>> grOUT=new ArrayList<LinkedList<GraphNode>>(num);
      int[] grBIN=new int[num];
      for(GraphNode n: nodes) {
         int ni = n.pos();
         LinkedList<GraphNode> in=new LinkedList<GraphNode>(), out=new LinkedList<GraphNode>();
         for(GraphEdge e: n.ins) { GraphNode a = e.a(); if (!in.contains(a)) in.add(a); }
         for(GraphEdge e: n.outs) { GraphNode b = e.b(); if (!out.contains(b)) out.add(b); }
         grIN.add(in);
         grOUT.add(out);
         grBIN[ni] = (out.size()==0) ? 0 : (in.size()==0 ? (2*num) : (out.size()-in.size()+num));
         bins.get(grBIN[ni]).add(n);
         // bin[0]     = { v | #out=0 }
         // bin[n + d] = { v | d=#out-#in and #out!=0 and #in!=0 } for -n < d < n
         // bin[n + n] = { v | #in=0 and #out>0 }
      }
      // Main loop
      final LinkedList<GraphNode> s1=new LinkedList<GraphNode>(), s2=new LinkedList<GraphNode>();
      while(true) {
         GraphNode x=null;
         if (!bins.get(0).isEmpty()) {
            // If a sink exists, take a sink X and prepend X to S2
            x = bins.get(0).remove(bins.get(0).size()-1);
            s1.add(x);
         } else for(int j=2*num; j>0; j--) {
            // Otherwise, let x be a source if one exists, or a node with the highest #out-#in. Then append X to S1.
            List<GraphNode> bin=bins.get(j);
            int sz=bin.size();
            if (sz>0) { x=bin.remove(sz-1); s2.addFirst(x); break; }
         }
         if (x==null) break; // This means we're done; else, delete X from its bin, and move each of X's neighbor into their new bin
         bins.get(grBIN[x.pos()]).remove(x);
         for(GraphNode n:grIN.get(x.pos()))  grOUT.get(n.pos()).remove(x);
         for(GraphNode n:grOUT.get(x.pos())) grIN.get(n.pos()).remove(x);
         for(GraphNode n:Util.fastJoin(grIN.get(x.pos()), grOUT.get(x.pos()))) {
            int ni=n.pos(), out=grOUT.get(ni).size(), in=grIN.get(ni).size();
            int b=(out==0)?0:(in==0?(2*num):(out-in+num));
            if (grBIN[ni]!=b) { bins.get(grBIN[ni]).remove(n); grBIN[ni]=b; bins.get(b).add(n); }
         }
      }
      sortNodes(Util.fastJoin(s1,s2));
   }

   //============================================================================================================================//

   /** Layout step #2: reverses all backward edges. */
   private void layout_backEdges() {
      for(GraphEdge e: edges) if (e.a().pos() < e.b().pos()) e.set(e.bhead(), e.ahead()).reverse();
   }

   //============================================================================================================================//

   /** Layout step #3: assign the nodes into one or more layers, then return the number of layers. */
   private int layout_decideLayer() {
      // Here, for each node X, I compute its maximum length to a sink; if X is a sink, its length to sink is 0.
      final int n = nodes.size();
      int[] len = new int[n];
      for(GraphNode x: nodes) {
         // Since we ensured that arrows only ever go from a node with bigger pos() to a node with smaller pos(),
         // we can compute the "len" array in O(n) time by visiting each node IN THE SORTED ORDER
         int max=0;
         for(GraphEdge e: x.outs) {
            GraphNode y = e.b();
            int yLen = len[y.pos()]+1;
            if (max < yLen) max = yLen;
         }
         len[x.pos()] = max;
      }
      // Now, we simply do the simplest thing: assign each node to the layer corresponding to its max-length-to-sink.
      for(GraphNode x: nodes) x.setLayer(len[x.pos()]);
      // Now, apply a simple trick: whenever every one of X's incoming edge is more than one layer above, then move X up
      while(true) {
         boolean changed = false;
         for(GraphNode x: nodes) if (x.ins.size() > 0) {
            int closestLayer=layers()+1;
            for(GraphEdge e: x.ins) {
               int y = e.a().layer();
               if (closestLayer>y) closestLayer=y;
            }
            if (closestLayer-1>x.layer()) { x.setLayer(closestLayer-1); changed=true; }
         }
         if (!changed) break;
      }
      // All done!
      return layers();
   }

   //============================================================================================================================//

   /** Layout step #4: add dummy nodes so that each edge only goes between adjacent layers. */
   private void layout_dummyNodesIfNeeded() {
      for(final GraphEdge edge: new ArrayList<GraphEdge>(edges)) {
         GraphEdge e = edge;
         GraphNode a = e.a(), b=e.b();
         while(a.layer() - b.layer() > 1) {
            GraphNode tmp = a;
            a = new GraphNode(a.graph, e.uuid).set((DotShape)null);
            a.setLayer(tmp.layer()-1);
            // now we have three nodes in the vertical order of "tmp", "a", then "b"
            e.change(a);                                                                           // let old edge go from "tmp" to "a"
            e = new GraphEdge(a, b, e.uuid, "", e.ahead(), e.bhead(), e.style(), e.color(), e.group); // let new edge go from "a" to "b"
         }
      }
   }

   //============================================================================================================================//

   /** Layout step #5: decide the order of the nodes within each layer. */
   private void layout_reorderPerLayer() {
      // This uses the original Barycenter heuristic
      final IdentityHashMap<GraphNode,Object> map = new IdentityHashMap<GraphNode,Object>();
      final double[] bc = new double[nodes.size()+1];
      int i=1; for(GraphNode n:layer(0)) { bc[n.pos()] = i; i++; }
      for(int layer=0; layer<layers()-1; layer++) {
         for(GraphNode n:layer(layer+1)) {
            map.clear();
            int count = 0;
            double sum = 0;
            for(GraphEdge e: n.outs) {
               GraphNode nn=e.b();
               if (map.put(nn,nn)==null) { count++; sum += bc[nn.pos()]; }
            }
            bc[n.pos()] = count==0 ? 0 : (sum/count);
         }
         sortLayer(layer+1, new Comparator<GraphNode>() {
            public int compare(GraphNode o1, GraphNode o2) {
               // If the two nodes have the same barycenter, we use their ordering that was established during layout_assignOrder()
               if (o1==o2) return 0;
               int n = Double.compare(bc[o1.pos()], bc[o2.pos()]); if (n!=0) return n; else if (o1.pos()<o2.pos()) return -1; else return 1;
            }
         });
         int j=1; for(GraphNode n:layer(layer+1)) { bc[n.pos()]=j; j++; }
      }
   }

   //============================================================================================================================//

   /** Layout step #6: decide the exact X position of each component. */
   private void layout_xAssignment(List<GraphNode> nodes) {
      // This implementation uses the iterative approach described in the paper "Layout of Bayesian Networks"
      // by Kim Marriott, Peter Moulder, Lucas Hope, and Charles Twardy
      final int n = nodes.size();
      if (n==0) return;
      final Block[] block = new Block[n+1];
      block[0] = new Block(); // The sentinel block
      for(int i=1; i<=n; i++) {
         Block b = new Block(nodes.get(i-1), i);
         block[i] = b;
         while(block[b.first-1].posn + block[b.first-1].width > b.posn) {
            b = new Block(block[b.first-1], b);
            block[b.last] = b;
            block[b.first] = b;
         }
      }
      int i=1;
      while(true) {
         Block b = block[i];
         double tmp = b.posn + (nodes.get(b.first-1).getWidth() + nodes.get(b.first-1).getReserved() + xJump)/2D;
         nodes.get(i-1).setX((int)tmp);
         for(i=i+1; i<=b.last; i++) {
            GraphNode v1 = nodes.get(i-1);
            GraphNode v2 = nodes.get(i-2);
            int xsep = (v1.getWidth() + v1.getReserved() + v2.getWidth() + v2.getReserved())/2 + xJump;
            v1.setX(v2.x() + xsep);
         }
         i=b.last+1;
         if (i>n) break;
      }
   }

   /** This computes the des() value as described in the paper.
    * <p> The desired location of V = ("sum e:in(V) | wt(e) * phi(start of e)" + "sum e:out(V) | wt(e) * phi(end of e)") / wt(v)
    */
   private static double des(GraphNode n) {
      int wt = wt(n);
      if (wt==0) return 0; // This means it has no "in" edges and no "out" edges
      double ans=0;
      for(GraphEdge e: n.ins)  ans += ((double)e.weight()) * e.a().x();
      for(GraphEdge e: n.outs) ans += ((double)e.weight()) * e.b().x();
      return ans/wt;
   }

   /** This computes the wt() value as described in the paper.
    * <p> The weight of a node is the sum of the weights of its in-edges and out-edges.
    */
   private static int wt(GraphNode n) {
      int ans=0;
      for(GraphEdge e: n.ins) ans += e.weight();
      for(GraphEdge e: n.outs) ans += e.weight();
      return ans;
   }

   /** This corresponds to the Block structure described in the paper. */
   private static final class Block {
      /** These fields are described in the paper. */
      private final int first, last, weight;
      /** These fields are described in the paper. */
      private final double width, posn, wposn;
      /** This constructs a regular block. */
      public Block(GraphNode v, int i) {
         first=i; last=i; width=v.getWidth()+v.getReserved()+xJump; posn=des(v)-(width/2); weight=wt(v); wposn=weight*posn;
      }
      /** This merges the two existing blocks into a new block. */
      public Block(Block a, Block b) {
         first=a.first; last=b.last; width=a.width+b.width;
         wposn=a.wposn+b.wposn-a.width*b.weight;  weight=a.weight+b.weight;  posn=wposn/weight;
      }
      /** This constructs a sentinel block. */
      public Block() {
         posn=Double.NEGATIVE_INFINITY; first=0; last=0; weight=0; width=0; wposn=0;
      }
   }

   //============================================================================================================================//

   /** (Re-)perform the layout. */
   public void layout() {

      // The rest of the code below assumes at least one node, so we return right away if nodes.size()==0
      if (nodes.size()==0) return;

      // Calculate each node's width and height
      for(GraphNode n:nodes) n.calcBounds();

      // Layout the nodes
      layout_assignOrder();
      layout_backEdges();
      final int layers = layout_decideLayer();
      layout_dummyNodesIfNeeded();
      layout_reorderPerLayer();

      // For each layer, this array stores the height of its tallest node
      layerPH = new int[layers];

      // figure out the Y position of each layer, and also give each component an initial X position
      for(int layer=layers-1; layer>=0; layer--) {
         int x=5; // So that we're not touching the left-edge of the window
         int h=0;
         for(GraphNode n: layer(layer)) {
            int nHeight = n.getHeight(), nWidth = n.getWidth();
            n.setX(x + nWidth/2);
            if (h < nHeight) h = nHeight;
            x = x + nWidth + n.getReserved() + 20;
         }
         layerPH[layer] = h;
      }

      // If there are more than one layer, then iteratively refine the X position of each component 3 times; 4 is a good number
      if (layers>1) {
         // It's important to NOT DO THIS when layers<=1, because without edges the nodes will overlap each other into the center
         for(int i=0; i<3; i++) for(int layer=0; layer<layers; layer++) layout_xAssignment(layer(layer));
      }

      // Calculate each node's y; we start at y==5 so that we're not touching the top-edge of the window
      int py=5;
      for(int layer=layers-1; layer>=0; layer--) {
         final int ph = layerPH[layer];
         for(GraphNode n:layer(layer)) n.setY(py + ph/2);
         py = py + ph + yJump;
      }

      relayout_edges(true);

      // Since we're doing layout for the first time, we need to explicitly set top and bottom, since
      // otherwise "recalcBound" will merely "extend top and bottom" as needed.
      recalcBound(true);
   }

}
