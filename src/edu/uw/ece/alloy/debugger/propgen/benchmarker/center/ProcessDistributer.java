package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

public interface ProcessDistributer extends RemoteProcessLogger {

	public RemoteProcess getRandomProcess();

	public RemoteProcess getActiveRandomeProcess();
}