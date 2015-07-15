package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyExecuter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.FrontAlloyProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessesManager;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ProcessRemoteMonitor;

public abstract class RemoteCommand implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 131434234232434L;

	final static Logger logger = Logger.getLogger(RemoteCommand.class.getName()+"--"+Thread.currentThread().getName());;

	public RemoteCommand(){
	}

	public static void sendACommand(final InetSocketAddress remoteAddres, Object command) throws InterruptedException{

		final Logger logger = Logger.getAnonymousLogger();

		AsynchronousSocketChannel clientSocketChannel = null;
		ObjectOutputStream oos = null;
		try {
			clientSocketChannel = AsynchronousSocketChannel
					.open();

			Future<Void> connectFuture = clientSocketChannel.connect(remoteAddres);
			connectFuture.get(); // Wait until connection is done.
			OutputStream os = Channels.newOutputStream(clientSocketChannel);
			oos = new ObjectOutputStream(os);
			oos.writeObject(command);
		} catch (IOException | ExecutionException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] "+"Failed on sending the done message " + command +" TO ="+remoteAddres, e);
			throw new RuntimeException("Unseccesful sending message "+ e.getMessage());
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] "+"Sending the done message is interrupted: "+command+" TO ="+remoteAddres, e);
			throw e;
		}finally{
			if(oos !=null)
				try {
					oos.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] "+"Failed to close the output stream" + command+" TO ="+remoteAddres, e);
				}
			if(clientSocketChannel != null && clientSocketChannel.isOpen())
				try {
					clientSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] "+"Failed to close the socket" + command+" TO ="+remoteAddres, e);
				}
		}
	}

	@SuppressWarnings("static-access")
	public  void sendMe(final InetSocketAddress remoteAddres) throws InterruptedException{
		this.sendACommand(remoteAddres, this);
	}

	public void findRemoteAddress( FrontAlloyProcess front) {
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for findRemoteAddress");
	}

	public void terminate(final AsynchronousSocketChannel param){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for terminate");
	}

	public void process(AlloyExecuter executer) {
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");		
	}

	public void killProcess(ProcessesManager manager){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}

	public void updatePorcessorLiveness(final ProcessesManager manager){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}

	public void processDone(final ProcessRemoteMonitor monitor){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}

	public void activateMe(ProcessesManager manager){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] "+"Inappropriate call for process");
	}
}
