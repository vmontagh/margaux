package edu.mit.csail.sdg.alloy4.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import kodkod.AlloyBridge;
import edu.mit.csail.sdg.alloy4.helper.Err;
import edu.mit.csail.sdg.alloy4.helper.Log;
import edu.mit.csail.sdg.alloy4.helper.LogToJTextPane;
import edu.mit.csail.sdg.alloy4.node.ParaRuncheck;
import edu.mit.csail.sdg.alloy4.node.ParaSig;
import edu.mit.csail.sdg.alloy4.node.Unit;
import edu.mit.csail.sdg.alloy4.node.VisitTypechecker;
import edu.mit.csail.sdg.alloy4.parser.AlloyParser;
import edu.mit.csail.sdg.alloy4.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4.translator.TranslateAlloyToKodkod.SolverChoice;
import edu.mit.csail.sdg.alloy4.translator.TranslateAlloyToKodkod.Verbosity;
import edu.mit.csail.sdg.alloy4.translator.ViaPipe;
import edu.mit.csail.sdg.alloy4util.Func0;
import edu.mit.csail.sdg.alloy4util.Func1;
import edu.mit.csail.sdg.alloy4util.OurBorder;
import edu.mit.csail.sdg.alloy4util.OurDialog;
import edu.mit.csail.sdg.alloy4util.MacUtil;
import edu.mit.csail.sdg.alloy4util.OurMenu;
import edu.mit.csail.sdg.alloy4util.OurMenuItem;
import edu.mit.csail.sdg.alloy4util.OurSplitPane;
import edu.mit.csail.sdg.alloy4util.OurUtil;
import edu.mit.csail.sdg.alloy4util.Pref;
import edu.mit.csail.sdg.alloy4util.Util;
import edu.mit.csail.sdg.alloy4util.Version;
import edu.mit.csail.sdg.alloy4viz.gui.KodVizGUI;

/**
 * The GUI; except noted below, methods in this class can only be called by the AWT thread.
 *
 * <p/> The methods that might get called from other threads are:
 * <br/> (1) the highlight() method.
 * <br/> (2) the run() method in Runner is launched from a fresh thread
 * <br/> (3) the run() method in the instance watcher (in constructor) is launched from a fresh thread
 *
 * <p/> As a result, exactly 3 fields must be guarded by a mutex: current_thread, latestInstance, and satOPTION.
 *
 * @author Felix Chang
 */

public final class SimpleGUI {

    //====== GUI components; only the AWT thread may access these. ==========//

    /** The JFrame for the main window. */
    private final JFrame frame;

    /** The JFrame for the visualizer window; null if it hasn't been opened yet. */
    private KodVizGUI viz=null;

    /** The "File", "Edit", "Run", "Option", "Window", and "Help" menus. */
    private final OurMenu filemenu, editmenu, runmenu, optmenu, windowmenu, windowmenu2, helpmenu;

    /** The toolbar. */
    private final JToolBar toolbar;

    /** The various toolbar buttons. */
    private final JButton newbutton, openbutton, loadbutton, savebutton, runbutton, stopbutton, showbutton;

    /** The Splitpane. */
    private final OurSplitPane splitpane;

    /** The JLabel that displays the current line/column position, etc. */
    private final JLabel status;

    /** Whether the editor has the focus, or the log window has the focus. */
    private boolean lastFocusIsOnEditor=true;

    /** The JTextArea containing the editor buffer. */
    private final JTextArea text;

    /** The UndoManager for the editor buffer. */
    private final UndoManager undo = new UndoManager();

    /** The Highlighter to use with the editor buffer. */
    private final Highlighter highlighter;

    /** The HighlightPainter to use to paint the highlights. */
    private final HighlightPainter highlightPainter;

    /** The JTextPane containing the error messages and success messages. */
    private final LogToJTextPane log;
    private final JScrollPane logpane;

    /** The filename for the content currently in the text editor. ("" if the text editor is unnamed) */
    private String latestName="";
    private List<String> openfiles=new ArrayList<String>();

    /** The most-recently-used directory (this is the directory we use when launching a FileChooser next time) */
    private String fileOpenDirectory=(new File(System.getProperty("user.home"))).getAbsolutePath();

    /** The last "find" that the user issued. */
    private String lastFind="";

    /** The latest command executed by the user. */
    private int latestCommand=0;

    /** This field is true iff the text in the text buffer hasn't been modified since the last time it was compiled */
    private Unit compiled=null;

    /** This field is true iff the text in the text buffer hasn't been modified since the last time it was saved */
    private boolean modified=false;

    //====== static readonly fields =========================================//

    /** The icon for a "checked" menu item. */
    private static final ImageIcon iconYes=OurUtil.loadIcon("images/menu1.gif");

    /** The icon for an "unchecked" menu item. */
    private static final ImageIcon iconNo=OurUtil.loadIcon("images/menu0.gif");

    /** The system-specific file separator (forward-slash on UNIX, back-slash on Windows, etc.) */
    private static final String fs=System.getProperty("file.separator");

    /** The darker background color (for the MessageLog window and the Toolbar and the Status Bar, etc.) */
    private static final Color background=Color.WHITE;

    //====== static methods =================================================//

    /** Constrain the font size to a reasonable number. */
    private static int boundFontSize(int n, int min, int max) {
        if (n<10) n=12; // Just pick a reasonable default value if the key didn't exist
        if (n<min) return min;
        if (n>max) return max;
        return n;
    }

    /** Remove the directory part from a pathname (for example, on UNIX, shortFileName("/abc/x") returns "x") */
    public static String shortFileName(String filename) {
        int index=filename.lastIndexOf(File.separatorChar);
        if (index>=0) return filename.substring(index+1); else return filename;
    }

    //====== synchronized fields (you must lock SimpleGUI.this before accessing them) =======//

    //=== other fields===

    /**
     * If it's not "", then it is the first part of the filename for the latest instance.
     * <p/> In particular, latestInstance+".dot" is the DOT file, and latestInstance+".xml" is the XML file.
     */
    private String latestInstance="";

    /** Read the latest instance. */
    private synchronized String getLatestInstance() { return latestInstance; }

    /** Sets the latest instance. */
    private synchronized void setLatestInstance(String value) { latestInstance=value; }

    /** The separate thread that is running the SAT solver (null if there is no SAT solver currently running) */
    private Thread current_thread=null;

    /** Read the current SAT solver thread. */
    private synchronized Thread getCurrentThread() { return current_thread; }

    /** Sets the current SAT solver thread. */
    private synchronized void setCurrentThread(Thread value) { current_thread=value; }


    //====== helper methods =================================================//

    /** Inserts "filename" as into the "recently opened file list". */
    private void addHistory(String filename) {
        String name0=Pref.get("history0"), name1=Pref.get("history1"), name2=Pref.get("history2");
        if (name0.equals(filename)) return; else {Pref.set("history0",filename); Pref.set("history1",name0);}
        if (name1.equals(filename)) return; else Pref.set("history2",name1);
        if (name2.equals(filename)) return; else Pref.set("history3",name2);
    }

