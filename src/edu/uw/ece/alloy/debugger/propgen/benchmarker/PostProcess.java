package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PostProcess.SocketWriter;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AlloyProcessed;


public abstract class PostProcess implements Runnable {

	public final BlockingQueue<AlloyProcessedResult> results = new LinkedBlockingQueue<>();
	public final PostProcess nextAction;

	public volatile AtomicInteger processed = new AtomicInteger(0);

	protected final static Logger logger = Logger.getLogger(PostProcess.class.getName()+"--"+Thread.currentThread().getName());

	public PostProcess(PostProcess nextAction) {
		super();
		this.nextAction = nextAction;

	}

	public PostProcess() {
		this( null);
	}

	public boolean isEmpty(){
		return results.isEmpty();
	}
	
	public void doAction(final AlloyProcessedResult result) throws InterruptedException{
		try {
			logger.info("["+Thread.currentThread().getName()+"] " +"Post process: "+result);
			results.put(result);
		} catch (InterruptedException e) {
			logger.info("["+Thread.currentThread().getName()+"] " +"Processing a new result is interupted: "+result);
			throw e;
		}
	}

	protected abstract void action(final AlloyProcessedResult result) throws InterruptedException;

	protected void actionAndIncreament(final AlloyProcessedResult result) throws InterruptedException{
		action(result);
		processed.incrementAndGet();
	}

	protected void doSerialAction(final AlloyProcessedResult result) throws InterruptedException{

		this.actionAndIncreament(result);

		if(nextAction != null)
			nextAction.doSerialAction(result);

	}



	@Override
	public void run() {

		AlloyProcessedResult result = null;
		try {
			while(!Thread.currentThread().isInterrupted()){

				result = results.take();
				logger.info("["+Thread.currentThread().getName()+"] " +"Start a Post process: "+result);

				doSerialAction(result);

			}

		} catch (InterruptedException e) {
			logger.info("["+Thread.currentThread().getName()+"] " +"Processing a result is interrupted: "+ result+" after processed "+processed+" results.");
		}
	}

	public void cancel() { Thread.currentThread().interrupt(); }

	public static class FileWrite extends PostProcess{

		public FileWrite(SocketWriter socketWriter) {
			super(socketWriter);
		}

		@Override
		protected void action(AlloyProcessedResult result) {

			String alloySource = result.params.getSourceFileName();

			String content = result.asRecordHeader()+",FileName" +"\n" + result.asRecord() + ","+alloySource;

			logger.info("["+Thread.currentThread().getName()+"] " +"Start wirint on file: "+result+"   "+content);

			try {
				Util.writeAll(result.params.destPath.getAbsolutePath(), content);
				logger.info("["+Thread.currentThread().getName()+"] " +"result is written in: "+result.params.destPath.getAbsolutePath());
			} catch (Err e) {
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] " +"Failed on storing the result: "+result, e);
			}
		}

	}


	public static class SocketWriter extends PostProcess{

		final InetSocketAddress remoteAddres;

		public SocketWriter(final PostProcess nextAction, final InetSocketAddress remoteAddress){
			super(nextAction);
			this.remoteAddres = remoteAddress;
		}

		public SocketWriter(final InetSocketAddress remoteAddress){
			super();
			this.remoteAddres = remoteAddress;
		}

		
		@Override
		protected void action(AlloyProcessedResult result) throws InterruptedException  {

			AlloyProcessed command = new AlloyProcessed(AlloyProcessRunner.getInstance().PID, result.params);
			logger.info("["+Thread.currentThread().getName()+"] " +"Start sending a done message: "+command+" to "+ remoteAddres);
			
			try {
				command.sendMe(remoteAddres);
				logger.info("["+Thread.currentThread().getName()+"] " +"message sent: "+command);
			} catch (InterruptedException e) {
				logger.info("["+Thread.currentThread().getName()+"] " +"Sending the result is interrupted: "+ result+ " to: "+ remoteAddres);
				throw e;
			}


		}

	}


}
