package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.logging.Level;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.onborder.propgen.HolaResult;

@Deprecated
public class HolaCompleteCommand extends RemoteCommand {

	private static final long serialVersionUID = 7812337897314648833L;

	public final InetSocketAddress PID;
	public final HolaResult result;

	public HolaCompleteCommand(final InetSocketAddress pID,
			final HolaResult result) {
		super();
		PID = pID;
		this.result = result;
	}

	public void send(final InetSocketAddress remoteAddres)
			throws InterruptedException {

		if (Configuration.IsInDeubbungMode)
			logger.fine("[" + Thread.currentThread().getName() + "] "
					+ "Sending a response: pID= " + PID + " result=" + result.toString());

		try {

			this.sendMe(remoteAddres);

			if (Configuration.IsInDeubbungMode)
				logger.fine("[" + Thread.currentThread().getName() + "] "
						+ "Response is sent: pID= " + PID + " result=" + result.toString());

		} catch (Exception e) {

			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Failed on prepare or send the message: " + this.result, e);
			e.printStackTrace();
		}

	}

	@Override
	public boolean processResult(final Deque<Object> queue) {

		if (result.getInstance() != null) {

			queue.push(result);

			System.out
					.println("=====================================================");
			System.out.println("Instance Result: \n    "
					+ result.getInstance().toString().replace("\n", "\n" + "    ") + "");
			System.out
					.println("=====================================================");

		}

		return result.isLast();
	}

	@Override
	public String toString() {
		return "HolaComplete [PID=" + PID + ", result=" + result.toString() + "]";
	}
}
