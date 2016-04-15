/**
 * 
 */
package edu.uw.ece.alloy.debugger.pattern;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.GeneratedStorage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AnalyzeExternalRequest;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.IamAlive;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.util.ServerSocketInterface;

/**
 * 
 * The class analyzes the patterns and returns the results. The call is based on
 * the socket. It takes an message for the requested type of analysis as well as
 * the input data.
 * 
 * The result is returned via a socket channel.
 * 
 * If the process takes longer time, the process kills itself or be killed by
 * the caller after receiving a suicide message.
 * 
 * @author vajih
 *
 */
public class PatternsAnalyzerInterface extends ServerSocketInterface {

	// Very temporary to determine the status of the analyzer from analyzing to IDLE.
	public static Boolean analyzing = false;
	
	protected final static Logger logger = Logger
			.getLogger(PatternsAnalyzerInterface.class.getName() + "--"
					+ Thread.currentThread().getName());

	protected final GeneratedStorage<AlloyProcessingParam> feeder;

	public PatternsAnalyzerInterface(final int hostPort, final int remotePort,
			final GeneratedStorage<AlloyProcessingParam> feeder) {
		super(remotePort, hostPort);
		this.feeder = feeder;
	}

	public PatternsAnalyzerInterface(final String hostName, final int hostPort,
			final String remoteName, final int remotePort,
			final GeneratedStorage<AlloyProcessingParam> feeder) {
		super(hostName, hostPort, remoteName, remotePort);
		this.feeder = feeder;
	}

	public PatternsAnalyzerInterface(final InetSocketAddress hostAddress,
			final InetSocketAddress remoteAddress,
			final GeneratedStorage<AlloyProcessingParam> feeder) {
		super(hostAddress, remoteAddress);
		this.feeder = feeder;
	}

	protected void onReceivedMessage(final RemoteCommand command) {
		// Supposed to be a registering call;
		super.onReceivedMessage(command);
		System.out.println("Commands is received in PatternsAnalyzer:" + command);
		command.doAnalyze(feeder);
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
		return "";
	}

	@Override
	public long isDelayed() {
		// TODO Auto-generated method stub
		return 0;
	}

}
