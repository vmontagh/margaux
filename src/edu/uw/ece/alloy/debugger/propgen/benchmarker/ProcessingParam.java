/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.Serializable;

/**
 * @author Fikayo Odunayo
 *        
 */
public  abstract class ProcessingParam implements Comparable<ProcessingParam>, Serializable {

    public final int priority;
    public final File tmpDirectory;
    
    public ProcessingParam(final int priority, final File tmpDirectory) {
        
        this.priority = priority;
        this.tmpDirectory = new File(tmpDirectory.getPath());
    }
    
    public ProcessingParam prepareToUse() throws Exception {
        
        return this;
    }
    
    public ProcessingParam prepareToSend() throws Exception {
        
        return this;
    }
    
    public abstract boolean isEmptyParam();
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ProcessingParam o) {
        
        if (o == null)
            return -1;
        
        if (o.priority == this.priority)
            return 0;
        
        if (o.priority < this.priority)
            return 1;
        
        return -1;
    }

    public abstract ProcessingParam createItself();
    
    public abstract ProcessingParam changeTmpDirectory(final File tmpDirectory);
    
}