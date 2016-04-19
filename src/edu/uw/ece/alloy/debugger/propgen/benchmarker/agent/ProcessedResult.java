/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import edu.mit.csail.sdg.gen.MyReporter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;

/**
 * @author fikayo
 *        
 */
public abstract class ProcessedResult extends MyReporter {
    
    public final ProcessingParam params;
    
    public ProcessedResult(final ProcessingParam params) {
        this.params = params != null ? params.createItself() : this.getEmptyParam();
    }
    
    protected abstract ProcessingParam getEmptyParam();
}
