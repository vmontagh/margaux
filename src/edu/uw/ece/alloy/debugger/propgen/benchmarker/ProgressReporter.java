package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.sun.rowset.CachedRowSetImpl;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.PostProcess;
import edu.uw.ece.alloy.util.Utils;

/**
 * A simple class that poll the files in the output directory to see the rate of processing 
 * @author vajih
 *
 */
public class ProgressReporter {

	protected final static Logger logger = Logger.getLogger(PostProcess.class.getName()+"--"+Thread.currentThread().getName());



	public static class ProgressTracker{
		int oldC;
		long deltaT = System.currentTimeMillis();
		long rate = 0;
		long totalRate = 0;
		long workingTotalRate = 0;
		long workingCount = 0;
		long total = 0;
		long count = 0;


		public ProgressTracker(int initialC ){
			oldC = initialC;
		}

		public ProgressTracker( ){
			this(0);
		}

		public String progressDeltaReport(int newC){
			deltaT =  System.currentTimeMillis() - deltaT;
			if(deltaT == 0) return "";
			rate = (newC)*1000/deltaT;
			deltaT = System.currentTimeMillis();			

			total += newC;
			totalRate += rate;
			++count;

			if(newC != 0){
				workingTotalRate += rate;
				++workingCount;
			}

			String result = String.format("Rate,%s,Total,%s,TotalRate,%s,TotalWorkingRate,%s", rate, total, count!=0? totalRate/count:0, workingCount!=0? workingTotalRate/workingCount:0 );

			return result;
		}

		public String progressReport(int newC){
			int deltaC = oldC != 0 ? newC-oldC : newC;
			oldC = newC;
			return progressDeltaReport(deltaC);
		}
	}


	final static File pathLogFiles = new File(Configuration.getProp("temporary_directory"));
	final static File pathDBFiles = new File(Configuration.getProp("db_file_root"));
	final static int refreshRate = Integer.parseInt(Configuration.getProp("reporter_refresh_rate"));
	final static int dbCleaners = Integer.parseInt(Configuration.getProp("db_cleaners"));

	final ProgressTracker fileProgressTracker = new ProgressTracker();
	final ProgressTracker fileMergerProgressTracker = new ProgressTracker();

	final protected static String  logTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

	final protected static DBConnectionPool dBConnection = new MySQLDBConnectionPool(DBConnectionInfo.CONFIGURED_DBConnectionInfo);

	final protected static String OUTPUT_REPORT_EXTENSION = ".*\\.out\\.txt";

	final boolean DO_LAZY;

	final BlockingQueue<File> fileQueue = new LinkedBlockingQueue<File>();

	final File mergedOutputs = new File(pathLogFiles, "merged_output_"+logTime+".log");
	final static long FILE_MERGE_DELAY = 5000;
	private static final String ConnectionURLHeader = "jdbc:h2:tcp://localhost/";

