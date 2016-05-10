package edu.uw.ece.alloy.debugger.pattern;

import java.io.File;
import java.util.UUID;

import edu.uw.ece.alloy.debugger.AnalyzingSession;

/**
 * The class is responsible for analyzing the patterns
 * of a given Expr in a source path.
 * @author vajih
 *
 */
public class ExpressionAnalyzingSession implements AnalyzingSession {
	
	final UUID id;
	final File sourcePath;
	final String predTobeAnalyzed;
	final String fieldName;
	
	
	
	public ExpressionAnalyzingSession() {
		 id = UUID.randomUUID();
		 
	}
	
	/**
	 * Start the generator to put appropriate tasks in the feeder
	 */
	public void start(){
		
	}
	
	/**
	 * Once a part of 
	 */
	public void followUp(){
		
	}
	
	public void doneOnWait(){
		
	}

	@Override
	public long getSessionCreationTime() {
		// TODO Auto-generated method stub
		return id.timestamp();
	}

	@Override
	public UUID getSessionID() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}

}
