package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

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

			deltaT =  System.currentTimeMillis() - deltaT;
			if(deltaT == 0) return "";
			rate = (newC - oldC)*1000/deltaT;

			String result;

			if(newC == 0 )
				count = 0;
			else
				count++;
			if(newC > 0){
				totalRate += rate;
			}else{
				totalRate = 0;
			}

			result = String.format("Rate,%s,Total,%s,TotalRate,%s", rate, newC, count!=0? totalRate/count:0 );

			deltaT = System.currentTimeMillis();
			oldC = newC ;

			return result;
		}
	}


	final static File pathLogFiles = new File(Configuration.getProp("temporary_directory"));
	final static File pathDBFiles = new File(Configuration.getProp("db_file_root"));
	final static int refreshRate = Integer.valueOf(Configuration.getProp("reporter_refresh_rate"));

	final ProgressTracker fileProgressTracker = new ProgressTracker();
	final ProgressTracker fileMergerProgressTracker = new ProgressTracker();
	final ProgressTracker dbTotalProgressTracker = new ProgressTracker();

	final ProgressTracker dbUnSATProgressTracker = new ProgressTracker();
	final ProgressTracker dbSATProgressTracker = new ProgressTracker();
	final ProgressTracker dbTimeoutProgressTracker = new ProgressTracker();
	final ProgressTracker dbFailedProgressTracker = new ProgressTracker();

	final protected static String  logTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

	final protected static DBConnectionPool dBConnection = new H2DBConnectionPool(Configuration.getProp("db_file_root")+"/"+"merged_"+logTime+".db");

	final protected static String OUTPUT_REPORT_EXTENSION = ".*\\.out\\.txt";

	final boolean DO_LAZY;

	final BlockingQueue<File> queue = new LinkedBlockingQueue<File>();
	final File mergedOutputs = new File(pathLogFiles, "merged_output_"+logTime+".log");
	final static long FILE_MERGE_DELAY = 5000;

	final private Thread fileMerger = new Thread(){
		public void run() {
			while(true){
				//Wait for a file to become available
				try{
					final File outputFile = queue.take();
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
				queue.put(file);
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
	}

	public String fileReporter(){
		final int merged = mergeFiles();
		final int total = /*merged + */filesCount();
		String result = "Merging Files:   "+fileMergerProgressTracker.progressDeltaReport(merged);
		return result += "\nFile:            "+fileProgressTracker. progressReport(total);
	}

	public ProgressReporter(final boolean doLazy) throws SQLException {

		this.DO_LAZY = doLazy;
		mergedDB = dBConnection.getConnection();
		
		if(!DO_LAZY){
			//Create the records table
			Statement statement = mergedDB.createStatement();
			statement.execute(PostProcess.DBWriter.RECORDS_SCHEMA);
			statement.close();
		}
	}

	static{
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		/*		cpds.setJdbcUrl("jdbc:sqlite:"+Configuration.getProp("db_file_root")+"/"+Configuration.getProp("db_file_name").replace("%t",LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-nnnnnnnnn"))).replace("%p", AlloyProcessRunner.getInstance().PID.getHostString()+"-"+AlloyProcessRunner.getInstance().PID.getPort()) );
		cpds.setAcquireIncrement(Integer.valueOf(Configuration.getProp("jdbc_max_increament_size")));
		cpds.setMaxPoolSize(Integer.valueOf(Configuration.getProp("jdbc_max_pool_size")));
		 */	


	}

	final Connection mergedDB;

	Map<String, Connection> DBNameToConnectionsMap = new HashMap<String, Connection>();
	Map<String, Long> DBNameToLastAddedMap = new HashMap<String, Long>();

	private Connection getConnection(final String dbFile) throws SQLException{

		//if(!DBNameToConnectionsMap.containsKey(dbFile)){
		DBNameToConnectionsMap.put(dbFile,DriverManager.getConnection("jdbc:h2:tcp://localhost/"+dbFile));
		//}
		//System.out.println("Opening: "+DBNameToConnectionsMap.get(dbFile));
		return DBNameToConnectionsMap.get(dbFile);

	}

	private void closeConnection(final String dbFile) throws SQLException{
		//System.out.println("Closing: "+DBNameToConnectionsMap.get(dbFile));

		if(DBNameToConnectionsMap.containsKey(dbFile) && !DBNameToConnectionsMap.get(dbFile).isClosed()){
			DBNameToConnectionsMap.get(dbFile).close();
			DBNameToConnectionsMap.remove(dbFile);
		}
	}

	private Long getLastRecordTime(final String dbFile){
		if(DBNameToLastAddedMap.containsKey(dbFile)){
			return DBNameToLastAddedMap.get(dbFile);
		}
		return (long) 0;
	}


	private ResultSet getNewRecords(final String dbFile, String  sqlQuery) throws SQLException{
		final Connection dbConnection = getConnection(dbFile);
		final Statement  statement = dbConnection.createStatement();
		try{

			final String sqlStmt = sqlQuery;
			PreparedStatement preparedSQLStmt = dbConnection.prepareStatement(sqlStmt);
			preparedSQLStmt.setLong(1, getLastRecordTime(dbFile) );
			return preparedSQLStmt.executeQuery();
		}finally{
			statement.close();
			//dbConnection.close();
		} 

	}

	private ResultSet getAllNewRecords(final String dbFile) throws SQLException{
		return getNewRecords(dbFile, "select * from records where recordTime > ? order by recordTime desc;");
	}

	private ResultSet getLatestNewRecords(final String dbFile) throws SQLException{
		return getNewRecords(dbFile, "select * from records where recordTime > ? order by recordTime desc limit 1;");
	}

	private ResultSet getAllNewRecordsCount(final String dbFile) throws SQLException{
		return getNewRecords(dbFile, "select count(*) from records where recordTime > ?");
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


	private int getTimeoutNewRecordsCount(final String dbFile) throws SQLException{
		return exatractCount(getNewRecords(dbFile, "select count(*) from records where recordTime > ? and sat = 'T'"));
	}

	private int getFailedNewRecordsCount(final String dbFile) throws SQLException{
		return exatractCount(getNewRecords(dbFile, "select count(*) from records where recordTime > ? and sat = 'F'"));
	}

	private int getSATNewRecordsCount(final String dbFile) throws SQLException{
		return exatractCount(getNewRecords(dbFile, "select count(*) from records where recordTime > ? and sat = 'S'"));
	}

	private int getUnSATNewRecordsCount(final String dbFile) throws SQLException{
		return exatractCount(getNewRecords(dbFile, "select count(*) from records where recordTime > ? and sat = 'U'"));
	}

	private int lazyReport(final String dbFile) throws SQLException{

		ResultSet rs = getAllNewRecordsCount(dbFile);
		int updatedRows = 0;
		if(rs.next()){

			updatedRows = rs.getInt(1);  

			final ResultSet latestRow = getLatestNewRecords(dbFile);
			if(latestRow.next()){
				DBNameToLastAddedMap.put(dbFile, latestRow.getLong("recordTime"));
			}
			latestRow.close();

		}
		return updatedRows;
	}


	private int mergeDBUpdated(final String dbFile, final ResultSet records) throws SQLException{

		//PreparedStatement preparedSQLStmt = mergedDB.prepareStatement(PostProcess.DBWriter.SQL_INSERT_STMT);

		int updatedRows= 0;

		boolean isFirst = true;

		while(records.next()){

			if(isFirst){

				DBNameToLastAddedMap.put(dbFile, records.getLong("recordTime"));

				isFirst = false;
			}

			PreparedStatement preparedSQLStmt = mergedDB.prepareStatement(PostProcess.DBWriter.SQL_INSERT_STMT);

			preparedSQLStmt.setString(1, records.getString(2));
			preparedSQLStmt.setString(2, records.getString(3));
			preparedSQLStmt.setInt(3, 	 records.getInt(4));
			preparedSQLStmt.setString(4, records.getString(5));
			preparedSQLStmt.setString(5, records.getString(6));
			preparedSQLStmt.setString(6, records.getString(7));
			preparedSQLStmt.setString(7, 	 records.getString(8));
			preparedSQLStmt.setString(8, records.getString(9));
			preparedSQLStmt.setInt(9,    records.getInt(10));
			preparedSQLStmt.setString(10, records.getString(11));
			preparedSQLStmt.setString(11, records.getString(12));
			preparedSQLStmt.executeUpdate();
			preparedSQLStmt.close();
			++updatedRows;
		}

		//System.out.println(updatedRows);


		//mergedDB.commit();
		//mergedDB.setAutoCommit(true);
		records.close();

		return updatedRows;
	}


	public int dBLazyReporter(String dbFile) throws SQLException{
		return lazyReport(dbFile);
	}

	private int dBEagerReporter(String dbFile) throws SQLException{
		return mergeDBUpdated(dbFile,
				getAllNewRecords(dbFile));
	}

	private int mergeGradually(final String dbFile, final int limit){
		return -1;
	}

	public String dBReporter(){

		String result = "";
		final File[] dbFiles = Utils.filesR(pathDBFiles.getAbsolutePath(), "^alloy((?!trace|lock).)*"); 
				//findAllFilesByName("alloy", pathDBFiles);

		int updatedCount = 0;
		int sat = 0, unsat = 0, failed = 0, timeout = 0;
		for(final File dbFile: dbFiles){
			try {
				
				String filename = dbFile.getAbsolutePath().replace(".h2.db", "").replace(".mv.db", "");
				logger.info("The DB file is: "+ filename);
				
				
				sat += getSATNewRecordsCount(filename);
				unsat += getUnSATNewRecordsCount(filename);
				timeout += getTimeoutNewRecordsCount(filename);
				failed += getFailedNewRecordsCount(filename);

				updatedCount += DO_LAZY ? dBLazyReporter(filename) : dBEagerReporter(filename);



				closeConnection(filename);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				result = result + '\n' + e.getMessage();
				e.printStackTrace();
			}

		}

		result  = "DB-SAT:          "+dbSATProgressTracker.progressDeltaReport(sat);
		result += "\nDB-UnSAT:        "+dbUnSATProgressTracker.progressDeltaReport(unsat);
		result += "\nDB-Timeout:      "+dbTimeoutProgressTracker.progressDeltaReport(timeout);
		result += "\nDB-Failed:       "+dbFailedProgressTracker.progressDeltaReport(failed);
		result += "\nDB-total:        "+dbTotalProgressTracker.progressDeltaReport(updatedCount);

		return result;

	}

	public static void main(String[] args) throws InterruptedException, SQLException {

		boolean doLazy = true;

		try{
			doLazy = Boolean.parseBoolean(args[0]);
		}catch(Exception e){}

		ProgressReporter pr = new ProgressReporter(doLazy);

		pr.startMergingThread();

		while(true){

			System.out.println(pr.fileReporter());

			System.out.println(pr.dBReporter());
			System.out.println("===========================================================================");
			Thread.sleep(refreshRate);


		}

	}
	
	
	

}
