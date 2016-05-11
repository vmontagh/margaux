package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.mit.csail.sdg.gen.alloy.Configuration;

public abstract class DBConnectionPool {

	protected final static Logger logger = Logger
			.getLogger(SQLiteDBConnectionPool.class.getName() + "--"
					+ Thread.currentThread().getName());;

	final protected ComboPooledDataSource cpds = new ComboPooledDataSource();
	final protected DBConnectionInfo connectionInfo;
	final protected Integer poolSizeIncrement;
	final protected Integer maxPoolSize;

	boolean isprepared;

	public Connection getPooledConnection() throws SQLException {
		if (!isprepared) {
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "Preparing the connection pool to " + getConnectionURL());
			prepareConnection();
			isprepared = true;
		}
		return cpds.getConnection();
	}

	public Connection getConnection() throws SQLException {

		return DriverManager.getConnection(getConnectionURL(),
				connectionInfo.username, connectionInfo.password);
	}

	protected abstract String getDriverName();

	protected abstract String getJDBCName();

	protected void prepareConnection() {

		cpds.setJdbcUrl(getConnectionURL());
		cpds.setUser(connectionInfo.username);
		cpds.setPassword(connectionInfo.password);
		cpds.setAcquireIncrement(poolSizeIncrement);
		cpds.setMaxPoolSize(maxPoolSize);

	}

	protected DBConnectionPool(final DBConnectionInfo dBConnectionInfo) {
		this(dBConnectionInfo,
				Integer.parseInt(Configuration.getProp("jdbc_max_increament_size")),
				Integer.parseInt(Configuration.getProp("jdbc_max_pool_size")));
	}

	protected DBConnectionPool(final DBConnectionInfo dBConnectionInfo,
			final int poolSizeIncrement, final int maxPoolSize) {
		this.connectionInfo = new DBConnectionInfo(dBConnectionInfo);
		this.maxPoolSize = maxPoolSize;
		this.poolSizeIncrement = poolSizeIncrement;

		try {
			Class.forName(getDriverName());
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "]"
					+ "Cannot load the Database driver!");
			e.printStackTrace();
		}

		isprepared = false;
	}

	public abstract DBConnectionPool createIt(
			final DBConnectionInfo dBConnectionInfo, final int poolSizeIncrement,
			final int maxPoolSize);

	public abstract DBConnectionPool createIt(
			final DBConnectionInfo dBConnectionInfo);

	public String getConnectionURL() {
		return "jdbc:" + getJDBCName() + ":" + connectionInfo.address;
	}

}
