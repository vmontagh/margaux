package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy.AlloyProcessedResult;

public class DBLogger {

	// Database related configurations
	final DBConnectionInfo connectionInfo;
	final boolean tableRenew;

	final public static String SQL_TABLE_SCHEMA = "CREATE TABLE [[TABLE]](id INTEGER PRIMARY KEY AUTO_INCREMENT, result TEXT, params TEXT, date NUMERIC, prop1 TEXT, prop2 TEXT, op char(20), sat char(2), recordTime BIGINT, recordsTimeConfirm BIGINT, srcPath TEXT, destPath TEXT, pid char(200))";
	final public static String SQL_INDEX = "CREATE INDEX IDX_[[TABLE]]_SAT ON [[TABLE]](SAT)";
	final public static String SQL_DROP_TABLE = "DROP TABLE [[TABLE]]";
	final public static String SQL_DROP_DATABASE = "DROP DATABASE [[TABLE]]";

	final public static String SQL_INSERT_STMT = "INSERT INTO [[TABLE]](result, params, date, prop1, prop2, op, sat, recordTime, recordsTimeConfirm, srcPath, destPath, pid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
	final public static String SQL_DELETE_STMT = "DELETE FROM [[TABLE]] WHERE ID=?";

	protected final static Logger logger = Logger.getLogger(
			DBLogger.class.getName() + "--" + Thread.currentThread().getName());

	final DBConnectionPool dBConnection;

	public DBLogger(final String dbAddress, final String dbUsername,
			final String dbPassword, final String dbName, final String tableName,
			final boolean tableRenew, final DBConnectionPool dBConnection) {

		this.connectionInfo = new DBConnectionInfo(dbAddress, dbUsername,
				dbPassword, dbName, tableName);

		this.tableRenew = tableRenew;
		this.dBConnection = dBConnection.createIt(connectionInfo);
	}

	/**
	 * 
	 * @param tableName
	 * @param tableRenew
	 * @param dBConnection
	 *          THE CONNECTION IS SHARED BETWEEN THE NEW OBJECT AND THE OUTSIDER.
	 */
	public DBLogger(final boolean tableRenew,
			final DBConnectionPool dBConnection) {
		this.connectionInfo = new DBConnectionInfo(dBConnection.connectionInfo);
		this.tableRenew = tableRenew;
		this.dBConnection = dBConnection;
	}

	public DBLogger(final DBConnectionPool dBConnection) {
		this(Boolean.parseBoolean(Configuration.getProp("db_table_renew")),
				dBConnection);
	}

	public DBLogger(final String dbName, final String tableName,
			final DBConnectionPool dBConnection) {
		this(Configuration.getProp("db_address"), Configuration.getProp("db_user"),
				Configuration.getProp("db_password"), dbName, tableName,
				Boolean.parseBoolean(Configuration.getProp("db_table_renew")),
				dBConnection);
	}

	/**
	 * The function just takes a DBConnectionPool to recreate a pool using new
	 * stored information.
	 * 
	 * @param dBConnection
	 * @return
	 */
	public static DBLogger createConfiguredSetuperObject(
			final DBConnectionPool dBConnection) {
		return new DBLogger("", "", dBConnection);
	}

	public static DBLogger createDatabaseOperationsObject(
			final DBConnectionPool dBConnection) {
		// No need for 'tableRenew'in case of insert/deleting
		return new DBLogger(false, dBConnection);
	}

	protected Connection getConnectionFromPool(final String serverName,
			final String dBName) throws SQLException {
		return dBConnection.getPooledConnection();
	}

	/**
	 * This function creates a Database
	 * 
	 * @return
	 * @throws SQLException
	 */
	protected void createDatabase(final String newDBName) throws SQLException {

		try (final Connection connection = (dBConnection
				.createIt(new DBConnectionInfo(connectionInfo.address,
						connectionInfo.username, connectionInfo.password)))
								.getConnection()) {
			try (final Statement statement = connection.createStatement()) {
				final String sql = "CREATE DATABASE " + newDBName;
				statement.executeUpdate(sql);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == 1007) {
				logger.warning("[" + Thread.currentThread().getName() + "]" + "'"
						+ newDBName + "' is already exist.'");
			} else {
				logger.severe("[" + Thread.currentThread().getName() + "]"
						+ "Cannot create to database because of " + e.getStackTrace());
				throw e;
			}
		}
	}

