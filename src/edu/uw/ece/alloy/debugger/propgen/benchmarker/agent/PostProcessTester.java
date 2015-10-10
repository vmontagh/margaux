package edu.uw.ece.alloy.debugger.propgen.benchmarker.agent;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Collections;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.mit.csail.sdg.gen.alloy.Configuration;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.AlloyProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionInfo;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.DBLogger;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.MySQLDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.SQLiteDBConnectionPool;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.PropertyToAlloyCode;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.VacPropertyToAlloyCode;

public class PostProcessTester {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDBWriter() {
		
		//Database setup
		DBConnectionInfo connectionInfo = new DBConnectionInfo(Configuration.getProp("db_address"), 
																Configuration.getProp("db_user"), 
																	Configuration.getProp("db_password"), "");
		DBLogger db = DBLogger.createConfiguredSetuperObject(new MySQLDBConnectionPool(connectionInfo));
		
		try {
			connectionInfo = db.makeNewConfiguredDatabaseLog();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		DBConnectionPool dbConnection = new MySQLDBConnectionPool( connectionInfo);
		
		db = DBLogger.createDatabaseOperationsObject( dbConnection );
		
		PostProcess.DBWriter dbWriter = new PostProcess.DBWriter();
		
		PropertyToAlloyCode vacPropertyToAlloyCode = VacPropertyToAlloyCode.EMPTY_CONVERTOR;
		
		AlloyProcessingParam params = vacPropertyToAlloyCode.generate(); 

		params = params.changeDBConnectionInfo(connectionInfo);
		
		//make a sample result
		//AlloyProcessingParam params = AlloyProcessingParam.EMPTY_PARAM.createItself(new File("tmp/testing/src.txt"), new File("tmp/testing/dest.txt"), 0, "I am a content");
		AlloyProcessedResult result = new AlloyProcessedResult(params);
		result.sat = 1;
		result.solveTime = 10;
		result.clauses = 100;
		result.evalInsts = 0;
		result.evalTime = 300;
		result.totalVaraibles = 200;
		result.trasnalationTime = 50;
		
		dbWriter.action(result);
		
		//Check whether the result is stored properly.
		try(Connection connection  = dbConnection.getPooledConnection()){
			System.out.println(connection);
			try(Statement  statement = connection.createStatement()){
				try(ResultSet rs = statement.executeQuery("select * from "+connectionInfo.tablename )){
					
					if(rs.next())
					{
						assertEquals(rs.getString("result"), result.asRecord());
						
						System.out.println(rs.getString("params"));
						System.out.println(result.params.toString());
						
						assertEquals(rs.getString("params"), result.params.content());
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("An exception happened while storing the result in the database: "+e.toString());
		}
		
	}

}
