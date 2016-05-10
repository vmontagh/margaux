/**
 * 
 */
package edu.uw.ece.alloy.debugger.infrastructure;

import java.net.InetSocketAddress;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ExpressionAnalyzerRunner_;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;

/**
 * @author vajih
 *
 */

@Deprecated
public class DebuggerRemoteProcessManager extends RemoteProcessManager<ExpressionAnalyzerRunner_> {

	public DebuggerRemoteProcessManager(InetSocketAddress localSocket,
			int maxActiveProcessNumbers, int maxDoingTasks) {
		super(localSocket, maxActiveProcessNumbers, maxDoingTasks);
	}

}