    /** Updates the status bar at the bottom of the screen. */
    private void updateStatusBar() {
        if (Util.onMac()) {
            if (modified) frame.getRootPane().putClientProperty("windowModified",Boolean.TRUE);
            else frame.getRootPane().putClientProperty("windowModified",Boolean.FALSE);
        }
        if (mode_externalEditor) {
            if (latestName.length()>0) status.setText("<html><b>&nbsp; Current file:&nbsp; "+latestName+"</b></html>");
            else status.setText("<html><b>&nbsp; Current file:&nbsp; None</b></html>");
            return;
        }
        try {
            int c=text.getCaretPosition();
            int y=text.getLineOfOffset(c)+1;
            int x=c-text.getLineStartOffset(y-1)+1;
            status.setText("<html>&nbsp; Line "+y+", Column "+x+(modified?" <b style=\"color:red;\">[modified]</b></html>":"</html>"));
        } catch(BadLocationException ex) {
            status.setText("<html>&nbsp; Line ?, Column ?"+(modified?" <b style=\"color:red;\">[modified]</b></html>":"</html>"));
        }
    }

    /**
     * If the text editor content is not modified since the last save, return true; otherwise, ask the user.
     * <p/> If the user says to save it, we will attempt to save it, then return true iff the save succeeded.
     * <p/> If the user says to discard it, this method returns true.
     * <p/> If the user says to cancel it, this method returns false.
     */
    private boolean my_confirm() {
        log.clearError();
        if (!modified) return true;
        Boolean ans=OurDialog.questionSaveDiscardCancel(frame,"The current Alloy model");
        if (ans==null) return false;
        if (!ans.booleanValue()) return true; else return a_save.run();
    }

    private void highlight(final Err e) {
        if (!SwingUtilities.isEventDispatchThread()) {
            OurUtil.invokeAndWait(new Runnable() { public final void run() { highlight(e); }});
            return;
        }
        if (e.pos!=null && e.pos.y>0 && e.pos.x>0) try {
            int c=text.getLineStartOffset(e.pos.y-1)+e.pos.x-1;
            highlighter.removeAllHighlights();
            highlighter.addHighlight(c, c+1, highlightPainter);
            text.setSelectionStart(c); text.setSelectionEnd(c); text.requestFocusInWindow();
        } catch(BadLocationException ex) { Util.harmless("highlight()",ex); }
    }

    //====== Message handlers ===============================================//

    /** Instance list */
    private Map<String,String> xml2title = new LinkedHashMap<String,String>();
    private List<String> xmlLoaded = new ArrayList<String>();
    private String xmlLatest = "";

    /** Called when we need to load a visualization window. */
    private final Func1 a_viz = new Func1() {
        public final boolean run(final String xmlName) {
            xmlLatest=xmlName;
            if (!xmlLoaded.contains(xmlName)) xmlLoaded.add(xmlName);
            if (viz!=null) {
                viz.instantiate(xmlName,false);
                viz.setVisible(true);
                if ((viz.getExtendedState() & JFrame.MAXIMIZED_BOTH)!=JFrame.MAXIMIZED_BOTH)
                    viz.setExtendedState(JFrame.NORMAL);
                viz.requestFocus();
                viz.toFront();
                return true;
            }
            if (!xmlName.endsWith(".xml")) return false;
            String dotname=xmlName.substring(0, xmlName.length()-4)+".dot";
            viz=new KodVizGUI(dotname, xmlName, windowmenu2, a_close2);
            return true;
        }
    };

    private static String slightlyShorterFilename(String name) {
        if (name.toLowerCase().endsWith(".als")) {
            int i=name.lastIndexOf(File.separatorChar);
            if (i>=0) return name.substring(i+1);
        } else if (name.toLowerCase().endsWith(".xml")) {
            int i=name.lastIndexOf(File.separatorChar);
            if (i>0) i=name.lastIndexOf(File.separatorChar, i-1);
            if (i>=0) return name.substring(i+1);
        }
        return name;
    }

