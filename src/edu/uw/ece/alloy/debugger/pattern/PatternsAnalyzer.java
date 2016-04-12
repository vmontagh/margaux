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
import edu.uw.ece.alloy.util.ServerSocketListener;

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
public class PatternsAnalyzer extends ServerSocketListener {

	// Very temporary to determine the status of the analyzer from analyzing to IDLE.
	public static Boolean analyzing = false;
	
	protected final static Logger logger = Logger
			.getLogger(PatternsAnalyzer.class.getName() + "--"
					+ Thread.currentThread().getName());

	private Thread thread = new Thread(this);
	protected volatile AtomicInteger livenessFailed = new AtomicInteger(0);
	protected final GeneratedStorage<AlloyProcessingParam> feeder;

	public PatternsAnalyzer(final int hostPort, final int remotePort,
			final GeneratedStorage<AlloyProcessingParam> feeder) {
		super(remotePort, hostPort);
		this.feeder = feeder;
	}

	public PatternsAnalyzer(final String hostName, final int hostPort,
			final String remoteName, final int remotePort,
			final GeneratedStorage<AlloyProcessingParam> feeder) {
		super(hostName, hostPort, remoteName, remotePort);
		this.feeder = feeder;
	}

	public PatternsAnalyzer(final InetSocketAddress hostAddress,
			final InetSocketAddress remoteAddress,
			final GeneratedStorage<AlloyProcessingParam> feeder) {
		super(hostAddress, remoteAddress);
		this.feeder = feeder;
	}

	protected void processCommand(final RemoteCommand command) {
		// Supposed to be a registering call;
		System.out.println("Commands is received in PatternsAnalyzer:" + command);
		command.doAnalyze(feeder);
	}

	protected Thread getThread() {
		return thread;
	}

	@Override
	public void cancelThread() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePriority(int newPriority) {
		// TODO Auto-generated method stub

	}

	protected void sendLivenessMessage() {
		try {
			IamAlive iamAlive = new IamAlive(hostAddress, System.currentTimeMillis(),
					-1, -1);
			iamAlive.sendMe(remoteAddress);
			livenessFailed.set(0);
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "]"
						+ "A live message" + iamAlive);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"[" + Thread.currentThread().getName() + "]"
							+ "Failed to send a live signal this is " + livenessFailed
							+ " attempt",
					e);
			livenessFailed.incrementAndGet();
		}
	}

	protected void haltIfCantProceed(final int maxRetryAttepmpts) {
		// recovery was not enough, the whole processes has to be shut-down
		if (livenessFailed.get() > maxRetryAttepmpts) {
			logger.severe("[" + Thread.currentThread().getName() + "]"
					+ livenessFailed
					+ " liveness message, attempts does not prceeed, So the process is exited.");
			Runtime.getRuntime().halt(0);
		}
	}

	@Override
	public void actionOnNotStuck() {
		sendLivenessMessage();
		haltIfCantProceed(
				/* AlloyProcessRunner.getInstance().SelfMonitorRetryAttempt */ 1);
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
