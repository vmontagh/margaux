package edu.mit.csail.sdg.alloy4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * This class asks for permission to email a bug report when an uncaught exception occurs.
 */

public final class ExitReporter implements UncaughtExceptionHandler, ActionListener {

    /** Construct a new ExirReporter. */
    public ExitReporter() { }

    /** The name of the main file being analyzed; "" if unknwon. */
    private String mainfile = "";

    /** The content of the main file being analyzed; "" if unknown. */
    private String mainfilecontent = "";

    /** The list of additional files being included from the main file. */
    private Set<String> subfiles = new LinkedHashSet<String>();

    /** Removes the info about main file and subfiles. */
    public synchronized void clearAll() { subfiles.clear(); mainfile=""; mainfilecontent=""; }

    /** Sets the main file's filename and main file's content. */
    public synchronized void setMainFile(String filename,String content) {mainfile=filename; mainfilecontent=content;}

    /** Add a new file to the set of "included files". */
    public synchronized void addSubFile(String filename) { subfiles.add(filename); }

    /** This method is an exception handler for uncaught exceptions. */
    public synchronized void uncaughtException(Thread thread, Throwable ex) {
        if (ex!=null) { ex.printStackTrace(System.err); System.err.flush(); }
        String yes="Send the Bug Report", no="Don't Send the Bug Report";
        JTextField email = new JTextField(20);
        JTextArea comment = new JTextArea();
        email.setBorder(new LineBorder(Color.DARK_GRAY));
        comment.setBorder(null);
        JScrollPane scroll=OurUtil.scrollpane(comment);
        scroll.setPreferredSize(new Dimension(300,200));
        scroll.setBorder(new LineBorder(Color.DARK_GRAY));
        if ((ex instanceof OutOfMemoryError) || (ex instanceof StackOverflowError)) {
            if (JOptionPane.showOptionDialog(null, new Object[]{
                    "Alloy Analyzer has run out of memory.",
                    "Your model may be too large to be analysed, or may",
                    "be using features that make the problem intractable.",
                    "Please visit http://alloy.mit.edu/",
                    "for tips on writing efficient Alloy models.",
                    " ",
                    "If you believe the model should be analyzable,",
                    "you may submit a bug report (via HTTP).",
                    "The error report will include your Alloy source",
                    "and system configuration, but no other information.",
                    " ",
                    "If you'd like to be notified about a fix,",
                    "please enter your email adress and optionally add a comment.",
                    " ",
                    OurUtil.makeHT("Email:",5,email,null),
                    OurUtil.makeHT("Comment:",5,scroll,null)
            }, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
            null, new Object[]{yes,no}, no)!=JOptionPane.YES_OPTION) { if (thread!=null) System.exit(1); return; }
        } else {
            if (JOptionPane.showOptionDialog(null, new Object[]{
                    (thread==null ? "Thank you for submitting a bug report." : "Sorry. An internal error has occurred."),
                    " ",
                    "You may post a bug report (via HTTP).",
                    "The error report will include your Alloy source",
                    "and system configuration, but no other information.",
                    " ",
                    "If you'd like to be notified about a fix,",
                    "please enter your email adress and optionally add a comment.",
                    " ",
                    OurUtil.makeHT("Email:",5,email,null),
                    OurUtil.makeHT("Comment:",5,scroll,null)
            }, "Error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
            null, new Object[]{yes,no}, no)!=JOptionPane.YES_OPTION) { if (thread!=null) System.exit(1); return; }
        }
        StringWriter sw=new StringWriter();
        PrintWriter pw=new PrintWriter(sw);
        pw.printf("\nAlloy Analyzer %s crash report (Build Date = %s)\n\n", Version.version(), Version.buildDate());
        pw.printf("========================= Email ============================\n%s\n\n", email.getText());
        pw.printf("========================= Comment ==========================\n%s\n\n", comment.getText());
        if (thread==null) {
          pw.printf("================== Bug Report Manually Triggered! ==========\n\n");
        } else {
          pw.printf("========================= Thread Name ======================\n%s\n\n", thread.getName());
        }
        if (ex!=null) {
          pw.printf("========================= Exception ========================\n%s\n\n",
                ex.getClass().toString()+": "+ex.toString());
          pw.printf("========================= Stack Trace ======================\n");
          ex.printStackTrace(pw);
        }
        pw.printf("\n========================= Preferences ======================\n");
        Pref.dump(pw);
        pw.printf("\n========================= System Properties ================\n");
        pw.println("Free memory = "+Runtime.getRuntime().freeMemory());
        pw.println("Total memory = "+Runtime.getRuntime().totalMemory());
        for(Map.Entry<Object,Object> e:System.getProperties().entrySet()) {
            Object k=e.getKey(); if (k==null) k="null";
            Object v=e.getValue(); if (v==null) v="null";
            if (k.equals("line.separator")) continue;
            pw.printf("%s = %s\n", k.toString(), v.toString());
        }
        pw.printf("\n\n========================= Main Model =======================\n");
        pw.printf("// %s\n%s\n", mainfile, mainfilecontent);
        for(String e:subfiles) {
            String content;
            pw.printf("\n\n========================= Sub Model ========================\n");
            try {content=Util.readAll(e);} catch(IOException ex2) {content="// IO Exception: "+ex2.getMessage();}
            pw.printf("// %s\n%s\n", e, content);
        }
        pw.printf("\n\n========================= The End ==========================\n\n");
        pw.close();
        sw.flush();
        System.err.println(sw.toString());
        System.err.flush();
        JTextArea status=null;
        try {
            final JFrame statusWindow=new JFrame();
            JButton done=new JButton("Close");
            if (thread==null) {
                done.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { statusWindow.dispose(); }
                });
            } else {
                done.addActionListener(this);
            }
            status=new JTextArea("Sending the bug report... please wait...");
            status.setEditable(false);
            status.setLineWrap(true);
            status.setWrapStyleWord(true);
            status.setBackground(Color.WHITE);
            status.setBorder(new EmptyBorder(2,2,2,2));
            JScrollPane statusPane=new JScrollPane(status);
            statusPane.setBorder(null);
            statusWindow.setTitle("Sending Bug Report");
            statusWindow.setBackground(Color.LIGHT_GRAY);
            statusWindow.getContentPane().setLayout(new BorderLayout());
            statusWindow.getContentPane().add(statusPane, BorderLayout.CENTER);
            statusWindow.getContentPane().add(done, BorderLayout.SOUTH);
            int w=OurUtil.getScreenWidth(), h=OurUtil.getScreenHeight();
            statusWindow.pack();
            statusWindow.setSize(600,200);
            statusWindow.setLocation(w/2-300,h/2-100);
            statusWindow.setVisible(true);
        } catch(Exception exception) { }
        String result=postBug(sw.toString());
        if (status!=null) status.setText(result); else if (thread!=null) System.exit(1);
    }

    /** Post the given string via POST HTTP request. */
    private static String postBug(String bugReport) {
        final String NEW_LINE = System.getProperty("line.separator");
        final String BUG_POST_URL = "http://alloy.mit.edu/postbug4.php";
        OutputStreamWriter out = null;
        BufferedReader in = null;
        try {
            // open the URL connection
            URL url = new URL(BUG_POST_URL);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            // write the bug report to the cgi script
            out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.write(bugReport);
            out.flush();
            // read the response back from the cgi script
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder report = new StringBuilder();
            for (String inputLine = in.readLine(); inputLine != null; inputLine = in.readLine()) {
                report.append(inputLine);
                report.append(NEW_LINE);
            }
            return report.toString();
        } catch (Exception ex) {
            return "Sorry. An error has occurred in posting the bug report.\n\n"
            +"Please email alloy.mit.edu directly and we will work to fix the problem.\n\n"
            +"(Bug posting failed due to Java exception: "+ex.toString()+")";
        } finally {
            if (out != null) { try { out.close(); } catch(Exception ignore) { } }
            if (in != null) { try { in.close(); } catch(Exception ignore) { } }
        }
    }

    /** Called when the user clicks the CLOSE button. */
    public void actionPerformed(ActionEvent e) { System.exit(1); }
}
