package edu.uw.ece.alloy.debugger.propgen.benchmarker;

public class H2DBConnectionPool extends DBConnectionPool {

	public H2DBConnectionPool(String url) {
		super(new DBConnectionInfo(
				"tcp://localhost/" + url/* +";MV_STORE=TRUE;" */ + ";MVCC=TRUE;", "",
				""));
	}

	@Override
	protected String getDriverName() {
		return "org.h2.Driver";
	}

	@Override
	protected String getJDBCName() {
		return "h2";
	}

	@Override
	public DBConnectionPool createIt(DBConnectionInfo dBConnectionInfo,
			int poolSizeIncrement, int maxPoolSize) {
		return null;
	}

	@Override
	public DBConnectionPool createIt(DBConnectionInfo dBConnectionInfo) {
		return null;
	}

}
