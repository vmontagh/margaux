package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDBConnectionPool extends DBConnectionPool {

	public SQLiteDBConnectionPool(String url) {
		super(url);
	}

	@Override
	protected String getDriverName() {
		return "org.sqlite.JDBC";
	}



	@Override
	protected String getJDBCName() {
		return "sqlite";
	}
	
}
