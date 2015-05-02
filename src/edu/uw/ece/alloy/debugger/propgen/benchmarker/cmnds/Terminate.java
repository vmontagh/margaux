package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.logging.Level;

public class Terminate extends RemoteCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1218570292314428282L;

	public Terminate() {
		super();
	}

	public void terminate(final AsynchronousSocketChannel param){
		try {
			((AsynchronousSocketChannel)param).close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"The connection cannot be terminated", e);
			throw new CommandRuntimeException(e.getMessage(),e);
		}
	}

	@Override
	public String toString() {
		return "Terminate []";
	}

}
