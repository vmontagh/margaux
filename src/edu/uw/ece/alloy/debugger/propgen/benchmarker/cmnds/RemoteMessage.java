/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcessManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;
import edu.uw.ece.alloy.util.Utils;
import edu.uw.ece.alloy.util.events.EventArgs;
import edu.uw.ece.alloy.util.events.EventListener;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

/**
 * The abstract class defining the general structure of the messages are sent to
 * a remote analyzer or receiving the result.
 * 
 * @author vajih
 *
 */
public abstract class RemoteMessage implements Serializable {

	private static final long serialVersionUID = -5491640781826368521L;
	/* The sender's process ID */
	public final RemoteProcess process;
	/* The time of the message creation */
	public final long creationTime;
	
	final static Logger logger = Logger.getLogger(
			RemoteMessage.class.getName() + "--" + Thread.currentThread().getName());;

	public RemoteMessage(RemoteProcess process, long creationTime) {
		super();
		this.process = process;
		this.creationTime = creationTime;
	}
	
	public RemoteMessage(RemoteProcess process){
		this(process, System.currentTimeMillis());
	}
	
	public static void sendAMessage(final RemoteProcess remoteProcess,
			Serializable message) throws InterruptedException {

		final Logger logger = Logger.getAnonymousLogger();

		AsynchronousSocketChannel clientSocketChannel = null;
		ObjectOutputStream oos = null;
		try {
			clientSocketChannel = AsynchronousSocketChannel.open();

			Future<Void> connectFuture = clientSocketChannel
					.connect(remoteProcess.getAddress());
			connectFuture.get(); // Wait until connection is done.
			OutputStream os = Channels.newOutputStream(clientSocketChannel);
			oos = new ObjectOutputStream(os);
			oos.writeObject(message);
		} catch (IOException | ExecutionException e) {
			logger.log(Level.SEVERE,
					Utils.threadName() + "Failed on sending the done message " + message
							+ " TO =" + remoteProcess,
					e);
			throw new RuntimeException(
					"Unseccesful sending message " + e.getMessage());
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,
					Utils.threadName() + "Sending the done message is interrupted: "
							+ message + " TO =" + remoteProcess,
					e);
			throw e;
		} finally {
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE,
							Utils.threadName() + "Failed to close the output stream" + message
									+ " TO =" + remoteProcess,
							e);
				}
			if (clientSocketChannel != null && clientSocketChannel.isOpen())
				try {
					clientSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE,
							Utils.threadName() + "Failed to close the socket" + message
									+ " TO =" + remoteProcess,
							e);
				}
		}
	}

	/**
	 * Prepares the data before the request is done on the client. 
	 * @throws InterruptedException
	 */
	public void prepareBeforeUse() throws InterruptedException{
	}
	
	/**
	 * Prepares a message before sending a messing.
	 * Request and Response Messages overwrites this API 
	 * @param remoteProcess
	 * @throws InterruptedException 
	 * @throws Exception 
	 */
	public void prepareThenSend(final RemoteProcess remoteProcess) throws InterruptedException, Exception{
		sendMe(remoteProcess);
	}
	
	@SuppressWarnings("static-access")
	public void sendMe(final RemoteProcess remoteProcess)
			throws InterruptedException {
		beforeSend(process);
		this.sendAMessage(remoteProcess, this);
		afterSend(process);
	}
	
	protected void beforeSend(RemoteProcess process){}
	protected void afterSend(RemoteProcess process){}

	/**
	 * The action happens once a message is processed on the callee side. A caller
	 * makes an object of the message and send to the callee. The callee has to
	 * take an action and changes it state w.r.t to the message. The current state
	 * is passed through the context.
	 * 
	 * @param context
	 * @throws InvalidParameterException
	 */
	public abstract void onAction(final Map<String, Object> context)
			throws InvalidParameterException;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
		result = prime * result + ((process == null) ? 0 : process.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RemoteMessage other = (RemoteMessage) obj;
		if (creationTime != other.creationTime) {
			return false;
		}
		if (process == null) {
			if (other.process != null) {
				return false;
			}
		} else if (!process.equals(other.process)) {
			return false;
		}
		return true;
	}

	/**
	 * Once an event happens, this method is called and an implementation
	 * of a listener is passed in.
	 * @param listener
	 */
	public abstract void onEvent(MessageListenerAction listener, MessageEventArgs args);
	
}