    private final Func0 a_window = new Func0() {
        public final boolean run() {
            windowmenu.removeAll();
            new OurMenuItem(windowmenu, "Minimize", KeyEvent.VK_M, KeyEvent.VK_M, new Func0() {
                public final boolean run() {
                    frame.setExtendedState(JFrame.ICONIFIED);
                    return true;
                }
            });
            new OurMenuItem(windowmenu, "Zoom", new Func0() {
                public final boolean run() {
                    if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH)==JFrame.MAXIMIZED_BOTH)
                        frame.setExtendedState(JFrame.NORMAL);
                    else
                        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    return true;
                }
            });
            JMenuItem it;
            windowmenu.addSeparator();
            if (openfiles.size()==0) {
                it=new JMenuItem("Alloy Analyzer");
                it.setIcon(iconYes);
                windowmenu.add(it);
            } else for(final String f:openfiles) {
                it=new JMenuItem("Model: "+slightlyShorterFilename(f));
                it.setIcon(f.equals(latestName)?iconYes:iconNo);
                it.addActionListener(new ActionListener() {
                    public final void actionPerformed(ActionEvent e) { a_openFileIfOk.run(f); }
                });
                windowmenu.add(it);
            }
            if (xmlLoaded.size()>0) {
                windowmenu.addSeparator();
                for(final String f:xmlLoaded) {
                    it=new JMenuItem("Instance: "+xml2title.get(f));
                    it.setIcon(iconNo);
                    it.addActionListener(new ActionListener() {
                        public final void actionPerformed(ActionEvent e) { a_viz.run(f); }
                    });
                    windowmenu.add(it);
                }
            }
            return true;
        }
    };

    private final Func0 a_window2 = new Func0() {
        public final boolean run() {
            windowmenu2.removeAll();
            new OurMenuItem(windowmenu2, "Minimize", KeyEvent.VK_M, KeyEvent.VK_M, new Func0() {
                public final boolean run() {
                    viz.setExtendedState(JFrame.ICONIFIED);
                    return true;
                }
            });
            new OurMenuItem(windowmenu2, "Zoom", new Func0() {
                public final boolean run() {
                    if ((viz.getExtendedState() & JFrame.MAXIMIZED_BOTH)==JFrame.MAXIMIZED_BOTH)
                        viz.setExtendedState(JFrame.NORMAL);
                    else
                        viz.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    return true;
                }
            });
            JMenuItem it;
            windowmenu2.addSeparator();
            if (openfiles.size()==0) {
                it=new JMenuItem("Alloy Analyzer");
                it.setIcon(iconNo);
                it.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { a_show.run(); }
                });
                windowmenu2.add(it);
            } else for(final String f:openfiles) {
                it=new JMenuItem("Model: "+slightlyShorterFilename(f));
                it.setIcon(iconNo);
                it.addActionListener(new ActionListener() {
                    public final void actionPerformed(ActionEvent e) { a_openFileIfOk.run(f); }
                });
                windowmenu2.add(it);
            }
            if (xmlLoaded.size()>0) {
                windowmenu2.addSeparator();
                for(final String f:xmlLoaded) {
                    it=new JMenuItem("Instance: "+xml2title.get(f));
                    it.setIcon(f.equals(xmlLatest)?iconYes:iconNo);
                    it.addActionListener(new ActionListener() {
                        public final void actionPerformed(ActionEvent e) { a_viz.run(f); }
                    });
                    windowmenu2.add(it);
                }
            }
            return true;
        }
    };

    /** Called when the user expands the "File" menu; always returns true. */
    private final Func0 a_file = new Func0() {
        public final boolean run() {
            JMenu recentmenu;
            String open=(mode_externalEditor?"Load":"Open");
            filemenu.removeAll();
            filemenu.addMenuItem(null, "New",                   true,KeyEvent.VK_N,KeyEvent.VK_N,a_new);
            filemenu.addMenuItem(null, open+"...",               true,KeyEvent.VK_O,KeyEvent.VK_O,a_open);
            filemenu.addMenuItem(null, open+" Sample Models...", true,KeyEvent.VK_B,-1,           a_openBuiltin);
            filemenu.add(recentmenu = new JMenu(open+" Recent"));
            if (!mode_externalEditor) {
                filemenu.addMenuItem(null, "Save",                  true,KeyEvent.VK_S,KeyEvent.VK_S,a_save);
                if (Util.onMac())
                    new OurMenuItem(filemenu,"Save As...",KeyEvent.VK_S, a_saveAs);
                else
                    new OurMenuItem(filemenu,"Save As...",KeyEvent.VK_A, -1, a_saveAs);
            }
            JMenuItem closemenu=filemenu.addMenuItem(null, "Close",       true,KeyEvent.VK_W,KeyEvent.VK_W,a_close);
            filemenu.addMenuItem(null, "Quit",true,KeyEvent.VK_Q,(Util.onMac()?-1:KeyEvent.VK_Q),a_quit);
            //
            boolean found=false;
            closemenu.setEnabled(latestName.length()>0 || text.getDocument().getLength()>0);
            recentmenu.removeAll();
            for(int i=0; i<=3; i++) {
                final String name = Pref.get("history"+i);
                if (name.length()==0) continue;
                found=true;
                JMenuItem x=new JMenuItem(name);
                x.addActionListener(new ActionListener() {
                    public final void actionPerformed(ActionEvent e) {a_show.run(); a_openFileIfOk.run(name);}
                });
                recentmenu.add(x);
            }
            recentmenu.addSeparator();
            JMenuItem y=new JMenuItem("Clear Menu");
            y.addActionListener(new ActionListener() {
                public final void actionPerformed(ActionEvent e) {
                    Pref.set("history0",""); Pref.set("history1",""); Pref.set("history2",""); Pref.set("history3","");
                    openfiles.clear();
                    if (latestName.length()>0) openfiles.add(latestName);
                }
            });
            recentmenu.add(y);
            recentmenu.setEnabled(found);
            return true;
        }
    };

    /** Called when the user clicks "File", then "New"; always returns true. */
    private final Func0 a_new = new Func0() {
        public final boolean run() {
            if (!my_confirm()) return false;
            a_show.run();
            latestName="";
            highlighter.removeAllHighlights();
            text.setText("");
            undo.discardAllEdits();
            frame.setTitle("Alloy Analyzer Version 4.0");
            log.clearError();
            latestCommand=0; compiled=null; modified=false; updateStatusBar();
            return true;
        }
    };

    /**
     * Called when the user requests that a specific file should be opened; this method will check
     * if the current text editor needs to be saved or not.
     * (Returns true iff the file was opened successfully)
     */
    private final Func1 a_openFileIfOk = new Func1() {
        public final boolean run(String arg) {
            if (!my_confirm()) return false;
            return a_openFile.run(arg);
        }
    };

    /**
     * Called when the user requests that a specific file should be opened.
     * (Returns true iff the file was opened successfully)
     */
    private final Func1 a_openFile = new Func1() {
        public final boolean run(String f) {
            a_show.run();
            f=(new File(f)).getAbsolutePath();
            boolean result=true;
            FileReader fr=null;
            BufferedReader br=null;
            try {
                fr=new FileReader(f);
                br=new BufferedReader(fr);
                StringBuffer sb=new StringBuffer();
                while(true) { String s=br.readLine(); if (s==null) break; sb.append(s); sb.append('\n'); }
                br.close();
                fr.close();
                highlighter.removeAllHighlights();
                text.setText(sb.toString());
                text.setCaretPosition(0);
                undo.discardAllEdits();
                frame.setTitle("Alloy Model: "+f);
                addHistory(latestName=f);
                log.clearError();
                if (!openfiles.contains(f)) openfiles.add(f);
                latestCommand=0; compiled=null; modified=false; updateStatusBar();
                a_show.run();
                text.requestFocusInWindow();
            } catch(FileNotFoundException e) { log.logRed("Cannot open the file "+f+"\n\n"); result=false;
            } catch(IOException e) { log.logRed("Cannot open the file "+f+"\n\n"); result=false;
            }
            if (br!=null) try{br.close();} catch(IOException ex) {Util.harmless("close()",ex);}
            if (fr!=null) try{fr.close();} catch(IOException ex) {Util.harmless("close()",ex);}
            return result;
        }
    };

    /**
     * Called when the user clicks "File", then "Open".
     * (Rreturns true iff a file was chosen and opened successfully)
     */
    private final Func0 a_open = new Func0() {
        public final boolean run() {
            if (!my_confirm()) return false;
            File file=OurDialog.showFileChooser(frame,true,fileOpenDirectory,".als",".als files");
            if (file==null) return false;
            fileOpenDirectory=file.getParent();
            return a_openFile.run(file.getAbsolutePath());
        }
    };

    /**
     * Called when the user clicks "File", then "Open Builtin Models".
     * (Returns true iff a file was chosen and opened successfully)
     */
    private final Func0 a_openBuiltin = new Func0() {
        public final boolean run() {
            if (!my_confirm()) return false;
            File file=OurDialog.showFileChooser(frame, true, Util.alloyHome()+File.separatorChar+"models", ".als", ".als files");
            if (file==null) return false;
            return a_openFile.run(file.getAbsolutePath());
        }
    };

    /**
     * Called when the user requests that the text editor content be saved to a specific filename.
     * (Returns true iff the file was saved successfully)
     */
    private final Func1 a_saveFile = new Func1() {
        public final boolean run(String filename) {
            if (mode_externalEditor) return false;
            filename=(new File(filename)).getAbsolutePath();
            try {
                FileWriter fw=new FileWriter(filename);
                BufferedWriter bw=new BufferedWriter(fw);
                PrintWriter out=new PrintWriter(bw);
                out.println(text.getText());
                out.flush();
                out.close();
                bw.close();
                fw.close();
                modified=false;
                updateStatusBar();
                addHistory(latestName=filename);
                log.clearError();
                if (!openfiles.contains(latestName)) openfiles.add(latestName);
                frame.setTitle("Alloy Model: "+latestName);
            } catch(IOException e) {
                log.logRed("Cannot write to the file "+filename+"\n\n");
                return false;
            }
            return true;
        }
    };

    /**
     * Called when the user clicks "File", then "Save As".
     * (Returns true iff a file was chosen and saved successfully)
     */
    private final Func0 a_saveAs = new Func0() {
        public final boolean run() {
            if (mode_externalEditor) return false;
            File file=OurDialog.showFileChooser(frame,false,fileOpenDirectory,".als",".als files");
            if (file==null) return false;
            String filename=file.getAbsolutePath();
            if (file.exists() && !OurDialog.questionOverwrite(frame,filename)) return false;
            if (!a_saveFile.run(filename)) return false;
            fileOpenDirectory=file.getParent();
            return true;
        }
    };

    /**
     * Called when the user clicks "File", then "Save".
     * (Returns true iff the file was saved successfully)
     */
    private final Func0 a_save = new Func0() {
        public final boolean run() {
            if (mode_externalEditor) return false;
            if (latestName.length()>0) return a_saveFile.run(latestName); else return a_saveAs.run();
        }
    };

    /**
     * Called when the user clicks "File", then "Close", in the main window
     * (Returns false iff the user cancels the Close operation)
     */
    private final Func0 a_close = new Func0() {
        public final boolean run() {
            if (my_confirm()) {
                openfiles.remove(latestName);
                modified=false;
                a_new.run();
                if (openfiles.size()>0) a_openFile.run(openfiles.get(openfiles.size()-1));
                return true;
            }
            return false;
        }
    };

    /**
     * Called when the user clicks "File", then "Close", in the visualizer window
     * (Returns false iff the user cancels the Close operation)
     */
    private final Func0 a_close2 = new Func0() {
        public final boolean run() {
            xmlLoaded.remove(xmlLatest);
            if (xmlLoaded.size()>0) a_viz.run(xmlLatest=xmlLoaded.get(xmlLoaded.size()-1)); else viz.setVisible(false);
            return true;
        }
    };

    /**
     * Called when the user clicks "File", then "Quit".
     * (Returns false iff the user cancels the Quit operation)
     */
    private final Func0 a_quit = new Func0() {
        public final boolean run() {
            if (my_confirm()) System.exit(0);
            return false;
        }
    };

    /** Called when the user expands the Edit menu */
    private final Func0 a_edit = new Func0() {
        public final boolean run() {
            boolean canUndo = (undo!=null ? undo.canUndo() : true);
            boolean canRedo = (undo!=null ? undo.canRedo() : true);
            editmenu.removeAll();
            (new OurMenuItem(editmenu, "Undo", KeyEvent.VK_Z, KeyEvent.VK_Z, a_undo)).setEnabled(canUndo);
            if (Util.onMac())
                (new OurMenuItem(editmenu, "Redo",                KeyEvent.VK_Z, a_redo)).setEnabled(canRedo);
            else
                (new OurMenuItem(editmenu, "Redo", KeyEvent.VK_Y, KeyEvent.VK_Y, a_redo)).setEnabled(canRedo);
            editmenu.addSeparator();
            editmenu.addMenuItem(null, "Cut",       !mode_externalEditor, KeyEvent.VK_X, KeyEvent.VK_X, a_cut);
            editmenu.addMenuItem(null, "Copy",      true,                 KeyEvent.VK_C, KeyEvent.VK_C, a_copy);
            editmenu.addMenuItem(null, "Paste",     !mode_externalEditor, KeyEvent.VK_V, KeyEvent.VK_V, a_paste);
            editmenu.addSeparator();
            editmenu.addMenuItem(null, "Find...",   !mode_externalEditor, KeyEvent.VK_F, KeyEvent.VK_F, a_find);
            editmenu.addMenuItem(null, "Find Next", !mode_externalEditor, KeyEvent.VK_G, KeyEvent.VK_G, a_findnext);
            return true;
        }
    };

    /** Called when the user clicks Edit->Undo */
    private final Func0 a_undo = new Func0() {
        public final boolean run() {
            try { if (undo!=null && !mode_externalEditor && undo.canUndo()) undo.undo(); }
            catch (CannotUndoException ex) { Util.harmless("undo",ex); }
            return true;
        }
    };

    /** Called when the user clicks Edit->Redo */
    private final Func0 a_redo = new Func0() {
        public final boolean run() {
            try {  if (undo!=null && !mode_externalEditor && undo.canRedo()) undo.redo(); }
            catch (CannotRedoException ex) { Util.harmless("redo",ex); }
            return true;
        }
    };

    /** Called when the user clicks Edit->Copy */
    private final Func0 a_copy = new Func0() {
        public final boolean run() {
            if (lastFocusIsOnEditor && !mode_externalEditor) text.copy(); else log.copy();
            return true;
        }
    };

    /** Called when the user clicks Edit->Cut */
    private final Func0 a_cut = new Func0() {
        public final boolean run() {
            if (lastFocusIsOnEditor && !mode_externalEditor) text.cut();
            return true;
        }
    };

    /** Called when the user clicks Edit->Paste */
    private final Func0 a_paste = new Func0() {
        public final boolean run() {
            if (lastFocusIsOnEditor && !mode_externalEditor) text.paste();
            return true;
        }
    };

    /** Called when the user clicks Edit->Find */
    private final Func0 a_find = new Func0() {
        public final boolean run() {
            if (mode_externalEditor) return false;
            Object answer=JOptionPane.showInputDialog(frame, "Find: ", "Find", JOptionPane.PLAIN_MESSAGE, null, null, lastFind);
            if (!(answer instanceof String)) return false;
            lastFind=(String)answer;
            return a_findnext.run();
        }
    };

    /** Called when the user clicks Edit->FindNext */
    private final Func0 a_findnext = new Func0() {
        public final boolean run() {
            if (mode_externalEditor) return false;
            String all=text.getText();
            int i=all.indexOf(lastFind, text.getCaretPosition());
            if (i<0) {
                i=all.indexOf(lastFind);
                if (i<0) {log.logRed("The specified search string cannot be found."); return false;}
                log.logRed("Search wrapped.");
            } else log.clearError();
            text.setCaretPosition(i);
            text.setSelectionStart(i);
            text.setSelectionEnd(i+lastFind.length());
            text.requestFocusInWindow();
            return true;
        }
    };

    /** Called when the user expands the "Run" menu; always returns true. */
    private final Func0 a_run = new Func0() {
        public final boolean run() {
            runmenu.getMenuComponent(0).setEnabled(true);
            runmenu.getMenuComponent(1).setEnabled(true);
            while(runmenu.getItemCount()>2) runmenu.remove(2);
            runmenu.getItem(0).setText("Run the latest command");
            if (getLatestInstance().length()==0) runmenu.getItem(1).setEnabled(false);
            if (mode_externalEditor) compiled=null;
            if (compiled==null) {
                try {
                    Unit u=null;
                    if (!mode_externalEditor)
                        u=AlloyParser.alloy_parseStream(new StringReader(text.getText()));
                    else if (latestName.length()>0)
                        u=AlloyParser.alloy_parseFile(latestName,"");
                    compiled=u;
                }
                catch(Err e) {
                    runmenu.getItem(0).setEnabled(false);
                    highlight(e); log.logRed(e.toString()+"\n\n");
                    return true;
                }
                catch(Exception e) {
                    runmenu.getItem(0).setEnabled(false);
                    log.logRed("Cannot parse the model!\n"+e.toString()+"\n\n");
                    return true;
                }
            }
            log.logRed(""); // To clear any residual error message
            if (compiled==null || compiled.runchecks.size()==0) { runmenu.getItem(0).setEnabled(false); return true; }
            runmenu.getItem(0).setText("Run "+Util.th(latestCommand+1)+" command");
            if (compiled.runchecks.size()>1) {
                JMenuItem y=new JMenuItem("All");
                y.addActionListener(new RunListener(-1));
                runmenu.add(y);
            }
            runmenu.add(new JSeparator());
            for(int i=0; i<compiled.runchecks.size(); i++) {
                JMenuItem y=new JMenuItem(compiled.runchecks.get(i).toString());
                y.addActionListener(new RunListener(i));
                runmenu.add(y);
            }
            return true;
        }
    };

    /** Called when the user clicks the "Run the latest command" button; always returns true. */
    private final Func0 a_runLatest = new Func0() {
        public final boolean run() {
            a_run.run();
            runmenu.getMenuComponent(0).setEnabled(true);
            runmenu.getMenuComponent(1).setEnabled(true);
            if (compiled==null) return false;
            int n=compiled.runchecks.size();
            if (n==0) { log.logRed("There are no commands to run.\n\n"); return false; }
            if (latestCommand>=n) latestCommand=n-1;
            if (latestCommand<0) latestCommand=0;
            new RunListener(latestCommand).actionPerformed(null);
            return true;
        }
    };

    /** Called when the user clicks the "Stop" button; always returns true. */
    private final Func0 a_stop = new Func0() {
        public final boolean run() {
            if (getCurrentThread()!=null) { AlloyBridge.stopped=true; ViaPipe.forceTerminate(); }
            return true;
        }
    };

    /**
     * Called when the user clicks the "Show the latest instance" button.
     * (returns true iff there was a latest instance to show)
     */
    private final Func0 a_showLatestInstance = new Func0() {
        public final boolean run() {
            String name=getLatestInstance();
            if (name.length()==0) {
                log.logRed("No previous instances are available for viewing.\n\n"); return false;
            } else {
                return a_viz.run(name+".xml");
            }
        }
    };

    /** The current choice of SAT solver. */
    private SolverChoice satOPTION = SolverChoice.parse(Pref.get("solver"));
    private final List<SolverChoice> satChoices;
    private synchronized SolverChoice getSatOption() { return satOPTION; }
    private synchronized void setSatOption(SolverChoice sc) { satOPTION=sc; }

    /** The current verbosity level. */
    private Verbosity verbosity = Verbosity.parse(Pref.get("verbosity"));
    private synchronized Verbosity getVerbosity() { return verbosity; }
    private synchronized void setVerbosity(Verbosity value) { verbosity=value; }

    /** The current font size. */
    private int fontSize = boundFontSize(Pref.getInt("fontsize"),12,24);

    /** Whether the system is using external editor or not. */
    private boolean mode_externalEditor = (Pref.get("externalEditor").length()>0);

    /** Called when the user expands the "Options" menu; always returns true. */
    private final Func0 a_option = new Func0() {
        public final boolean run() {
            optmenu.removeAll();
            JMenu sat=new JMenu("SAT Solver: "+getSatOption());
            for(final SolverChoice sc:satChoices) {
                (new OurMenuItem(sat, ""+sc, new Func0() {
                    public boolean run() { setSatOption(sc); Pref.set("solver",sc.id); return true; }
                })).setIcon(sc==getSatOption()?iconYes:iconNo);
            }
            optmenu.add(sat);
            JMenu verb=new JMenu("Message Verbosity: "+getVerbosity());
            for(final Verbosity vb:Verbosity.values()) {
                (new OurMenuItem(verb, ""+vb, new Func0() {
                    public final boolean run() { setVerbosity(vb); Pref.set("verbosity",vb.id); return true; }
                })).setIcon(vb==getVerbosity()?iconYes:iconNo);
            }
            optmenu.add(verb);
            JMenu size=new JMenu("Font Size: "+fontSize);
            for(final int n: new Integer[]{9,10,11,12,14,16,18,24}) {
                (new OurMenuItem(size, ""+n, new Func0() {
                    public final boolean run() {
                        fontSize=n; Pref.setInt("fontsize",n);
                        text.setFont(new Font(OurUtil.getFontName(), Font.PLAIN, n));
                        status.setFont(new Font(OurUtil.getFontName(), Font.PLAIN, n));
                        log.setFontSize(n);
                        return true;
                    }
                })).setIcon(n==fontSize?iconYes:iconNo);
            }
            optmenu.add(size);
            Func0 ext=new Func0() {
                public boolean run() {
                    if (!mode_externalEditor && modified && !my_confirm()) return false;
                    modified=false; String n=latestName; a_new.run(); if (n.length()>0) a_openFile.run(n);
                    mode_externalEditor = !mode_externalEditor; Pref.set("externalEditor",mode_externalEditor?"y":"");
                    Container all=frame.getContentPane();
                    all.removeAll();
                    newbutton.setVisible(!mode_externalEditor);
                    openbutton.setVisible(!mode_externalEditor);
                    savebutton.setVisible(!mode_externalEditor);
                    loadbutton.setVisible(mode_externalEditor);
                    if (mode_externalEditor) {
                        ((JPanel)(splitpane.getTopComponent())).remove(toolbar);
                        splitpane.setBottomComponent(null);
                        all.add(toolbar, BorderLayout.NORTH);
                        all.add(logpane, BorderLayout.CENTER);
                    } else {
                        ((JPanel)(splitpane.getTopComponent())).add(toolbar, BorderLayout.NORTH);
                        splitpane.setBottomComponent(logpane);
                        all.add(splitpane, BorderLayout.CENTER);
                    }
                    all.add(status, BorderLayout.SOUTH);
                    updateStatusBar();
                    frame.validate();
                    if (mode_externalEditor) logpane.requestFocusInWindow(); else text.requestFocusInWindow();
                    lastFocusIsOnEditor=!mode_externalEditor;
                    return true;
                }
            };
            new OurMenuItem(optmenu, "Use an External Editor: "+(mode_externalEditor?"Yes":"No"), ext);
            if (mode_externalEditor && splitpane.getBottomComponent()!=null) {mode_externalEditor=false; ext.run();}
            return true;
        }
    };

    /** Called when the user wishes to bring this window to the foreground; always returns true. */
    private final Func0 a_show = new Func0() {
        public final boolean run() {
            frame.setVisible(true); frame.setExtendedState(JFrame.NORMAL); frame.requestFocus(); frame.toFront();
            return true;
        }
    };

    /** Called when the user clicks "Help", then "About"; always returns true. */
    private static final Func0 a_showAbout = new Func0() {
        public final boolean run() {
            Icon icon=OurUtil.loadIcon("images/logo.gif");
            Object[] array = {
                    icon,
                    "Alloy Analyzer Version 4.0",
                    "Build date: "+Version.buildDate(),
                    " ",
                    "Lead developer: Felix Chang",
                    "Engine developer: Emina Torlak",
                    "Graphic design: Julie Pelaez",
                    "Project lead: Daniel Jackson",
                    " ",
                    "More information at: http://alloy.mit.edu",
                    "Comments and questions to: alloy@mit.edu",
                    " ",
                    "Thanks to: Ilya Shlyakhter, Manu Sridharan, Derek Rayside, Jonathan Edwards, Gregory Dennis,",
                    "Robert Seater, Edmond Lau, Vincent Yeung, Sam Daitch, Andrew Yip, Jongmin Baek, Ning Song,",
                    "Arturo Arizpe, Li-kuo (Brian) Lin, Joseph Cohen, Jesse Pavel, Ian Schechter, and Uriel Schafer."};
            OurDialog.alert(null, array, "About Alloy 4");
            return true;
        }
    };

    /** Called when the user clicks "Help", then "Show the change log"; always returns true. */
    private static final Func0 a_showChangeLog = new Func0() {
        public final boolean run() {
            String[] changelog = new String[]{
                "2006 Oct 9, 2PM:",
                "  Added BerkMin (via pipe), MiniSat2+Simp (via pipe), and MiniSat2 (via pipe).",
                "  The pipe-based method is now default, since it's more stable than JNI.",
                "2006 Sep 29 6:30PM:",
                "  Major improvements to the look-and-feel components on Mac OS X.",
                "2006 Spe 29 10AM:",
                "  1) Unconnected Int atoms are now hidden by default.",
                "  2) Projection and Unprojection buttons are changed into hyperlinks in the titlebar.",
                "  3) Projection Panel and Tree View will now use the label from the theme.",
                "2006 Sep 21, 3:55PM:",
                "  Added a new builtin method: disj",
                "  disj is a predicate that takes 2 or more arguments.",
                "  For example, disj[x,y,z] means no(x&y) && no(y&z) && no(x&z)",
                "  Note: for historical reasons, disjoint[] is an alias for disj[]",
                "2006 Sep 21, 3:15PM:",
                "  Added a new builtin method: int",
                "  int[x] converts a set of Int atoms into a primitive integer.",
                "  The old Alloy 3 syntax ``int x'' and ``sum x'' are still allowed.",
                "  Note: for historical reasons, sum[] is an alias for int[]",
                "2006 Sep 21, 2:30PM:",
                "  Added a new builtin method: Int",
                "  Int[x] converts a primitive integer into a SIGINT atom.",
                "  The old Alloy 3 syntax ``Int x'' must now be written as ``Int[x]'' or ``x.Int''",
                "2006 Sep 21, 1:10PM:",
                "  Alloy4 Alpha1 released."
            };
            OurDialog.popup(background,changelog);
            return true;
        }
    };

    //====== Constructor ====================================================//

    /** The constructor; this method will be called by the AWT thread, using the "invokeLater" method. */
    private SimpleGUI(String[] args) {

        // Enable better look-and-feel
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Alloy 4");
        System.setProperty("com.apple.mrj.application.growbox.intrudes","true");
        System.setProperty("com.apple.mrj.application.live-resize","true");
        System.setProperty("com.apple.macos.useScreenMenuBar","true");
        System.setProperty("apple.laf.useScreenMenuBar","true");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { Util.harmless("SimpleGUI()",e); }
        // Find out the appropriate Alloy directory
        final String alloyHome=Util.alloyHome();
        final String binary=alloyHome+fs+"binary";
        // Copy the JAR file
        Util.copy(false, alloyHome, "alloy4.jar");
        // Copy the JNI files
        Util.copy(true,binary,"libminisat.so", "libminisat4.so", "libminisat6.so", "libminisat.jnilib");
        Util.copy(true,binary,"libzchaff_basic.so", "libzchaff_basic4.so", "libzchaff_basic6.so", "libzchaff_basic.jnilib");
        Util.copy(false,binary,"minisat.dll", "zchaff_basic.dll");
        // Copy the platform-dependent binaries
        Util.copy(true, binary,"dotbin6", "minisatsimp6", "minisatcore6", "minisat6", "berkmin6");
        Util.copy(true, binary,"dotbin4", "minisatsimp4", "minisatcore4", "minisat4", "berkmin4");
        Util.copy(true, binary,"dotbin", "minisatsimp", "minisatcore", "minisat", "berkmin");
        Util.copy(false,binary,"dotbin.exe", "minisatsimp.exe", "minisatcore.exe", "minisat.exe", "berkmin.exe");
        Util.copy(false,binary,"jpeg.dll","libexpat.dll","libexpatw.dll","zlib1.dll","z.dll","freetype6.dll","png.dll");
        // Copy the model files
        Util.copy(false,alloyHome,
                "models/examples/algorithms/dijkstra.als", "models/examples/algorithms/messaging.als",
                "models/examples/algorithms/opt_spantree.als", "models/examples/algorithms/peterson.als",
                "models/examples/algorithms/ringlead.als", "models/examples/algorithms/s_ringlead.als",
                "models/examples/algorithms/stable_mutex_ring.als",
                "models/examples/algorithms/stable_orient_ring.als",
                "models/examples/algorithms/stable_ringlead.als", "models/examples/case_studies/INSLabel.als",
                "models/examples/case_studies/chord.als", "models/examples/case_studies/chord2.als",
                "models/examples/case_studies/chordbugmodel.als", "models/examples/case_studies/com.als",
                "models/examples/case_studies/firewire.als", "models/examples/case_studies/ins.als",
                "models/examples/case_studies/iolus.als", "models/examples/case_studies/sync.als",
                "models/examples/case_studies/syncimpl.als", "models/examples/puzzles/farmer.als",
                "models/examples/puzzles/handshake.als", "models/examples/puzzles/hanoi.als",
                "models/examples/systems/file_system.als", "models/examples/systems/javatypes_soundness.als",
                "models/examples/systems/lists.als", "models/examples/systems/marksweepgc.als",
                "models/examples/systems/views.als", "models/examples/toys/birthday.als",
                "models/examples/toys/ceilingsAndFloors.als", "models/examples/toys/genealogy.als",
                "models/examples/toys/grandpa.als", "models/examples/toys/javatypes.als",
                "models/examples/toys/life.als", "models/examples/toys/numbering.als",
                "models/examples/toys/railway.als", "models/examples/toys/trivial.als",
                "models/examples/tutorial/farmer.als",
                "models/util/boolean.als", "models/util/graph.als", "models/util/integer.als",
                "models/util/natural.als", "models/util/ordering.als", "models/util/relation.als",
                "models/util/seqrel.als", "models/util/sequence.als", "models/util/ternary.als"
        );

        // Figure out the desired x, y, width, and height
        int screenWidth=OurUtil.getScreenWidth(), screenHeight=OurUtil.getScreenHeight();
        int width=Pref.getInt("width");
        if (width<=0) width=screenWidth/10*8; else if (width<100) width=100;
        if (width>screenWidth) width=screenWidth;
        int height=Pref.getInt("height");
        if (height<=0) height=screenHeight/10*8; else if (height<100) height=100;
        if (height>screenHeight) height=screenHeight;
        int x=Pref.getInt("x"); if (x<0) x=screenWidth/10; if (x>screenWidth-100) x=screenWidth-100;
        int y=Pref.getInt("y"); if (y<0) y=screenHeight/10; if (y>screenHeight-100) y=screenHeight-100;

        // Construct the JFrame object
        frame=new JFrame("Alloy Analyzer Version 4.0");

        // Create the menu bar
        JMenuBar bar=new JMenuBar();
        frame.setJMenuBar(bar);

        filemenu=new OurMenu(bar, "File", KeyEvent.VK_F, a_file);
        editmenu=new OurMenu(bar, "Edit", KeyEvent.VK_E, a_edit);
        runmenu=new OurMenu(bar, "Run", KeyEvent.VK_R, a_run);
        runmenu.addMenuItem(null, "Run the latest command", true, KeyEvent.VK_R, KeyEvent.VK_R, a_runLatest);
        runmenu.addMenuItem(null, "Show the latest instance", true, KeyEvent.VK_L, KeyEvent.VK_L, a_showLatestInstance);

        if (1==1) { // Options menu
            Error ex=null;
            satChoices = new ArrayList<SolverChoice>();
            for(SolverChoice sc:SolverChoice.values()) satChoices.add(sc);
            try {               System.load(binary+fs+"libminisat6.so");    ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"libminisat4.so");    ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"libminisat.so");     ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"libminisat.jnilib"); ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"minisat.dll");       ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) satChoices.remove(SolverChoice.MiniSatJNI);
            try {               System.load(binary+fs+"libzchaff_basic6.so");    ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"libzchaff_basic4.so");    ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"libzchaff_basic.so");     ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"libzchaff_basic.jnilib"); ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) try { System.load(binary+fs+"zchaff_basic.dll");       ex=null; } catch(UnsatisfiedLinkError e) {ex=e;}
            if (ex!=null) satChoices.remove(SolverChoice.ZChaffJNI);
            // Query twice, so that we can gracefully back off along the JNI choices
            if (!satChoices.contains(getSatOption())) setSatOption(SolverChoice.ZChaffJNI);
            if (!satChoices.contains(getSatOption())) setSatOption(SolverChoice.MiniSatPIPE);
            optmenu = new OurMenu(bar, "Options", KeyEvent.VK_O, a_option);
        }

        windowmenu = new OurMenu(bar, "Window", KeyEvent.VK_W, a_window);

        windowmenu2 = new OurMenu(null, "Window", KeyEvent.VK_W, a_window2);

        helpmenu = new OurMenu(bar, "Help", KeyEvent.VK_H, null);
        if (!Util.onMac()) helpmenu.addMenuItem(null, "About Alloy4...", true, KeyEvent.VK_A, -1, a_showAbout);
        helpmenu.addMenuItem(null, "See the Change Log...", true, KeyEvent.VK_V, -1, a_showChangeLog);

        // Create the toolbar
        toolbar=new JToolBar();
        toolbar.setFloatable(false);
        Color tcolor=new Color(.9f, .9f, .9f);
        if (!Util.onMac()) toolbar.setBackground(tcolor);
        toolbar.add(newbutton=OurUtil.makeJButton(tcolor, "New","Starts a new blank model","images/24_new.gif", a_new));
        toolbar.add(openbutton=OurUtil.makeJButton(tcolor, "Open","Opens an existing model","images/24_open.gif", a_open));
        toolbar.add(loadbutton=OurUtil.makeJButton(tcolor, "Load","Chooses an Alloy model to analyze","images/24_open.gif", a_open));
        loadbutton.setVisible(false);
        toolbar.add(savebutton=OurUtil.makeJButton(tcolor, "Save","Saves the current model","images/24_save.gif", a_save));
        toolbar.add(runbutton=OurUtil.makeJButton(tcolor, "Run","Runs the latest command","images/24_execute.gif", a_runLatest));
        toolbar.add(stopbutton=OurUtil.makeJButton(tcolor, "Stop","Stops the current analysis","images/24_execute_abort2.gif", a_stop));
        toolbar.add(showbutton=OurUtil.makeJButton(tcolor, "Show","Show the latest instance","images/24_graph.gif", a_showLatestInstance));
        toolbar.add(Box.createHorizontalGlue());
        toolbar.setBorder(new OurBorder(false,true));
        stopbutton.setVisible(false);

        // Create the highlighter
        highlighter=new DefaultHighlighter();
        highlightPainter=new Highlighter.HighlightPainter() {
            // TODO: code copied from Java SDK
            private final Color color = new Color(0.9f, 0.4f, 0.4f);
            public void paint(Graphics g, int start, int end, Shape shape, JTextComponent text) {
                Color oldcolor=g.getColor();
                g.setColor(color);
                Rectangle box = shape.getBounds();
                try {
                    Rectangle a = text.getUI().modelToView(text, start);
                    Rectangle b = text.getUI().modelToView(text, end);
                    if (a.y == b.y) { // same line, render a rectangle
                        Rectangle r = a.union(b);
                        g.fillRect(r.x, r.y, (r.width<=1 ? (box.x+box.width-r.x) : r.width), r.height);
                    } else { // different lines
                        int toMarginWidth = box.x + box.width - a.x;
                        g.fillRect(a.x, a.y, toMarginWidth, a.height);
                        if (a.y+a.height != b.y) g.fillRect(box.x, a.y+a.height, box.width, b.y-(a.y+a.height));
                        g.fillRect(box.x, b.y, b.x-box.x, b.height);
                    }
                } catch (BadLocationException e) { Util.harmless("highlightPainter",e); }
                g.setColor(oldcolor);
            }
        };

        // Create the text editor
        text=new JTextArea();
        text.setBorder(new EmptyBorder(1,1,1,1));
        text.setHighlighter(highlighter);
        text.setLineWrap(false);
        text.setEditable(true);
        text.setTabSize(3);
        text.setFont(OurUtil.getFont(fontSize));
        text.addCaretListener(new CaretListener() {
            public final void caretUpdate(CaretEvent e) {updateStatusBar();}
        });
        text.addFocusListener(new FocusListener() {
            public final void focusGained(FocusEvent e) { lastFocusIsOnEditor=true; }
            public final void focusLost(FocusEvent e) { }
        });
        text.getDocument().addDocumentListener(new DocumentListener() {
            public final void changedUpdate(DocumentEvent e) {
                highlighter.removeAllHighlights();
                compiled=null; if (!modified) {modified=true;updateStatusBar();}
            }
            public final void removeUpdate(DocumentEvent e) { changedUpdate(e); }
            public final void insertUpdate(DocumentEvent e) { changedUpdate(e); }
        });
        text.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public final void undoableEditHappened(UndoableEditEvent event) {
                undo.addEdit(event.getEdit());
            }
        });
        JComponent textPane = OurUtil.makeJScrollPane(text);
        textPane.setBorder(new EmptyBorder(0,0,0,0));
        if (!Util.onMac()) {
            text.getActionMap().put("my_copy", new AbstractAction("my_copy") {
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) { a_copy.run(); }
            });
            text.getActionMap().put("my_cut", new AbstractAction("my_cut") {
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) { a_cut.run(); }
            });
            text.getActionMap().put("my_paste", new AbstractAction("my_paste") {
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) { a_paste.run(); }
            });
            text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), "my_copy");
            text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), "my_cut");
            text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), "my_paste");
        }

        // Create the message area
        logpane = OurUtil.makeJScrollPane();
        Func0 focus=new Func0() {
            public final boolean run() {
                lastFocusIsOnEditor=false;
                return true;
            }
        };
        log = new LogToJTextPane(logpane, fontSize, background, Color.BLACK, new Color(.7f,.2f,.2f), focus, a_viz);
        logpane.setBorder(new EmptyBorder(0,0,0,0));

        // Add everything to the frame, then display the frame
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public final void windowClosing(WindowEvent e) { a_close.run(); }
        });
        frame.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                Pref.setInt("width", frame.getWidth());
                Pref.setInt("height", frame.getHeight());
            }
            public void componentMoved(ComponentEvent e) {
                Point p=frame.getLocation();
                Pref.setInt("x", p.x);
                Pref.setInt("y", p.y);
            }
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {}
        });
        Container all=frame.getContentPane();
        all.setLayout(new BorderLayout());
        JPanel lefthalf=new JPanel();
        lefthalf.setLayout(new BorderLayout());
        lefthalf.add(toolbar, BorderLayout.NORTH);
        lefthalf.add(textPane, BorderLayout.CENTER);
        splitpane=new OurSplitPane(JSplitPane.HORIZONTAL_SPLIT, lefthalf, logpane, width/2);
        all.add(splitpane, BorderLayout.CENTER);
        all.add(status=OurUtil.makeJLabel(" ",OurUtil.getFont(fontSize)), BorderLayout.SOUTH);
        status.setBackground(new Color(.9f, .9f, .9f));
        status.setOpaque(true);
        status.setBorder(new OurBorder(true,false));
        frame.pack();
        frame.setSize(new Dimension(width,height));
        frame.setLocation(x,y);
        a_file.run();
        a_edit.run();
        a_option.run();
        a_window.run();
        frame.setVisible(true);

        // Generate some informative log messages
        log.logBold("Alloy Analyzer Version 4.0 (build date: "+Version.buildDate()+")\n\n");
        if (Util.onMac()) MacUtil.registerApplicationListener(a_show, a_showAbout, a_openFileIfOk, a_quit);

        // Open the given file, if a filename is given in the command line
        if (args.length==1) {
            File f=new File(args[0]);
            if (f.exists()) a_openFile.run(f.getAbsolutePath());
        } else if (args.length==2 && args[0].equals("-open")) {
            File f=new File(args[1]);
            if (f.exists()) a_openFile.run(f.getAbsolutePath());
        }

        // Start a separate thread to watch for other invocations of Alloy4
        Runnable r = new Runnable() {
            public final void run() {
                while(true) {
                    try {
                        String msg=Util.lockThenReadThenErase(Util.alloyHome()+File.separatorChar+"msg");
                        if (msg.equals("open:")) OurUtil.invokeAndWait(a_show);
                        else if (msg.startsWith("open:")) OurUtil.invokeAndWait(a_openFileIfOk,msg.substring(5));
                    } catch(IOException e) { }
                    try {Thread.sleep(400);} catch (InterruptedException e) {}
                }
            }
        };
        Thread t=new Thread(r);
        t.start();
    }

    //=======================================================================//

    @SuppressWarnings("unused")
    /** The lock object is static, to ensure that it does not get garbage-collected. */
    private static FileLock lock=null;

    /** Main method that launches the program; this method might be called by an arbitrary thread. */
    public static final void main(final String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashReport());
        try {
            File lockfile = new File(Util.alloyHome() + File.separatorChar + "lock");
            FileChannel lockchannel = new RandomAccessFile(lockfile, "rw").getChannel();
            lock = lockchannel.tryLock();
            if (lock==null) {
                // If Alloy4 is already launched, then write the commandline argument into a msg file then exit.
                // The existing Alloy4 will watch this msg file, and responds accordingly when it is nonempty.
                String dest = Util.alloyHome() + File.separatorChar + "msg";
                if (args.length==1) {
                    File f=new File(args[0]);
                    if (f.exists()) Util.lockThenWrite(dest, "open:"+f.getAbsolutePath());
                } else if (args.length==2 && args[0].equals("-open")) {
                    File f=new File(args[1]);
                    if (f.exists()) Util.lockThenWrite(dest, "open:"+f.getAbsolutePath());
                } else {
                    Util.lockThenWrite(dest, "open:");
                }
                return;
            }
        } catch(IOException ex) {
            OurDialog.fatal(null, "A fatal error has occurred! "+ex.getMessage());
        }
        SwingUtilities.invokeLater(new Runnable() {
            public final void run() { new SimpleGUI(args); }
        });
    }

    //=======================================================================//

    /** This Runnable is used to execute a SAT query; this allows the main GUI to remain responsive. */
    private final class Runner implements Runnable {
        /** The Alloy model to be anaylzed */
        private final Reader source;
        /** The command that this runner should run (0 is first, 1 is second..) (-1 means all of them) */
        private final int index;
        /** Constructs a runner that runs the i-th command (0 is first, 1 is second...) (-1 means all of them) */
        public Runner(Reader source, int i) { this.source=source; this.index=i; }
        /** The run() method that actually runs the SAT query. */
        public void run() {
            try {
                Log blanklog=new Log();
                ArrayList<Unit> units=null;
                if (!mode_externalEditor)
                    units=AlloyParser.alloy_totalparseStream(Util.alloyHome(), source);
                else if (latestName.length()>0)
                    units=AlloyParser.alloy_totalparseFile(Util.alloyHome(),latestName);
                else return;
                ArrayList<ParaSig> sigs=VisitTypechecker.check(blanklog, units);
                String tempdir = Util.maketemp();
                SolverChoice sc=getSatOption();
                List<TranslateAlloyToKodkod.Result> result=
                    TranslateAlloyToKodkod.codegen(index,log,getVerbosity(),units,sigs,sc,tempdir);
                log.flush(); // To make sure everything is flushed.
                (new File(tempdir)).delete(); // In case it was UNSAT, or was TRIVIALLY SAT, or cancelled.
                if (sc!=SolverChoice.FILE && result.size()==1 && result.get(0)==TranslateAlloyToKodkod.Result.SAT) {
                    setLatestInstance(tempdir+fs+(index+1));
                    xml2title.put(tempdir+fs+(index+1)+".xml", units.get(0).runchecks.get(0).toString());
                }
                if (sc!=SolverChoice.FILE && result.size()>1) {
                    log.logBold("" + result.size() + " commands were executed. The results are:\n");
                    int i=0;
                    for(TranslateAlloyToKodkod.Result b:result) {
                        ParaRuncheck r=units.get(0).runchecks.get(i);
                        String label=r.getLabel();
                        i++;
                        if (b==null) {log.log("   #"+i+": Canceled.\n"); continue;}
                        switch(b) {
                        case SAT:
                            if (r.check) log.log("   #"+i+": Counterexample found. "+label+" is invalid. ");
                            else log.log("   #"+i+": Instance found. "+label+" is consistent. ");
                            log.logLink(tempdir+fs+i+".xml");
                            xml2title.put(tempdir+fs+i+".xml", r.toString());
                            log.log("\n");
                            setLatestInstance(tempdir+fs+i);
                            break;
                        case TRIVIALLY_SAT:
                            if (r.check) log.log("   #"+i+": Trivial counterexample found. "+label+" is invalid.\n");
                            else log.log("   #"+i+": Trivial instance found. "+label+" is consistent.\n");
                            break;
                        case UNSAT: case TRIVIALLY_UNSAT:
                            if (r.check) log.log("   #"+i+": No counterexample found. "+label+" may be valid.\n");
                            else log.log("   #"+i+": No instance found. "+label+" may be inconsistent.\n");
                            break;
                        default: log.log("   #"+i+": Canceled.\n");
                        }
                    }
                    log.log("\n");
                }
                log.logDivider();
            }
            catch(UnsatisfiedLinkError e) {
                log.logRed("Cannot run the command!\nThe required JNI library cannot be found!\n"+e.toString()+"\n\n");
            }
            catch(Err e) {
                highlight(e);
                log.logRed(e.toString()+"\n\n");
            }
            OurUtil.invokeAndWait(new Runnable() {
                public final void run() {
                    runmenu.setEnabled(true);
                    runbutton.setVisible(true);
                    showbutton.setEnabled(true);
                    stopbutton.setVisible(false);
                    setCurrentThread(null);
                }
            });
        }
    }

    //=======================================================================//

    /** This ActionListener is called when the user indicates a particular command to execute. */
    private class RunListener implements ActionListener {
        /** The index number of the command that the user wishes to execute (0..) (-1 means ALL of them). */
        private final int index;
        /** The constructor. */
        public RunListener(int index) {this.index=index;}
        /** The event handler that gets called when the user clicked on one of the menu item. */
        public void actionPerformed(ActionEvent e) {
            if (getCurrentThread()!=null) return;
            latestCommand=(index>=0 ? index : compiled.runchecks.size()-1);
            AlloyBridge.stopped=false;
            Runner r=new Runner(new StringReader(text.getText()), index);
            Thread t=new Thread(r);
            setCurrentThread(t);
            runmenu.setEnabled(false);
            runbutton.setVisible(false);
            showbutton.setEnabled(false);
            stopbutton.setVisible(true);
            t.start();
        }
    }

    //=======================================================================//
}
