package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;

public class ProcessIt extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8188777849612332517L;
	public final AlloyProcessingParam param;
	public transient final ProcessesManager processesManager;
	
	public ProcessIt(AlloyProcessingParam param, final ProcessesManager processesManager) {
		super();
		this.param = param;
		this.processesManager = processesManager;
	}

	public void process(AlloyExecuter executer) {
		executer.process(param);
	}

	@Override
	public String toString() {
		return "ProcessIt [param=" + param + "]";
	}
	
	public  void sendMe(final InetSocketAddress remoteAddres) throws InterruptedException{
	
		super.sendMe(remoteAddres);
		processesManager.recordAMessageSentCounter(remoteAddres.getPort());
		processesManager.IncreaseSentTasks(remoteAddres.getPort(), 1);

	}
	
}
