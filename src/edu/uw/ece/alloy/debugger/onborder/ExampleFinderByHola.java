package edu.uw.ece.alloy.debugger.onborder;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.uw.ece.alloy.debugger.mutate.ExampleFinder;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.ProcessDistributer;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.ResponseMessage;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder.OnBorderProcessedResult;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder.OnBorderProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.onborder.OnBorderRequestMessage;
import edu.uw.ece.alloy.util.ServerSocketInterface;
import edu.uw.ece.alloy.util.events.MessageEventListener;
import edu.uw.ece.alloy.util.events.MessageReceivedEventArgs;

public class ExampleFinderByHola implements ExampleFinder {

	private final ServerSocketInterface interfacE;
	private final ProcessDistributer processManager;
	private final File tmpLocalDirectory;
	
	/**
	 * Creates a new instance of {@link ExampleFinderByHola}.
	 * 
	 * @param interfacE - The socket interface to be used by the example finder.
	 * @param processManager - The remote process manager used to distribute and manage each hola agent
	 * @param tmpLocalDirectory - The specified local directory to be used as a temp directory by the hola agent.
	 */
	public ExampleFinderByHola(ServerSocketInterface interfacE, ProcessDistributer processManager,
			File tmpLocalDirectory) {
		this.interfacE = interfacE;
		this.processManager = processManager;
		this.tmpLocalDirectory = tmpLocalDirectory;
	}

	@Override
	public Pair<Optional<String>, Optional<String>> findOnBorderExamples(File path, String predNameA,
			String predNameB) {

		OnBorderProcessingParam param = new OnBorderProcessingParam(0, this.tmpLocalDirectory, UUID.randomUUID(),
				Long.MAX_VALUE, path.getAbsolutePath(), predNameA, predNameB);

		OnBorderRequestMessage message = new OnBorderRequestMessage(this.interfacE.getHostProcess(), param);

		final SynchronizedResult<OnBorderProcessedResult> result = new SynchronizedResult<>();
		MessageEventListener<MessageReceivedEventArgs> receiveListener = new MessageEventListener<MessageReceivedEventArgs>() {
			@Override
			public void actionOn(ResponseMessage responseMessage, MessageReceivedEventArgs messageArgs) {

				result.result = (OnBorderProcessedResult) responseMessage.getResult();
				synchronized (result) {
					System.out.println("I have the result at last");
					result.notify();
				}

			}
		};

		this.interfacE.MessageReceived.addListener(receiveListener);
		this.interfacE.sendMessage(message, processManager.getActiveRandomeProcess());

		// Wait for result;
		synchronized (result) {
			try {
				do {
					result.wait();
					// Wait until the response for the same session is arrived.
				} while (!result.getResult().get().getParam().getAnalyzingSessionID()
						.equals(param.getAnalyzingSessionID()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		interfacE.MessageReceived.removeListener(receiveListener);
		System.out.println("result: " + result.getResult().get().getResult());

		// Right now, the result is the same for both
		if (result.getResult().get().getResult().isPresent()) {
		    
		    String res = result.getResult().get().getResult().get();
//			return new Pair<Optional<String>, Optional<String>>(Optional.of(res.get(predNameA)),
//					Optional.of(res.get(predNameB)));
		    
		    // TODO (Vajih): Why is the result a pair? Which result is for predA and for predB?
		    return new Pair<Optional<String>, Optional<String>>(Optional.of(res), Optional.of(res)); 
		}

		return new Pair<Optional<String>, Optional<String>>(Optional.empty(), Optional.empty());
	}

	public class SynchronizedResult<T> {
		T result = null;

		public Optional<T> getResult() {
			return Optional.ofNullable(result);
		}
	}

}
