package edu.uw.ece.alloy.debugger.mutate;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.util.ServerSocketListener;

public class PatternAnalyzerStub extends ServerSocketListener {

	protected final static Logger logger = Logger.getLogger(PatternAnalyzerStub.class.getName()+"--"+Thread.currentThread().getName());
	// Debugger waits on the queue to be locked.
	Queue<AlloyProcessedResult> queue;
	protected Thread self = new Thread(this);
	
	public PatternAnalyzerStub(InetSocketAddress hostAddress,
			InetSocketAddress remoteAddress,
			Queue<AlloyProcessedResult> queue) {
		super(hostAddress, remoteAddress);
		this.queue = queue;
	}

	protected void onStartingListening() throws InterruptedException{
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" The process is started on port : " + this.getHostAddress().getPort());
		System.out.println("onStartingListening");
	}
	
	protected void processCommand(final RemoteCommand command){
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" Recieved message: "+command);
		command.patternExtractionDone(queue);
		command.storeResult(queue);
		command.readyToUse(queue);
	}
	
	
	@Override
	public void cancelThread() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePriority(int newPriority) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionOnNotStuck() {
		// TODO Auto-generated method stub

	}

	@Override
	public int triesOnStuck() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void actionOnStuck() {
		// TODO Auto-generated method stub

	}

	@Override
	public String amIStuck() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected Thread getThread() {
		return self;
	}

}
