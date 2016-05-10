package edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.ProcessingParam;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.util.events.MessageEventArgs;

public abstract class RequestMessage extends RemoteMessage {

	private static final long serialVersionUID = -9179129766458690826L;
	protected final ProcessingParam param;
	/* A session is a sequence of requests and response */

	public RequestMessage(RemoteProcess process, long creationTime,
			final ProcessingParam param) {
		super(process, creationTime);
		this.param = param;
	}

	public RequestMessage(RemoteProcess process, final ProcessingParam param) {
		super(process);
		this.param = param;
	}

	public void prepareBeforeUse() throws InterruptedException {
	}

	/**
	 * Given a param, a copy of the Request is returned. The subclass implements
	 * it, to return the actual type.
	 * 
	 * @param param
	 * @return
	 */
	protected abstract RequestMessage changeParam(final ProcessingParam param);

	public void prepareThenSend(final RemoteProcess remoteProcess)
			throws Exception {
		changeParam(param.prepareToSend()).sendMe(remoteProcess);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((param == null) ? 0 : param.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RequestMessage other = (RequestMessage) obj;
		if (param == null) {
			if (other.param != null) {
				return false;
			}
		} else if (!param.equals(other.param)) {
			return false;
		}
		return true;
	}

	public ProcessingParam getProcessingParam() {
		return this.param;
	}

	@Override
	public void onEvent(MessageListenerAction listener, MessageEventArgs args) {
		listener.actionOn(this, args);
	}

}
