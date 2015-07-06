package edu.uw.ece.alloy.debugger.propgen.benchmarker;


public class H2DBConnectionPool extends DBConnectionPool {

	public H2DBConnectionPool(String url) {
		super("tcp://localhost/"+url/*+";MV_STORE=TRUE;"/*+";MVCC=FALSE;"*/);
	}


	@Override
	protected String getDriverName() {
		return "org.h2.Driver";
	}



	@Override
	protected String getJDBCName() {
		return "h2";
	}

}
