package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.swing.internal.plaf.synth.resources.synth;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParamLazyCompressing;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.H2DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.SQLiteDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AlloyProcessed;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.watchdogs.ThreadDelayToBeMonitored;


public abstract class PostProcess implements Runnable, ThreadDelayToBeMonitored{

	protected final BlockingQueue<AlloyProcessedResult> results = new LinkedBlockingQueue<>();
	public final PostProcess nextAction;

	protected volatile AtomicInteger processed = new AtomicInteger(0);

	protected volatile AtomicInteger shadowProcessed = new AtomicInteger(-1);
	protected volatile AtomicInteger recoveredTry = new AtomicInteger(0);

	protected final static Logger logger = Logger.getLogger(PostProcess.class.getName()+"--"+Thread.currentThread().getName());
	final public static boolean doCompress = Boolean.valueOf(Configuration.getProp("doCompressAlloyParams"));

	Thread postProcessThread = new Thread(this);;

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

	public String amIStuck(){
		return isDelayed() == 0 ? "" :  "Processing PostProess"+getClass().getSimpleName()+" is stuck after processing "+processed+" messages.";
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
	 * If the delay condition is true, then it returns a message how many 
	 * messages are stuck in the queue. 
	 * This function is useful for monitoring the threads.
	 * @return
	 * Non-empty message means a delay is recorded
	 * Empty message
	 */
	public synchronized long isDelayed(){
		long result = 0;
		//monitor the socket
		if(shadowProcessed.get() == processed.get() && !isEmpty()){
			//The executer does not proceeded.
			result = processed.longValue(); 
			//TODO manage to reset the socket thread
		}else{
			//The executer proceeded
			shadowProcessed.set(processed.get());
		}
		return result;
	}

	public int triesOnStuck(){
		return recoveredTry.get();
	}

	protected void doActionOnStuck(){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] " +"");
	}

	public void actionOnStuck(){
		triesOnStuck();
	}

	public void startThread(){
		postProcessThread.start();
	}

	public void actionOnNotStuck(){
		if(Configuration.IsInDeubbungMode) logger.finer("["+Thread.currentThread().getName()+"] " +"");
	}

	public void doAction(final AlloyProcessedResult result) throws InterruptedException{
		try {
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"Post process: "+result);
			results.put(result);
		} catch (InterruptedException e) {
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"Processing a new result is interupted: "+result);
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
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"Start a Post process: "+result);

				doSerialAction(result);

			}

		} catch (InterruptedException e) {
			logger.severe("["+Thread.currentThread().getName()+"] " +"Processing a result is interrupted: "+ result+" after processed "+processed+" results.");
		}
	}

	public void cancel() { Thread.currentThread().interrupt(); }





	public static class FileWrite extends PostProcess{

		public FileWrite() {
			super();
		}

		public FileWrite(PostProcess socketWriter) {
			super(socketWriter);
		}

		@Override
		protected void action(AlloyProcessedResult result) {

			String content = result.asRecordHeader()+",propa,propb,op,sat" +"\n" + result.asRecord() + 
					","+result.params.alloyCoder.predNameA+","+result.params.alloyCoder.predNameB+","+result.params.alloyCoder.commandOperator()+","+DBLogger. convertSATResult(result);

			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"Start wirint on file: "+result+"   "+content);

			try {
				Util.writeAll(result.params.destPath().getAbsolutePath(), content);
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"result is written in: "+result.params.destPath().getAbsolutePath());
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

			AlloyProcessedResult updatedResult = result;

			AlloyProcessed command = new AlloyProcessed(AlloyProcessRunner.getInstance().PID, updatedResult);
			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"Start sending a done message: "+command+" as the result is:"+result+" TO: "+ remoteAddres);

			try {
				command.send(remoteAddres);
				if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " +"message sent: "+command);
			} catch (InterruptedException e) {
				logger.severe("["+Thread.currentThread().getName()+"] " +"Sending the result is interrupted: "+ result+ " TO: "+ remoteAddres);
				throw e;
			}


		}

	}





	public static class DBWriter extends PostProcess{

		//A map from url, that sent from server, to a connection pool.
		final Map<DBConnectionInfo, DBConnectionPool> connections = new HashMap<>();
		
		protected DBConnectionPool getConnection(final DBConnectionInfo dBConnectionInfo) throws SQLException{
			if( !connections.containsKey(dBConnectionInfo) ){
				connections.put(dBConnectionInfo, new MySQLDBConnectionPool(dBConnectionInfo));
			}

			return connections.get(dBConnectionInfo);
		}

		@Override
		protected void action(AlloyProcessedResult result)  {

			try {
				(DBLogger.createDatabaseOperationsObject(getConnection(result.params.dBConnectionInfo)) ).insertResult(result,
						/* AlloyProcessRunner.getInstance().PID.toString()*/
						"1");
			} catch (SQLException e) {
				logger.severe("["+Thread.currentThread().getName()+"] " +" Error happened in insertin the result into the database."+e);
			}
					
		}


	}




	public static class CleanAfterProccessed extends PostProcess{

		public CleanAfterProccessed() {
			super();
		}

		@Override
		protected void action(AlloyProcessedResult result) {

			if(Configuration.IsInDeubbungMode) logger.info("["+Thread.currentThread().getName()+"] " + "The conent of the param is removed from the disk: "+ result.params);
			result.params.removeContent();

		}

	}

}
