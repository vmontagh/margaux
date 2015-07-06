package edu.uw.ece.alloy.debugger.propgen.benchmarker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.agent.AlloyProcessRunner;

public abstract class DBConnectionPool {
	
	protected final static Logger logger = Logger.getLogger(SQLiteDBConnectionPool.class.getName()+"--"+Thread.currentThread().getName());;
	
	final protected  ComboPooledDataSource cpds = new ComboPooledDataSource();
	final protected String url;
	
	public Connection getConnection() throws SQLException{
		//return cpds.getConnection();
		return DriverManager.getConnection("jdbc:"+getJDBCName()+":"+url);
	}
	
	protected abstract String getDriverName();
	
	protected abstract String getJDBCName();
	
	protected void prepareConnection(){
		try {
			Class.forName(getDriverName());
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE,"["+Thread.currentThread().getName()+"]"+ "Cannot load the Database driver!");
			e.printStackTrace();
		}
		cpds.setJdbcUrl("jdbc:"+getJDBCName()+":"+url );
		cpds.setAcquireIncrement(Integer.valueOf(Configuration.getProp("jdbc_max_increament_size")));
		cpds.setMaxPoolSize(Integer.valueOf(Configuration.getProp("jdbc_max_pool_size")));
		
	}
	
	protected DBConnectionPool( String url){
		this.url = url;
		prepareConnection();
	}

}
