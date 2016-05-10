package edu.uw.ece.alloy.util;

import java.util.Optional;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.RemoteProcess;
import edu.uw.ece.alloy.debugger.propgen.benchmarker.cmnds.RemoteMessage;

public interface SendOnServerSocketInterface {
	public void sendMessage(RemoteMessage message);

	public RemoteProcess getHostProcess();

	public Optional<RemoteProcess> getRemoteProcess();

	public void sendMessage(RemoteMessage message, RemoteProcess remoteProcess);
}
