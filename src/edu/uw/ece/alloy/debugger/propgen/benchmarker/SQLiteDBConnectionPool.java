package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDBConnectionPool extends DBConnectionPool {



	protected SQLiteDBConnectionPool(DBConnectionInfo dBConnectionInfo) {
		super(dBConnectionInfo);
	}



	@Override
	protected String getDriverName() {
		return "org.sqlite.JDBC";
	}



	@Override
	protected String getJDBCName() {
		return "sqlite";
	}



	@Override
	public DBConnectionPool createIt(DBConnectionInfo dBConnectionInfo) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public DBConnectionPool createIt(DBConnectionInfo dBConnectionInfo,
			int poolSizeIncrement, int maxPoolSize) {
		// TODO Auto-generated method stub
		return null;
	}


	
}
