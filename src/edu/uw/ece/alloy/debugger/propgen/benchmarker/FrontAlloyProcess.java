package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessIt;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ProcessReady;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteCommand;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.Terminate;

public class FrontAlloyProcess implements Runnable {

	public final InetSocketAddress hostAddress;
	private final InetSocketAddress remoteAddress;
	private final AlloyExecuter executer; 
	protected final static Logger logger = Logger.getLogger(FrontAlloyProcess.class.getName()+"--"+Thread.currentThread().getName());

	public FrontAlloyProcess(final int hostPort, final int remotePort, final AlloyExecuter executer) {
		this.remoteAddress = new InetSocketAddress(remotePort);
		this.hostAddress = new InetSocketAddress(hostPort);
		this.executer = executer;
	}

	public FrontAlloyProcess(final String hostName, final int hostPort, final String remoteName, final int remotePort,  final AlloyExecuter executer) {
		this.hostAddress = new InetSocketAddress(hostName, hostPort);
		this.remoteAddress = new InetSocketAddress(remoteName, remotePort);
		this.executer = executer;
	}

	public FrontAlloyProcess(final InetSocketAddress hostAddress, final InetSocketAddress remoteAddress, final AlloyExecuter executer) {
		this.hostAddress = hostAddress;
		this.remoteAddress = remoteAddress;
		this.executer = executer;
	}

	public InetSocketAddress getRemoteAddress(){
		while(remoteAddress == null){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"] " +"The thread is waiting for the remote address, but interrupted.");
			}
		}

		return remoteAddress;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress){
		remoteAddress = new InetSocketAddress(remoteAddress.getAddress(), remoteAddress.getPort());
	}

	private void processCommand(final RemoteCommand command){
		//Supposed to be a registering call;
		logger.info("["+Thread.currentThread().getName()+"] "+" Recieved message: "+command);
		command.process(executer);
	}

	public void listening(){

		AsynchronousServerSocketChannel serverSocketChannel = null;

		try {
			logger.info("["+Thread.currentThread().getName()+"] "+" Starting listeing fornt runner for pId: "+hostAddress.getPort());
			serverSocketChannel = AsynchronousServerSocketChannel
					.open().bind(hostAddress);
			logger.info("["+Thread.currentThread().getName()+"] "+" Sending a readyness message pId: "+hostAddress.getPort());
			(new ProcessReady(+hostAddress.getPort())).sendMe(remoteAddress);

			Future<AsynchronousSocketChannel> serverFuture = null;
			AsynchronousSocketChannel clientSocket = null;
			InputStream connectionInputStream = null;
			ObjectInputStream ois = null;

			while(!Thread.currentThread().isInterrupted()){
				try{
					logger.info("["+Thread.currentThread().getName()+"] "+" waiting for a request: "+hostAddress.getPort());
					serverFuture = serverSocketChannel
							.accept();
					logger.info("["+Thread.currentThread().getName()+"] "+" a message is received: "+hostAddress.getPort());
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

	@Override
	public void run() {
		listening();
	}

	public void stop(){
		Thread.currentThread().interrupt();
	}


	public static void main(String... args) throws InterruptedException{

		AlloyExecuter executer = AlloyExecuter.getInstance();
		FrontAlloyProcess f = new FrontAlloyProcess(45321, -1, executer);
		ProcessesManager manager = null;// new ProcessesManager();

		(new Thread(f)).start();

		while(true){
			Thread.sleep(500);
			ProcessIt c = new ProcessIt(new AlloyProcessingParam(new File("."),new File(".."),1), manager);
			c.sendMe(new InetSocketAddress(45321));
			//Thread.sleep(1000);

			ProcessIt c2 = new ProcessIt(new AlloyProcessingParam(new File("user"),new File(".."),2), manager);
			c2.sendMe(new InetSocketAddress(45321));

			//Thread.sleep(1000);
			Terminate c3 = new Terminate();
			c3.sendMe(new InetSocketAddress(45321));
		}

	}

}