	final private Thread fileMerger = new Thread(){
		public void run() {
			while(true){
				//Wait for a file to become available
				try{
					final File outputFile = fileQueue.take();
					final String content = Util.readAll(outputFile.getAbsolutePath());
					Utils.appendFile(mergedOutputs.getAbsolutePath(), content);
					outputFile.delete();
				}catch(InterruptedException e){
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	};

	private File[] findAllFilesByNameAndBeforeTime(String pattern, File path, long time){

		File[] allFiles = Utils.filesR(pathLogFiles.getAbsolutePath(), pattern);
		return (File[]) Arrays.asList(allFiles).stream().filter(f->f.lastModified() < time).toArray(size->new File[size]);

	}

	private int mergeFiles(){
		long delayed =System.currentTimeMillis() - FILE_MERGE_DELAY;
		File[] files = findAllFilesByNameAndBeforeTime(OUTPUT_REPORT_EXTENSION, pathLogFiles, delayed);

		for(File file: files){
			try {
				fileQueue.put(file);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return files.length;
	}

	public int filesCount(){
		return Utils.filesR(pathLogFiles.getAbsolutePath(), OUTPUT_REPORT_EXTENSION).length;
	}


	public void startMergingThread(){
		fileMerger.start();
		//DBMerger.start();
		//for(int i =0; i < DBCleaners.length; ++i)
		//	DBCleaners[i].start();
	}

	public String fileReporter(){
		final int merged = mergeFiles();
		final int total = /*merged + */filesCount();
		String result = "Merging Files:   "+fileMergerProgressTracker.progressDeltaReport(merged);
		result += "\nFile:            "+fileProgressTracker. progressReport(total);
		result += "\nTo be merged "+fileQueue.size();
		return result;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////

	public class MySQLProgressReporter{
		final protected DBConnectionPool dBConnection = new MySQLDBConnectionPool(DBConnectionInfo.CONFIGURED_DBConnectionInfo);
		private long lastUpdated = 0;

		private CachedRowSetImpl getNewRecords( String  sqlQuery) throws SQLException{

			CachedRowSetImpl result = new CachedRowSetImpl();
			try(final Connection dbConnection = dBConnection.getPooledConnection();){
				try(final PreparedStatement preparedSQLStmt = dbConnection.prepareStatement(sqlQuery)){
					preparedSQLStmt.setLong(1, lastUpdated );
					result.populate(preparedSQLStmt.executeQuery());
				}
			} 
			return result;
		}

		private int exatractCount(final ResultSet rs) {

			int updatedRows = -1;
			try {
				if(rs.next()){
					updatedRows = rs.getInt(1);  
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return updatedRows;
		}

		private int getSpecificNewRecordsCount( final String sat) throws SQLException{
			return exatractCount(getNewRecords( "select count(*) from "+dBConnection.connectionInfo.tablename+" where recordTime > ? and sat = '"+sat+"'"));
		}

		private ResultSet getAllNewRecordsCount() throws SQLException{
			return getNewRecords( "select count(*) from "+dBConnection.connectionInfo.tablename+" where recordTime > ?;");
		}

		private ResultSet getLatestNewRecords() throws SQLException{
			return getNewRecords( "select * from "+dBConnection.connectionInfo.tablename+" where recordTime > ? order by recordTime desc limit 1;");
		}

		private Map<ReportVariables, Integer> processLazyResult() throws SQLException{

			Map<ReportVariables, Integer> report = new HashMap<ProgressReporter.ReportVariables, Integer>();

			for(ReportVariables var: ReportVariables.values()){
				final int count = getSpecificNewRecordsCount( var.toString());
				final int totalCount = !report.containsKey(var) ? count: report.get(var)+count;
				if(totalCount  != 0)
					report.put(var, totalCount );
			}

			ResultSet rs = getAllNewRecordsCount();
			int updatedRows = 0;
			if(rs.next()){

				updatedRows = rs.getInt(1);  

				final ResultSet latestRow = getLatestNewRecords();
				if(latestRow.next()){
					lastUpdated = latestRow.getLong("recordTime") ;
				}
				latestRow.close();

			}
			rs.close();
			//updating the result.
			report.put(ReportVariables.A, !report.containsKey(ReportVariables.A) ? updatedRows: report.get(ReportVariables.A)+updatedRows);
			return report;
		}


		public String dBReporter(){

			String result = "";

			Map<ReportVariables, Integer> report;
			try {
				report = processLazyResult();


				for(final ReportVariables var: report.keySet()){
					result  += var.toString()+":          "+dbTrackers.get(var).progressDeltaReport(report.get(var))+"\n";
				}

				//result += "To be merged: "+dbQueue.size() +" To be deleted: "+ toBeDelete.size() ;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch( Throwable tr){
				tr.printStackTrace();
			}
			return result;

		}


	}


	//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
	
	public ProgressReporter(final boolean doLazy) throws SQLException {

		this.DO_LAZY = doLazy;
		mergedDB = dBConnection.getPooledConnection();

		if(!DO_LAZY){
			//Create the records table
			Statement statement = mergedDB.createStatement();
			statement.execute(DBLogger.SQL_TABLE_SCHEMA);
			statement.close();
		}

		for(ReportVariables var:ReportVariables.values()){
			dbTrackers.put(var, new ProgressTracker());
		}

		/*		DBCleaners = new Thread[10];


		for(int i=0; i < DBCleaners.length; ++i){
			DBCleaners[i] = new Thread(){
					public void run() {
						while(true){
							try {
								cleanDatabase() ;
							} catch (InterruptedException e) {
								logger.severe("The cleaning loop is broken "+e);
								break;					
							}
						}
					}
				};
		}*/

	}

	static{
		try {
			Class.forName(dBConnection.getDriverName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		/*		cpds.setJdbcUrl("jdbc:sqlite:"+Configuration.getProp("db_file_root")+"/"+Configuration.getProp("db_file_name").replace("%t",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-nnnnnnnnn"))).replace("%p", AlloyProcessRunner.getInstance().PID.getHostString()+"-"+AlloyProcessRunner.getInstance().PID.getPort()) );
		cpds.setAcquireIncrement(Integer.valueOf(Configuration.getProp("jdbc_max_increament_size")));
		cpds.setMaxPoolSize(Integer.valueOf(Configuration.getProp("jdbc_max_pool_size")));
		 */	


	}

	final Connection mergedDB;

	Map<String, DBConnectionPool> DBNameToConnectionsMap = new HashMap<>();
	Map<String, Long> DBNameToLastAddedMap = new HashMap<String, Long>();

	private Connection getConnection(final String dbFile) throws SQLException{

		if(!DBNameToConnectionsMap.containsKey(dbFile)){


			DBNameToConnectionsMap.put(dbFile,new H2DBConnectionPool(dbFile));
		}
		//System.out.println("Opening: "+DBNameToConnectionsMap.get(dbFile));
		return DBNameToConnectionsMap.get(dbFile).getPooledConnection();

	}


	private Long getLastRecordTime(final String dbFile){
		if(DBNameToLastAddedMap.containsKey(dbFile)){
			return DBNameToLastAddedMap.get(dbFile);
		}
		return (long) 0;
	}


	private CachedRowSetImpl getNewRecords(final String dbFile, String  sqlQuery) throws SQLException{

		CachedRowSetImpl result = new CachedRowSetImpl();
		try(final Connection dbConnection = getConnection(dbFile);){
			try(final PreparedStatement preparedSQLStmt = dbConnection.prepareStatement(sqlQuery)){
				preparedSQLStmt.setLong(1, getLastRecordTime(dbFile) );
				result.populate(preparedSQLStmt.executeQuery());
			}
		} 
		return result;
	}




	private ResultSet getAllNewRecords(final String dbFile) throws SQLException{
		return getNewRecords(dbFile, "select * from records where recordTime > ? order by recordTime desc;");
	}

	private ResultSet getLatestNewRecords(final String dbFile) throws SQLException{
		return getNewRecords(dbFile, "select * from records where recordTime > ? order by recordTime desc limit 1;");
	}

	private ResultSet getAllNewRecordsCount(final String dbFile) throws SQLException{
		return getNewRecords(dbFile, "select count(*) from records where recordTime > ?;");
	}

	private int exatractCount(final ResultSet rs) {

		int updatedRows = -1;
		try {
			if(rs.next()){
				updatedRows = rs.getInt(1);  
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return updatedRows;
	}




	private enum ReportVariables {
		S,//sat
		U,//unsat
		F,//failed
		T,//time-out
		A //all
	}

	//[(databaseName)->(RecordID)->(Statement for deleting the records)]
	final BlockingQueue<Pair<String, ResultSet>> dbQueue    = new LinkedBlockingQueue<>();
	final BlockingQueue<Pair<String, Integer>>   toBeDelete = new LinkedBlockingQueue<>();
	final ConcurrentMap<String, Map<ReportVariables, Integer>> reports = new ConcurrentHashMap<>();

	final ProgressTracker dbTotalProgressTracker = new ProgressTracker();


	Map<ReportVariables, ProgressTracker> dbTrackers = new HashMap<ProgressReporter.ReportVariables, ProgressReporter.ProgressTracker>();


	final ProgressTracker dbUnSATProgressTracker = new ProgressTracker();
	final ProgressTracker dbSATProgressTracker = new ProgressTracker();
	final ProgressTracker dbTimeoutProgressTracker = new ProgressTracker();
	final ProgressTracker dbFailedProgressTracker = new ProgressTracker();

	final private Thread DBMerger = new Thread(){
		public void run() {
			while(true){
				//Wait for a file to become available
				final Pair<String, ResultSet> recordPair;
				try{
					recordPair = dbQueue.take();
				}catch(InterruptedException e){
					logger.severe("Cannot take from dbQueue "+e);
					break;
				}

				try {
					processEagerResult(recordPair.a, recordPair.b);
				} catch (SQLException e) {
					logger.severe("Cannot merge "+e);
					e.printStackTrace();
				}

			}
		};
	};

	/*final*/ private Thread[] DBCleaners;



	private void cleanDatabase() throws InterruptedException{
		Pair<String, Integer> toBeDeleted = null;
		try {
			toBeDeleted = toBeDelete.take();
			try(final Connection dbConnection = /*DriverManager.getConnection("jdbc:h2:tcp://localhost/"+toBeDeleted.a)*/ getConnection(toBeDeleted.a)){
				try(PreparedStatement preparedSQLStmt = dbConnection.prepareStatement(DBLogger.SQL_DELETE_STMT)){
					preparedSQLStmt.setString(1, String.valueOf(toBeDeleted.b));
					preparedSQLStmt.executeUpdate();
				}
			}
		} catch (InterruptedException e) {
			logger.severe("Cannot take from queue "+e);
			throw e;
		} catch (SQLException e) {
			logger.severe("The record is not deleted "+e);
			//put the record back
			try {
				if(toBeDeleted != null ) toBeDelete.put(toBeDeleted);
			} catch (InterruptedException e1) {
				logger.severe("Cannot put t toBeDelete "+e1);
			}
		}
	}

	private void processEagerResult(final String dbFile,  final ResultSet records) throws SQLException{

		Map<ReportVariables, Integer> report = !reports.containsKey(dbFile) ? new HashMap<>() : reports.get(dbFile);
		boolean isFirst = true;

		while(records.next()){

			if(isFirst){
				DBNameToLastAddedMap.put(dbFile, records.getLong("recordTime"));
				isFirst = false;
			}
			try(PreparedStatement preparedSQLStmt = mergedDB.prepareStatement(DBLogger.SQL_INSERT_STMT)){

				preparedSQLStmt.setString(1, Utils.ClobToString(records.getObject(2) ));
				preparedSQLStmt.setString(2, Utils.ClobToString(records.getObject(3)) );
				preparedSQLStmt.setInt(3, 	 records.getInt(4));
				preparedSQLStmt.setString(4, Utils.ClobToString(records.getObject(5)) );
				preparedSQLStmt.setString(5, Utils.ClobToString(records.getObject(6)) );
				preparedSQLStmt.setString(6, Utils.ClobToString(records.getObject(7)) );
				preparedSQLStmt.setString(7, Utils.ClobToString(records.getObject(8)) );
				preparedSQLStmt.setString(8, Utils.ClobToString(records.getObject(9)) );
				preparedSQLStmt.setInt(9,    records.getInt(10));
				preparedSQLStmt.setString(10,Utils.ClobToString(records.getObject(11)) );
				preparedSQLStmt.setString(11,Utils.ClobToString(records.getObject(12)) );

				try{
					preparedSQLStmt.executeUpdate();
					try {
						toBeDelete.put(new Pair<>( dbFile, records.getInt(1)));
					} catch (InterruptedException e) {
						logger.severe("Cannot put t dbQueue "+e);
					}
				}catch(SQLException e){
					logger.severe("The records is not added to mergedDB"+ e);
				}finally{
				}
			}
			//Analyzing the result
			for(ReportVariables var: ReportVariables.values()){
				if( records.getString("sat").equals(var.toString()) )
					report.put(var, !report.containsKey(var) ? 1: report.get(var)+1);
			}


			report.put(ReportVariables.A, !report.containsKey(ReportVariables.A) ? 1: report.get(ReportVariables.A)+1);

		}
		records.close();

		reports.put(dbFile, report);
	}

	private int getSpecificNewRecordsCount( final String dbFile,  final String sat) throws SQLException{
		return exatractCount(getNewRecords(dbFile, "select count(*) from records where recordTime > ? and sat = '"+sat+"'"));
	}
	

	private void processLazyResult(final String dbFile) throws SQLException{

		Map<ReportVariables, Integer> report = !reports.containsKey(dbFile) ? new HashMap<>() : reports.get(dbFile);

		for(ReportVariables var: ReportVariables.values()){
			final int count = getSpecificNewRecordsCount(dbFile, var.toString());
			final int totalCount = !report.containsKey(var) ? count: report.get(var)+count;
			if(totalCount  != 0)
				report.put(var, totalCount );
		}

		ResultSet rs = getAllNewRecordsCount(dbFile);
		int updatedRows = 0;
		if(rs.next()){

			updatedRows = rs.getInt(1);  

			final ResultSet latestRow = getLatestNewRecords(dbFile);
			if(latestRow.next()){
				DBNameToLastAddedMap.put(dbFile,latestRow.getLong("recordTime") );
			}
			latestRow.close();

		}
		rs.close();
		//updating the result.


		report.put(ReportVariables.A, !report.containsKey(ReportVariables.A) ? updatedRows: report.get(ReportVariables.A)+updatedRows);

		reports.put(dbFile, report);
	}




	public void dBLazyReporter(String dbFile) throws SQLException{
		processLazyResult(dbFile);
	}

	private void dBEagerReporter(String dbFile) throws SQLException{

		final ResultSet records = getAllNewRecords(dbFile);
		try {
			if(!records.isLast())
				dbQueue.put(new Pair<String, ResultSet>(dbFile, records));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public String dBReporter(){

		String result = "";
		final File[] dbFiles = Utils.filesR(pathDBFiles.getAbsolutePath(), "^alloy((?!trace|lock).)*"); 
		//findAllFilesByName("alloy", pathDBFiles);

		for(final File dbFile: dbFiles){
			try {

				String filename = dbFile.getAbsolutePath().replace(".h2.db", "").replace(".mv.db", "");

				if(Configuration.IsInDeubbungMode) logger.info("The DB file is: "+ filename);

				if(! DO_LAZY){
					dBEagerReporter(filename);
				}else{
					dBLazyReporter(filename);
				}
			} catch (SQLException e) {
				result = result + '\n' + e.getMessage();
				logger.severe("There is an sql Error in "+dbFile.getName() +" as "+ e.getMessage());
				logger.severe("There is an sql Error in "+dbFile.getName() +" as "+ e.getStackTrace());
			}

		}

		final Map<ReportVariables, Integer> aggReport = new HashMap<ProgressReporter.ReportVariables, Integer>();

		for(final Map<ReportVariables, Integer> report: reports.values()){
			for(final ReportVariables var: report.keySet()){
				aggReport.put(var, aggReport.containsKey(var) ? report.get(var) + aggReport.get(var): report.get(var));
			}
		}

		for(final ReportVariables var: aggReport.keySet()){
			result  += var.toString()+":          "+dbTrackers.get(var).progressReport(aggReport.get(var))+"\n";
		}

		result += "To be merged: "+dbQueue.size() +" To be deleted: "+ toBeDelete.size() ;

		return result;

	}

	public static void main(String[] args) throws InterruptedException, SQLException {
		
		boolean doLazy = true;

		try{
			doLazy = Boolean.parseBoolean(args[0]);
		}catch(Exception e){}

		ProgressReporter pr = new ProgressReporter(doLazy);

		pr.startMergingThread();

		MySQLProgressReporter mySQLProgressReporter = pr. new MySQLProgressReporter();
		
		while(true){

			System.out.println(pr.fileReporter());

			//System.out.println(pr.dBReporter());
			//System.out.println(mySQLProgressReporter.dBReporter());
			System.out.println("===========================================================================");
			Thread.sleep(refreshRate);


		}

	}




}
