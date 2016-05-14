package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Optional;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.communication.Queue;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadToBeMonitored;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.Utils;

public abstract class PostProcess implements Runnable, ThreadToBeMonitored {

	protected final Queue<AlloyProcessedResult> resultsQueue = new Queue<>();
	public final Optional<PostProcess> nextAction;

	protected volatile AtomicInteger processed = new AtomicInteger(0);

	protected volatile AtomicInteger shadowProcessed = new AtomicInteger(-1);
	protected volatile AtomicInteger recoveredTry = new AtomicInteger(0);

	protected final static Logger logger = Logger.getLogger(
			PostProcess.class.getName() + "--" + Thread.currentThread().getName());
	final public static boolean doCompress = Boolean
			.valueOf(Configuration.getProp("doCompressAlloyParams"));

	Thread postProcessThread = new Thread(this);;

	public PostProcess(Optional<PostProcess> nextAction) {
		this.nextAction = nextAction;
	}

	public PostProcess(PostProcess nextAction) {
		this(Optional.fromNullable(nextAction));
	}

	public PostProcess() {
		this(Optional.fromNullable(null));
	}

	public boolean isEmpty() {
		return resultsQueue.isEmpty();
	}

	public String amIStuck() {
		return isDelayed() == 0 ? ""
				: "Processing PostProess" + getClass().getSimpleName()
						+ " is stuck after processing " + processed + " messages.";
	}

	@Override
	public void cancelThread() {
		postProcessThread.interrupt();
	}

	@Override
	public void changePriority(int newPriority) {
		postProcessThread.setPriority(newPriority);
	}

	/**
	 * If the delay condition is true, then it returns a message how many messages
	 * are stuck in the queue. This function is useful for monitoring the threads.
	 * 
	 * @return Non-empty message means a delay is recorded Empty message
	 */
	public synchronized long isDelayed() {
		long result = 0;
		// monitor the socket
		if (shadowProcessed.get() == processed.get() && !isEmpty()) {
			// The executer does not proceeded.
			result = processed.longValue();
			// TODO manage to reset the socket thread
		} else {
			// The executer proceeded
			shadowProcessed.set(processed.get());
		}
		return result;
	}

	public int triesOnStuck() {
		return recoveredTry.get();
	}

	protected void doActionOnStuck() {
		if (Configuration.IsInDeubbungMode)
			logger.finer("[" + Thread.currentThread().getName() + "] " + "");
	}

	public void actionOnStuck() {
		triesOnStuck();
	}

	public void startThread() {
		postProcessThread.start();
	}

	public void actionOnNotStuck() {
		if (Configuration.IsInDeubbungMode)
			logger.finer("[" + Thread.currentThread().getName() + "] " + "");
	}

	public void doAction(final AlloyProcessedResult result)
			throws InterruptedException {
		try {
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName() + "Post process: " + result);
			resultsQueue.put(result);
		} catch (InterruptedException e) {
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName()
						+ "Processing a new result is interupted: " + result);
			throw e;
		}
	}

	protected abstract void action(final AlloyProcessedResult result)
			throws InterruptedException;

	protected void actionAndIncreament(final AlloyProcessedResult result)
			throws InterruptedException {
		action(result);
		processed.incrementAndGet();
	}

	protected void doSerialAction(final AlloyProcessedResult result)
			throws InterruptedException {
		this.actionAndIncreament(result);
		if (nextAction.isPresent())
			nextAction.get().doSerialAction(result);
	}

	@Override
	public void run() {
		AlloyProcessedResult result = null;
		try {
			while (!Thread.currentThread().isInterrupted()) {
				result = resultsQueue.take();
				if (Configuration.IsInDeubbungMode)
					logger.info(Utils.threadName() + "Start a Post process: " + result);
				doSerialAction(result);
			}
		} catch (InterruptedException e) {
			logger.severe(Utils.threadName() + "Processing a result is interrupted: "
					+ result + " after processed " + processed + " results.");
		}
	}

	public void cancel() {
		Thread.currentThread().interrupt();
	}

	public static class FileWrite extends PostProcess {

		public FileWrite() {
			super();
		}

		public FileWrite(PostProcess socketWriter) {
			super(socketWriter);
		}

		@Override
		protected void action(AlloyProcessedResult result) {

			String content = result.asRecordHeader() + ",propa,propb,op,sat" + "\n"
					+ result.asRecord() + ","
					+ result.getParam().getAlloyCoder().get().predNameA + ","
					+ result.getParam().getAlloyCoder().get().predNameB + ","
					+ result.getParam().getAlloyCoder().get().commandOperator() + ","
					+ DBLogger.convertSATResult(result);

			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "Start wirint on file: " + result + "   " + content);

			try {
				Util.writeAll(result.getParam().getDestPath().get().getAbsolutePath(),
						content);
				if (Configuration.IsInDeubbungMode)
					logger.info("[" + Thread.currentThread().getName() + "] "
							+ "result is written in: "
							+ result.getParam().getDestPath().get().getAbsolutePath());
			} catch (Err e) {
				logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
						+ "Failed on storing the result: " + result, e);
			}
		}

	}

	public static class SocketWriter extends PostProcess {

		final ServerSocketInterface interfacE;

		public SocketWriter(final PostProcess nextAction,
				final ServerSocketInterface interfacE) {
			super(nextAction);
			this.interfacE = interfacE;
		}

		public SocketWriter(final ServerSocketInterface interfacE) {
			super();
			this.interfacE = interfacE;
		}

		
		@Override
		protected void action(AlloyProcessedResult result)
				throws InterruptedException {
			
			AlloyResponseMessage message = new AlloyResponseMessage(result,
					interfacE.getHostProcess());
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName() + "Start sending a done message: "
						+ message + " as the result is:" + result + " TO: "
						+ interfacE.getRemoteProcess());
			interfacE.sendMessage(message);
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName() + "message sent: " + message);

		}
	}

	public static class DBWriter extends PostProcess {

		final RemoteProcess host;

		public DBWriter(PostProcess nextAction, RemoteProcess host) {
			super(nextAction);
			this.host = host;
		}

		public DBWriter(RemoteProcess host) {
			super();
			this.host = host;
		}

		// A map from url, that sent from server, to a connection pool.
		final Map<DBConnectionInfo, DBConnectionPool> connections = new HashMap<>();

		protected DBConnectionPool getConnection(
				final DBConnectionInfo dBConnectionInfo) throws SQLException {
			if (!connections.containsKey(dBConnectionInfo)) {
				connections.put(dBConnectionInfo,
						new MySQLDBConnectionPool(dBConnectionInfo));
			}
			return connections.get(dBConnectionInfo);
		}

		@Override
		protected void action(AlloyProcessedResult result) {
			try {
				(DBLogger.createDatabaseOperationsObject(
						getConnection(result.getParam().getDBConnectionInfo().get())))
								.insertResult(result, host + "");
			} catch (SQLException e) {
				logger.severe(Utils.threadName()
						+ " Error happened in insertin the result into the database." + e);
			}
		}
	}

	public static class CleanAfterProccessed extends PostProcess {

		public CleanAfterProccessed() {
			super();
		}

		@Override
		protected void action(AlloyProcessedResult result) {
			if (Configuration.IsInDeubbungMode)
				logger.info(Utils.threadName()
						+ "The conent of the param is removed from the disk: "
						+ result.getParam());
			result.getParam().removeContent();
		}
	}

}
