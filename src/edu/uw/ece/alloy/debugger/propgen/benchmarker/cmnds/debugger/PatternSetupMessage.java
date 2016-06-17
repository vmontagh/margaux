package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.debugger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.InvalidParameterException;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.SetupMessage;
import edu.uw.ece.alloy.util.LazyFile;

public class PatternSetupMessage extends SetupMessage {

	private static final long serialVersionUID = 6294864070004084966L;
	private List<LazyFile> files;

	public PatternSetupMessage(RemoteProcess process, List<LazyFile> files) {
		super(process);
		this.files = new ArrayList<>(files);
	}

	@Override
	public void onAction(Map<String, Object> context) throws InvalidParameterException {
		@SuppressWarnings("unchecked")
		Consumer<List<LazyFile>> copyDependecyFiles = (Consumer<List<LazyFile>>) context.get("copyDependecyFiles");
		try {
			copyDependecyFiles.accept(files);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
