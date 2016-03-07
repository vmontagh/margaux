package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.sql.SQLException;

import edu.uw.ece.alloy.Compressor;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;

public class AnalyzerRunner {
	protected static DBConnectionInfo dbConnectionInfo = null;
	public static DBConnectionInfo getDefaultConnectionInfo() throws SQLException{
		dbConnectionInfo = dbConnectionInfo != null ? dbConnectionInfo : DBLogger.createConfiguredSetuperObject(new MySQLDBConnectionPool(Compressor.EMPTY_DBCONNECTION)).makeNewConfiguredDatabaseLog();
		return dbConnectionInfo ;
	}

}
