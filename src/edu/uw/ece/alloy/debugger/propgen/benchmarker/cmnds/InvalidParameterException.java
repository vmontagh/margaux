/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

/**
 * The parameters passed into the messages on an action is 
 * invalid, i.e. missed or cannot be retrieved.
 * @author vajih
 *
 */
public class InvalidParameterException extends Exception {

	private static final long serialVersionUID = 2877132515508130736L;
	
	public InvalidParameterException() {
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}
	
	
}
