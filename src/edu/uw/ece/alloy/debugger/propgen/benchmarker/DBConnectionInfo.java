package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.io.Serializable;

import com.google.common.base.Optional;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.Compressor;

public class DBConnectionInfo implements Serializable {

	private static final long serialVersionUID = 2837758635787489270L;
	protected final String address;
	protected final String username;
	protected final String password;
	protected final String database;
	protected final String tablename;

	public final static DBConnectionInfo CONFIGURED_DBConnectionInfo = new DBConnectionInfo(
			Configuration.getProp("db_address"), Configuration.getProp("db_user"),
			Configuration.getProp("db_password"),
			Configuration.getProp("db_file_name_candidate"),
			Configuration.getProp("db_table_name_candidate"));

	final static public DBConnectionInfo EMPTY_DBCONNECTIONINFO = new DBConnectionInfo(
			Compressor.EMPTY_STRING, Compressor.EMPTY_STRING, Compressor.EMPTY_STRING,
			Compressor.EMPTY_STRING, Compressor.EMPTY_STRING);

	public DBConnectionInfo(String address, String username, String password,
			String database, String tablename) {
		super();
		this.address = address;
		this.username = username;
		this.password = password;
		this.database = database;
		this.tablename = tablename;
	}

	public DBConnectionInfo(String address, String username, String password,
			String database) {
		this(address, username, password, database, Compressor.EMPTY_STRING);
	}

	public DBConnectionInfo(String address, String username, String password) {
		this(address, username, password, Compressor.EMPTY_STRING,
				Compressor.EMPTY_STRING);
	}

	public DBConnectionInfo(DBConnectionInfo dBConnectionInfo) {
		this(dBConnectionInfo.address, dBConnectionInfo.username,
				dBConnectionInfo.password, dBConnectionInfo.database,
				dBConnectionInfo.tablename);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((database == null) ? 0 : database.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((tablename == null) ? 0 : tablename.hashCode());
		return result;
	}

	protected boolean isEqual(DBConnectionInfo other) {
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (database == null) {
			if (other.database != null)
				return false;
		} else if (!database.equals(other.database))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (tablename == null) {
			if (other.tablename != null)
				return false;
		} else if (!tablename.equals(other.tablename))
			return false;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBConnectionInfo other = (DBConnectionInfo) obj;
		return (isEqual(other));
	}

	@Override
	public String toString() {
		return "DBConnectionInfo [address=" + address + ", username=" + username
				+ ", password=" + password + ", database=" + database + ", tablename="
				+ tablename + "]";
	}

	public Optional<String> getAddress() {
		return Optional.fromNullable(address);
	}

	public Optional<String> getUsername() {
		return Optional.fromNullable(username);
	}

	public Optional<String> getPassword() {
		return Optional.fromNullable(password);
	}

	public Optional<String> getDatabase() {
		return Optional.fromNullable(database);
	}

	public Optional<String> getTablename() {
		return Optional.fromNullable(tablename);
	}

}
