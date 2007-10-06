/*
 * Alloy Analyzer
 * Copyright (c) 2007 Massachusetts Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA,
 * 02110-1301, USA
 */

package edu.mit.csail.sdg.alloy4graph;

import static java.awt.Color.WHITE;
import static java.awt.Color.BLACK;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import edu.mit.csail.sdg.alloy4.OurDialog;
import edu.mit.csail.sdg.alloy4.OurImageUtil;
import edu.mit.csail.sdg.alloy4.Util;

/**
 * This class displays the graph.
 *
 * <p><b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public final class VizViewer extends JPanel {

    /** This silences javac's warning about missing serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The graph that we are displaying. */
    private final VizGraph graph;

    /** The current amount of zoom. */
    private double scale = 1d;

    /** The currently highlighted VizNode or VizEdge, or null if there is none. */
    private Object highlight = null;

    /** The right-click context menu associated with this JPanel. */
    private final JPopupMenu pop = new JPopupMenu();

    /** This allows users to attach a String object to this JPanel. */
    private String annotation = "";

    /** Construct a VizViewer that displays the given graph. */
    public VizViewer(final VizGraph graph) {
        setOpaque(true);
        setBackground(WHITE);
        setBorder(new EmptyBorder(0,0,0,0));
        this.graph = graph;
        graph.layout();
        final JMenuItem zoomIn = new JMenuItem("Zoom In");
        final JMenuItem zoomOut = new JMenuItem("Zoom Out");
        final JMenuItem zoom100 = new JMenuItem("Zoom to 100%");
        final JMenuItem zoomToFit = new JMenuItem("Zoom to Fit");
        final JMenuItem print = new JMenuItem("Export to PNG");
        pop.add(zoomIn);
        pop.add(zoomOut);
        pop.add(zoom100);
        pop.add(zoomToFit);
        pop.add(print);
        ActionListener act = new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              Container c=getParent();
              while(c!=null) { if (c instanceof JViewport) break; else c=c.getParent(); }
              if (e.getSource() == print) do_saveAsPNG();
              if (e.getSource() == zoom100) scale=1d;
              if (e.getSource() == zoomIn) { scale=scale*1.33d; if (!(scale<500d)) scale=500d; }
              if (e.getSource() == zoomOut) { scale=scale/1.33d; if (!(scale>0.1d)) scale=0.1d; }
              if (e.getSource() == zoomToFit) {
                 if (c==null) return;
                 int w=c.getWidth()-15, h=c.getHeight()-15; // 15 gives a comfortable round-off margin
                 if (w<=0 || h<=0) return;
                 double scale1 = ((double)w)/graph.totalWidth, scale2 = ((double)h)/graph.totalHeight;
                 if (scale1<scale2) scale=scale1; else scale=scale2;
              }
              setSize((int)(graph.totalWidth*scale), (int)(graph.totalHeight*scale));
              if (c!=null) { c.invalidate(); c.repaint(); c.validate(); } else { invalidate(); repaint(); validate(); }
           }
        };
        zoomIn.addActionListener(act);
        zoomOut.addActionListener(act);
        zoom100.addActionListener(act);
        zoomToFit.addActionListener(act);
        print.addActionListener(act);
        addMouseMotionListener(new MouseMotionAdapter() {
           @Override public void mouseMoved(MouseEvent ev) {
              double x=ev.getX()/scale, y=ev.getY()/scale;
              for(VizNode n:graph.nodes) if (n.intersects(x,y)) {
                 if (highlight!=n) { highlight=n; invalidate(); repaint(); validate(); }
                 return;
              }
              for(VizEdge e:graph.edges) if (e.intersects(x,y,scale)) {
                 if (highlight!=e) { highlight=e; invalidate(); repaint(); validate(); }
                 return;
              }
              if (highlight!=null) { highlight=null; invalidate(); repaint(); validate(); }
           }
        });
        addMouseListener(new MouseAdapter() {
           @Override public void mousePressed(MouseEvent ev) {
               if (ev.getButton()==MouseEvent.BUTTON3) {
                   pop.show(VizViewer.this, ev.getX(), ev.getY());
               } else if (ev.getButton()==MouseEvent.BUTTON1 && ev.isControlDown()) {
                   // This lets Ctrl+LeftClick bring up the popup menu, just like RightClick,
                   // since many Mac mouses do not have a right button.
                   pop.show(VizViewer.this, ev.getX(), ev.getY());
               }
           }
           @Override public void mouseExited(MouseEvent ev) {
               if (highlight!=null) { highlight=null; invalidate(); repaint(); validate(); }
           }
        });
    }

    /** Retrieves the annotation associated with this object; "" if no annotation has been set yet. */
    public String do_getAnnotation() { return annotation; }

    /** Changes the annotation associated with this object. */
    public void do_setAnnotation(String newAnnotation) { this.annotation=newAnnotation; }

    /** This stores the most recent DPI value. */
    private static int oldDPI=72;

    /** Export the current drawing as a PNG file by asking the user for the filename and the image resolution. */
    public void do_saveAsPNG() {
       // Find the enclosing JFrame if such a JFrame exists
       JFrame me=null;
       for(Container c=getParent(); c!=null; c=c.getParent()) if (c instanceof JFrame) { me=(JFrame)c; break; }
       // Create the editable and noneditable fields
       int newDPI;
       synchronized(VizViewer.class) { newDPI=oldDPI; }
       final JLabel w = new JLabel("Width  : "+graph.totalWidth+" pixels ("+(graph.totalWidth*100/oldDPI)/100D+" inch)");
       final JLabel h = new JLabel("Height : "+graph.totalHeight+" pixels ("+(graph.totalHeight*100/oldDPI)/100D+" inch)");
       final JTextField dpi = new JTextField(""+oldDPI);
       dpi.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent e) {
             int newDPI=(-1);
             try { newDPI=Integer.parseInt(dpi.getText()); } catch(NumberFormatException ex) { newDPI=(-1); }
             if (newDPI<=0) {
                w.setText("<html><font color=red>DPI must be a positive integer.</font></html>");
                h.setText(" ");
                return;
             }
             w.setText("Width  : "+graph.totalWidth+" pixels ("+(graph.totalWidth*100/newDPI)/100D+" inch)");
             h.setText("Height : "+graph.totalHeight+" pixels ("+(graph.totalHeight*100/newDPI)/100D+" inch)");
          }
          public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
          public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
       });
       if (!OurDialog.getInput(me, "Export as PNG", new Object[]{"Please specify the DPI (dots per inch):", dpi, w, h})) return;
       try { newDPI=Integer.parseInt(dpi.getText()); } catch(NumberFormatException ex) { newDPI=(-1); }
       if (newDPI<=0) { OurDialog.alert(me, "The DPI must be a positive integer.", "Error exporting to PNG"); return; }
       // Now, let's ask for a filename
       File filename = OurDialog.askFile(me, false, null, ".png", "PNG file");
       if (filename==null) return;
       synchronized(VizViewer.class) { oldDPI=newDPI; }
       try {
          do_saveAsPNG(filename.getAbsolutePath(), newDPI, newDPI);
          Util.setCurrentDirectory(filename.getParentFile());
       } catch(IOException ex) {
          OurDialog.alert(me, "An error has occured in writing the PNG file.", "Error");
       }
    }

    /** Export the current drawing as a PNG file with the given file name and image resolution. */
    public void do_saveAsPNG(String filename, int dpiX, int dpiY) throws IOException {
       int width = (int) (graph.totalWidth*scale);   if (width<10) width=10;
       int height = (int) (graph.totalHeight*scale); if (height<10) height=10;
       BufferedImage bf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
       Graphics2D gr = (Graphics2D) (bf.getGraphics());
       gr.setColor(WHITE);
       gr.fillRect(0, 0, width, height);
       gr.setColor(BLACK);
       gr.scale(scale,scale);
       gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       graph.draw(gr, scale, null);
       OurImageUtil.writePNG(bf, filename, dpiX, dpiY);
    }

    /** Show the popup menu at location (x,y) */
    public void do_popup(Component c, int x, int y) {
       pop.show(c,x,y);
    }

    /** {@inheritDoc} */
    @Override public Dimension getPreferredSize() {
        return new Dimension((int)(graph.totalWidth*scale), (int)(graph.totalHeight*scale));
    }

    /** {@inheritDoc} */
    @Override public void paintComponent(final Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g2 = (Graphics2D)gr;
        AffineTransform oldAF = (AffineTransform) (g2.getTransform().clone());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.scale(scale, scale);
        graph.draw(g2, scale, highlight);
        g2.setTransform(oldAF);
    }
}