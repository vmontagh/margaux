/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.DoneMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;

/**
 * The setup Action is Done
 * @author vajih
 *
 */
public class AlloySetupDoneMessage extends DoneMessage {

	private static final long serialVersionUID = -6663189080545286387L;

	public AlloySetupDoneMessage(RemoteProcess process) {
		super(process);
	}

	@Override
	public void onAction(Map<String, Object> context) throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<RemoteProcess> processIsSetup = (Consumer<RemoteProcess>) context.get("processIsSetup");
		try {
			processIsSetup.accept(process);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
