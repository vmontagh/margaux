/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.alloy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.SetupMessage;
import edu.uw.ece.alloy.util.LazyFile;

/**
 * @author vajih
 *
 */
public class AlloySetupMessage extends SetupMessage {

	final static Logger logger = Logger
			.getLogger(AlloySetupMessage.class.getName() + "--" + Thread.currentThread().getName());
	private static final long serialVersionUID = -7241044167818193880L;
	private List<LazyFile> files;

	public AlloySetupMessage(RemoteProcess process, List<LazyFile> files) {
		super(process);
		this.files = new ArrayList<>(files);
	}

	@Override
	public void onAction(Map<String, Object> context) throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<List<LazyFile>> addNewParamInQueue = (Consumer<List<LazyFile>>) context.get("copyDependecyFiles");
		try {
			addNewParamInQueue.accept(files);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
