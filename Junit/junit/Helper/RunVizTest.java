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

package junit.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import edu.mit.csail.sdg.alloy4viz.VizGUI;

/** This class demonstrates how to access Alloy4 via the compiler methods. */

public final class RunVizTest {

    /*
     * Execute every command in every file.
     *
     * This method parses every file, then execute every command.
     *
     * If there are syntax or type errors, it may throw
     * a ErrorSyntax or ErrorType or ErrorAPI or ErrorFatal exception.
     * You should catch them and display them,
     * and they may contain filename/line/column information.
     */
    public void main(String[] args) throws Err {
    	Execute(args);
       
    }

	public final File OUT_DIR = new File("tmp/TestInstances/");

    public List<String> Execute(String[] args)throws Err{
    	
    	if (!OUT_DIR.exists()) {
    		final boolean result = OUT_DIR.mkdirs();
    		assert result : "couldn't make " + OUT_DIR;
    	}
    	
    	 // The visualizer (We will initialize it to nonnull when we visualize an Alloy solution)
        List<String> list_instances_xml = new ArrayList<String>();
        String alloyinstance_xml;

        // Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
        // By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
        A4Reporter rep = new A4Reporter() {
            // For example, here we choose to display each "warning" by printing it to System.out
            @Override public void warning(ErrorWarning msg) {
                System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                System.out.flush();
            }
        };

        for(String filename:args) {
        	
        	File file = new File(filename);
        	String filename1 = file.getName();
            // Parse+typecheck the model
            System.out.println("=========== Parsing+Typechecking "+filename1+" =============");
            Module world = CompUtil.parseEverything_fromFile(rep, null, filename);

            // Choose some default options for how you want to execute the commands
            A4Options options = new A4Options();
            options.solver = A4Options.SatSolver.SAT4J;

            for (Command command: world.getAllCommands()) {
                // Execute the command
                System.out.println("============ Command "+command+": ============");
                A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
                // Print the outcome
                System.out.println(ans);
                // If satisfiable...
                if (ans.satisfiable()) {
                    // You can query "ans" to find out the values of each set or type.
                    // This can be useful for debugging.
                    //
                    // You can also write the outcome to an XML file
                	alloyinstance_xml= IntanceFileName(filename1, world.getAllCommands().indexOf(command));
                    ans.writeXML(alloyinstance_xml);
                    list_instances_xml.add(alloyinstance_xml);
                }
            }
        }
        return list_instances_xml;
    }
    
    public String IntanceFileName(String filename, Object command){
    	return OUT_DIR.getAbsolutePath() +filename.replace(".als", "_"+"Command"+command.toString()+".xml");
    }
}