	/**
	 * Create table
	 * 
	 * @throws SQLException
	 * @throws Exception
	 */
	protected void createTableLogMySQLDatabase(final String dBName,
			final String tableName) throws SQLException {
		try (final Connection connection = (dBConnection
				.createIt(new DBConnectionInfo(connectionInfo.address,
						connectionInfo.username, connectionInfo.password, dBName)))
								.getConnection()) {

			if (tableRenew) {
				try (final PreparedStatement statement = connection
						.prepareStatement(SQL_DROP_TABLE.replace("[[TABLE]]", tableName))) {
					// statement.setString(1, tableName);
					try {
						statement.executeUpdate();
					} catch (SQLException e) {
						if (e.getErrorCode() == 1051) {
							logger.warning("[" + Thread.currentThread().getName() + "]"
									+ "Table '" + tableName + "' does not exist to be dropped'");
						} else {
							logger.severe("[" + Thread.currentThread().getName() + "]"
									+ "Cannot drop the table in '" + dBName + "' because of "
									+ e.getStackTrace());
							throw e;
						}
					}
				}

			}
			try (final PreparedStatement statement = connection
					.prepareStatement(SQL_TABLE_SCHEMA.replace("[[TABLE]]", tableName))) {
				statement.executeUpdate();
			} catch (SQLException e) {
				if (e.getErrorCode() == 1050) {
					logger.warning("[" + Thread.currentThread().getName() + "]" + "'"
							+ tableName + "' dows already exist.'");
				} else {
					logger.severe("[" + Thread.currentThread().getName() + "]"
							+ "Cannot create to table in '" + dBName + "' because of "
							+ e.getStackTrace());
					throw e;
				}
			}
			try (final PreparedStatement statement = connection.prepareStatement(
					SQL_INDEX.replaceAll("\\[\\[TABLE\\]\\]", tableName))) {
				statement.executeUpdate();
			} catch (SQLException e) {
				if (e.getErrorCode() == 1061) {
					logger.warning("[" + Thread.currentThread().getName() + "]"
							+ " The index for'(" + tableName + ").sat' is already exist.'");
				} else {
					logger.severe("[" + Thread.currentThread().getName() + "]"
							+ "Cannot create to table in '" + dBName + "' because of "
							+ e.getStackTrace());
					throw e;
				}
			}

		} catch (SQLException e) {
			logger.severe("[" + Thread.currentThread().getName() + "]"
					+ "Cannot create to table in '" + dBName + "' because of "
					+ e.getStackTrace());
			throw e;
		}
	}

	public DBConnectionInfo makeNewConfiguredDatabaseLog() throws SQLException {
		return makeNewDatabaseLog(Configuration.getProp("db_file_name_candidate"),
				Configuration.getProp("db_table_name_candidate"));
	}

	/**
	 * 
	 * @param DataBaseName
	 * @param TableName
	 * @return A pair of new database name and new table name.
	 * @throws SQLException
	 */
	public DBConnectionInfo makeNewDatabaseLog(final String DataBaseName,
			final String TableName) throws SQLException {

		final String newDatabaseName = DataBaseName.replace("%t",
				LocalDateTime.now().format(
						DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_nnnnnnnnn")));
		// First try to create the database
		createDatabase(newDatabaseName);

		final String newTableName = TableName.replace("%t", LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_nnnnnnnnn")));

		createTableLogMySQLDatabase(newDatabaseName, newTableName);

		return new DBConnectionInfo(connectionInfo.address, connectionInfo.username,
				connectionInfo.password, newDatabaseName, newTableName);

	}

	public void insertResult(final AlloyProcessedResult result,
			final String pid) {
		this.insertResult(result, this.connectionInfo.tablename, pid);
	}

	protected void insertResult(final AlloyProcessedResult result,
			final String tableName, String pid) {
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ "Inserting a record in the database: " + result);
		try (Connection connection = dBConnection.getPooledConnection()) {
			connection.setAutoCommit(true);
			try (PreparedStatement preparedSQLStmt = connection
					.prepareStatement(SQL_INSERT_STMT.replace("[[TABLE]]", tableName))) {

				String SATResult = convertSATResult(result);

				// Extract properties and operator

				preparedSQLStmt.setString(1, result.asRecord());
				preparedSQLStmt.setString(2, result.getParam().content().get());
				preparedSQLStmt.setInt(3, -1);
				preparedSQLStmt.setString(4, result.getParam().alloyCoder.predNameA);
				preparedSQLStmt.setString(5, result.getParam().alloyCoder.predNameB);
				preparedSQLStmt.setString(6,
						result.getParam().alloyCoder.srcNameOperator());
				preparedSQLStmt.setString(7, SATResult);
				preparedSQLStmt.setLong(8, System
						.currentTimeMillis() /*
																  * LocalDateTime.now().format(DateTimeFormatter
																  * .ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
																  */);
				preparedSQLStmt.setInt(9, -1);
				preparedSQLStmt.setString(10,
						result.getParam().getSrcPath().get().getName());
				preparedSQLStmt.setString(11,
						result.getParam().getDestPath().get().getName());
				preparedSQLStmt.setString(12, pid);
				preparedSQLStmt.executeUpdate();
			}

			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The records is inserted in the DB: " + result);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Failed on storing the result in DB: " + result, e);
		}
	}

	public void dropDatabase(String databaseName) {
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ "Droping Database: " + databaseName);
		try (Connection connection = dBConnection.getConnection()) {
			try (PreparedStatement preparedSQLStmt = connection
					.prepareStatement(SQL_DROP_DATABASE)) {
				preparedSQLStmt.setString(1, databaseName);
				preparedSQLStmt.executeUpdate();
			}
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The Database has been successfully droppped: " + databaseName);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Failed on dropping : " + databaseName, e);
		}
	}

	public void dropTable(String tableName) {
		if (Configuration.IsInDeubbungMode)
			logger.info("[" + Thread.currentThread().getName() + "] "
					+ "Droping Database: " + tableName);
		try (Connection connection = dBConnection.getConnection()) {
			try (PreparedStatement preparedSQLStmt = connection
					.prepareStatement(SQL_DROP_TABLE)) {
				preparedSQLStmt.setString(1, tableName);
				preparedSQLStmt.executeUpdate();
			}
			if (Configuration.IsInDeubbungMode)
				logger.info("[" + Thread.currentThread().getName() + "] "
						+ "The Database has been successfully droppped: " + tableName);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "[" + Thread.currentThread().getName() + "] "
					+ "Failed on dropping : " + tableName, e);
		}
	}

	public static String convertSATResult(final AlloyProcessedResult result) {
		return result.isFailed() ? "F"
				: result.isTimedout() ? "T"
						: result.sat == 1 ? "S" : result.sat == -1 ? "U" : "N";
	}
}
