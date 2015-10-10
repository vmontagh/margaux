package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import edu.uw.ece.alloy.Compressor;

public class MySQLDBConnectionPool extends DBConnectionPool {


	public MySQLDBConnectionPool(DBConnectionInfo dBConnectionInfo) {
		super(dBConnectionInfo);
	}

	public MySQLDBConnectionPool(final DBConnectionInfo dBConnectionInfo, int poolSizeIncrement,
			int maxPoolSize) {
		super (dBConnectionInfo, poolSizeIncrement, maxPoolSize);
	}

	

	@Override
	protected String getDriverName() {
		return "com.mysql.jdbc.Driver";
	}

	@Override
	protected String getJDBCName() {
		return "mysql";
	}
	
	public String getConnectionURL(){
		return "jdbc:"+getJDBCName()+"://"+connectionInfo.address + (!(connectionInfo.database == null || connectionInfo.database.equals("")|| connectionInfo.database.equals(Compressor.EMPTY_STRING)) ?  "/"+connectionInfo.database : "");
	}

	@Override
	public DBConnectionPool createIt(final DBConnectionInfo dBConnectionInfo, int poolSizeIncrement,
			int maxPoolSize) {
		return new MySQLDBConnectionPool(dBConnectionInfo,  poolSizeIncrement,
				 maxPoolSize);
	}

	@Override
	public DBConnectionPool createIt(DBConnectionInfo dBConnectionInfo) {
		return new MySQLDBConnectionPool(dBConnectionInfo);
	}


}
