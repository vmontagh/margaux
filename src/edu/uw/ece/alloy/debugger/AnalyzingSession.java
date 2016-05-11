package edu.uw.ece.alloy.debugger;

import java.util.UUID;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;

/**
 * Every analyzing session starts with a Request message and finishes once all
 * the execution is done or a timeout occurs.
 * 
 * @author vajih
 *
 */
public interface AnalyzingSession {

	public long getSessionCreationTime();

	public UUID getSessionID();

	/**
	 * starting a session once a request is received.
	 */
	public void start();

	/**
	 * An intermediate message is reported, so a follow up is reacted.
	 */
	void followUp(ResponseMessage message);

	/**
	 * If session needs to be finished, call done.
	 */
	public void done();

	/**
	 * An internal timer that fires the done message after a particular time.
	 */
	void doneOnWait();
}
