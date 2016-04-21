/**
 * 
 */
package edu.uw.ece.alloy.debugger.mutate;

import java.io.IOException;

import edu.mit.csail.sdg.alloy4.Err;

/**
 * @author vajih
 *
 */
public class DebuggerRunner {

	
	
	public void start() throws Exception {
		
	}
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		String AlloyTmpTestPath = "tmp/testing.als";
		
		Debugger deg;
		try {
			deg = new Debugger(AlloyTmpTestPath);
			deg.bootRemoteAnalyzer();
			//Thread.sleep(1000);
			deg.analyzeImpliedPatterns();
		} catch (Err | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
