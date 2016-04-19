/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import edu.uw.ece.alloy.debugger.onborder.OnBorderCodeGenerator;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.HolaProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;

/**
 * @author Fikayo Odunayo
 *         
 */
public class OnBorderRequest extends RemoteCommand {
    
    private final String alloyFileName;
    
    public OnBorderRequest(final String alloyFileName) {
        this.alloyFileName = alloyFileName;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand#
     * doAnalyze(edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage)
     */
    @Override
    public void doAnalyze(final GeneratedStorage<ProcessingParam> generatedStorage) {
        
        File file = new File(this.alloyFileName);
        String directory = file.getParent() + "/";
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf(".")) + "_mod.als";
        
        File newFile = new File(directory + name);
        newFile.getParentFile().mkdirs();
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(newFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        OnBorderCodeGenerator generator = new OnBorderCodeGenerator(this.alloyFileName, writer);
        generator.run();
        
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("", "");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        generatedStorage.addGeneratedProp(new HolaProcessingParam(newFile.getPath(), tmpFile));
    }
}
