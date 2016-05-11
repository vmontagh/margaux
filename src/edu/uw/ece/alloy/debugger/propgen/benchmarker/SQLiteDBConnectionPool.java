package edu.uw.ece.alloy.debugger.propgen.benchmarker;

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
		return null;
	}

	@Override
	public DBConnectionPool createIt(DBConnectionInfo dBConnectionInfo,
			int poolSizeIncrement, int maxPoolSize) {
		return null;
	}

}
