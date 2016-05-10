/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

/**
 * The liveness message reports the number of processed tasks.
 * @author vajih
 *
 */
public interface UpdateLivenessStatus {
	public void setProcessed(int processed);
	public void setTobeProcessed(int tobeProcessed);
}
