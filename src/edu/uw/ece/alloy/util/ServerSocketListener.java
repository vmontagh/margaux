package edu.uw.ece.alloy.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.pattern.PatternsAnalyzer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadDelayToBeMonitored;

public abstract class ServerSocketListener implements Runnable, ThreadDelayToBeMonitored {

	protected final static Logger logger = Logger.getLogger(ServerSocketListener.class.getName()+"--"+Thread.currentThread().getName());

	public final InetSocketAddress hostAddress;
	public final InetSocketAddress remoteAddress;

	public ServerSocketListener(final int hostPort, final int remotePort) {
		this.remoteAddress = new InetSocketAddress(remotePort);
		this.hostAddress = new InetSocketAddress(hostPort);
	}

	public ServerSocketListener(final String hostName, final int hostPort, final String remoteName, final int remotePort) {
		this.hostAddress = new InetSocketAddress(hostName, hostPort);
		this.remoteAddress = new InetSocketAddress(remoteName, remotePort);
	}

	public ServerSocketListener(final InetSocketAddress hostAddress, final InetSocketAddress remoteAddress) {
		this.hostAddress = hostAddress;
		this.remoteAddress = remoteAddress;
	}

	protected void processCommand(final RemoteCommand command){
		// Supposed to be a registering call;
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" Recieved message: "+command);

	}

	protected void onStartingListening() throws InterruptedException{
		if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" Sending a readyness message pId: "+hostAddress.getPort());
		(new ProcessReady(hostAddress)).sendMe(remoteAddress);
	}
	
	public void listening(){

		AsynchronousServerSocketChannel serverSocketChannel = null;

		try {
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" Starting listening fornt runner for pId: "+hostAddress.getPort());
			serverSocketChannel = AsynchronousServerSocketChannel
					.open().bind(hostAddress);
			
			onStartingListening();

			Future<AsynchronousSocketChannel> serverFuture = null;
			AsynchronousSocketChannel clientSocket = null;
			InputStream connectionInputStream = null;
			ObjectInputStream ois = null;
			
			while(!Thread.currentThread().isInterrupted()){
				try{
					if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" waiting for a request: "+hostAddress.getPort());
					serverFuture = serverSocketChannel
							.accept();
					if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] "+" a message is received: "+hostAddress.getPort());
					clientSocket = serverFuture.get();

					if ((clientSocket != null) && (clientSocket.isOpen())) {
						connectionInputStream = Channels.newInputStream(clientSocket);
						ois = new ObjectInputStream(connectionInputStream);

						processCommand( (RemoteCommand)ois.readObject() );
					}
				} catch (EOFException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while listening for request: ", e);
				} catch (IOException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while listening for request: ", e);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while listening for request: ", e);
				} catch (ExecutionException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while listening for request: ", e);
				} catch (ClassNotFoundException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while listening for request: ", e);
				}finally{
					if(ois != null)
						try{
							ois.close();
						}catch(IOException e){
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing InputOutputstream: ", e);
						}
					if(connectionInputStream != null)
						try{
							connectionInputStream.close();
						}catch(IOException e){
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing Connection Inputputstream: ", e);
						}
					if(clientSocket!=null && clientSocket.isOpen())
						try{
							clientSocket.close();
						}catch(IOException e){
							logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing Client socket: ", e);
						}
				}
			}

		} catch (Throwable t  ){
			logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"A serious error breaks the Front Processor listener: ", t);
		} finally{
			if(serverSocketChannel!=null && serverSocketChannel.isOpen())
				try {
					serverSocketChannel.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, "["+Thread.currentThread().getName()+"] "+"Error while closing AsynchronousServerSocketChannel socket: ", e);
				}
		}

	}
	
	protected abstract Thread getThread();

	public void startThread() {
		getThread().start();
	}

	@Override
	public void run() {
		listening();
	}
}
