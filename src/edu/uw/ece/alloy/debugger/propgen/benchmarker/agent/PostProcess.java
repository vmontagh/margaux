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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParamLazyCompressing;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.H2DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.SQLiteDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.AlloyProcessed;


public abstract class PostProcess implements Runnable {

	public final BlockingQueue<AlloyProcessedResult> results = new LinkedBlockingQueue<>();
	public final PostProcess nextAction;

	public volatile AtomicInteger processed = new AtomicInteger(0);

	protected final static Logger logger = Logger.getLogger(PostProcess.class.getName()+"--"+Thread.currentThread().getName());
	final public static boolean doCompress = Boolean.valueOf(Configuration.getProp("doCompressAlloyParams"));
	
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
	
	protected String converSATResult(AlloyProcessedResult result) {
		return result.isFailed() ? "F": result.isTimedout() ? "T" : result.sat == 1 ? "S" : result.sat == -1 ? "U": "N";
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

		public FileWrite() {
			super();
		}
		
		public FileWrite(PostProcess socketWriter) {
			super(socketWriter);
		}

		@Override
		protected void action(AlloyProcessedResult result) {

			String alloySource = result.params.getSourceFileName();

			String content = result.asRecordHeader()+",propa,propb,op,sat" +"\n" + result.asRecord() + 
					","+result.params.alloyCoder.predNameA+","+result.params.alloyCoder.predNameB+","+result.params.alloyCoder.commandOperator()+","+converSATResult(result);

			logger.info("["+Thread.currentThread().getName()+"] " +"Start wirint on file: "+result+"   "+content);

			try {
				Util.writeAll(result.params.destPath().getAbsolutePath(), content);
				logger.info("["+Thread.currentThread().getName()+"] " +"result is written in: "+result.params.destPath().getAbsolutePath());
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
			logger.info("["+Thread.currentThread().getName()+"] " +"Start sending a done message: "+command+" as the result is:"+result+" TO: "+ remoteAddres);

			try {
				command.send(remoteAddres);
				logger.info("["+Thread.currentThread().getName()+"] " +"message sent: "+command);
			} catch (InterruptedException e) {
				logger.severe("["+Thread.currentThread().getName()+"] " +"Sending the result is interrupted: "+ result+ " TO: "+ remoteAddres);
				throw e;
			}


		}

	}


	public static class DBWriter extends PostProcess{

		final public static String RECORDS_SCHEMA = "CREATE TABLE RECORDS(id INTEGER PRIMARY KEY AUTO_INCREMENT, result TEXT, params TEXT, date NUMERIC, prop1 TEXT, prop2 TEXT, op char(20), sat char(2), recordTime NUMERIC, recordsTimeConfirm NUMERIC, srcPath TEXT, destPath TEXT)";
		final public static String RECORDS_INDEX = "CREATE INDEX IDX_RECORDS_SAT ON RECORDS(SAT)";
		final public static String SQL_INSERT_STMT = "INSERT INTO records(result, params, date, prop1, prop2, op, sat, recordTime, recordsTimeConfirm, srcPath, destPath) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		final public static DBConnectionPool dBConnection = new H2DBConnectionPool(Configuration.getProp("db_file_root")+"/"+Configuration.getProp("db_file_name")
						.replace("%t",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-nnnnnnnnn")))
							.replace("%p", AlloyProcessRunner.getInstance().PID.getHostString()+"-"+AlloyProcessRunner.getInstance().PID.getPort())
							);
		
		public DBWriter(){
			super();
		}
		
		public DBWriter(PostProcess nextAction){
			super(nextAction);
		}
		
		static{
			//Create an appropriate table in the database. 
			try(Connection connection  = dBConnection.getConnection()){
				connection.setAutoCommit(true);
				try(Statement  statement = connection.createStatement()){
					try(ResultSet rs = statement.executeQuery("SELECT * FROM information_schema.tables WHERE table_name='RECORDS'")){
						logger.info("["+Thread.currentThread().getName()+"] " +"Trying to drop the old database");
						if(rs.next() && Boolean.valueOf(Configuration.getProp("db_renew"))){//There exists a table called records, so lets drop it first and create a new one.
							logger.info("["+Thread.currentThread().getName()+"] " +"The old database is dropped.");
							statement.execute("drop table if exists records");
							statement.execute(RECORDS_SCHEMA);
							statement.execute(RECORDS_INDEX);
							statement.close();
						}else if(!rs.next()){
							statement.execute(RECORDS_SCHEMA);
							statement.execute(RECORDS_INDEX);
							statement.close();
						}
					}
					
				}
				logger.info("["+Thread.currentThread().getName()+"] " +"Database is created as: "+connection);
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] " +"Failed on creating database:" , e);
				//throw new RuntimeException("An exception happened while storing the result in the database: "+e.toString());
			}		
		}


		@Override
		protected void action(AlloyProcessedResult result)  {
			
			logger.info("["+Thread.currentThread().getName()+"] " +"Inserting a record in the database: "+result);
			try(Connection connection  = dBConnection.getConnection()){
				connection.setAutoCommit(true);
				try(Statement  statement = connection.createStatement()){
					
					String SATResult = converSATResult(result);
					
					//Extract properties and operator
					PreparedStatement preparedSQLStmt = connection.prepareStatement(SQL_INSERT_STMT);
					
					preparedSQLStmt.setString(1, result.asRecord());
					preparedSQLStmt.setString(2, result.params.content());
					preparedSQLStmt.setInt(3, -1);
					preparedSQLStmt.setString(4, result.params.alloyCoder.predNameA);
					preparedSQLStmt.setString(5, result.params.alloyCoder.predNameB);
					preparedSQLStmt.setString(6, result.params.alloyCoder.srcNameOperator());
					preparedSQLStmt.setString(7, SATResult);
					preparedSQLStmt.setLong(8, System.currentTimeMillis() /*LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))*/);
					preparedSQLStmt.setInt(9, -1);
					preparedSQLStmt.setString(10, result.params.alloyCoder.srcPath().getName());
					preparedSQLStmt.setString(11, result.params.alloyCoder.destPath().getName());
					
					//System.out.println(preparedSQLStmt);
					
					preparedSQLStmt.executeUpdate();
				}
				
				logger.info("["+Thread.currentThread().getName()+"] " +"The records is inserted in the DB: "+result);
			} catch (SQLException e) {
				e.printStackTrace();
				logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"] " +"Failed on storing the result in DB: "+result, e);
				//throw new RuntimeException("An exception happened while storing the result in the database: "+e.toString());
			}		
		}


	}


}
