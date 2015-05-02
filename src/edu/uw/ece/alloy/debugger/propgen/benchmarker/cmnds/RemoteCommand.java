package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.FrontAlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public abstract class RemoteCommand implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 131434234232434L;
	
	final static Logger logger = Logger.getLogger(RemoteCommand.class.getName()+"--"+Thread.currentThread().getName());;
	
	public RemoteCommand(){
	}
	
	public static void sendACommand(final SocketAddress remoteAddres, Object command) throws InterruptedException{
		
		final Logger logger = Logger.getAnonymousLogger();

		AsynchronousSocketChannel clientSocketChannel;
		try {
			clientSocketChannel = AsynchronousSocketChannel
					.open();
		
		Future<Void> connectFuture = clientSocketChannel.connect(remoteAddres);
		connectFuture.get(); // Wait until connection is done.
		OutputStream os = Channels.newOutputStream(clientSocketChannel);
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(command);
		oos.close();
		clientSocketChannel.close();
		} catch (IOException | ExecutionException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] "+"Failed on sending the done message " + command, e);
			throw new RuntimeException("Unseccesful sending message "+ e.getMessage());
		} catch (InterruptedException e) {
			logger.info("["+Thread.currentThread().getName()+"] "+"Sendining the done message is interrupted: "+command);
			throw e;
		}
	}
	
	@SuppressWarnings("static-access")
	public  void sendMe(final SocketAddress remoteAddres) throws InterruptedException{
		this.sendACommand(remoteAddres, this);
	}

	public void findRemoteAddress( FrontAlloyProcess front) {
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for findRemoteAddress");
	}
	
	public void terminate(final AsynchronousSocketChannel param){
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for terminate");
	}

	public void process(AlloyExecuter executer) {
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");		
	}
	
	public void killProcess(ProcessesManager manager){
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}
	
	public void updatePorcessorLiveness(final ProcessesManager manager){
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}

	public void processDone(ProcessRemoteMonitor monitor, final ProcessesManager manager){
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}
	
	public void activateMe(ProcessesManager manager){
		logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}
}
