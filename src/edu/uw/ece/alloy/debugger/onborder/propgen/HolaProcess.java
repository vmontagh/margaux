package edu.uw.ece.alloy.debugger.onborder.propgen;

import java.net.InetSocketAddress;

import java.io.InputStream;
import java.io.OutputStream;

import edu.uw.ece.alloy.debugger.propgen.benchmarker.center.AlloyProcess;

public class HolaProcess extends AlloyProcess {
	
	public HolaProcess(InetSocketAddress address, Process process) {
		super(address, process);
	}
	
	public HolaProcess(InetSocketAddress address, int doneTasks, int doingTasks,
			int sentTasks, Status status, Process process, long lastLiveTimeReported,
			long lastLiveTimeRecieved) {
		
		super(address, doneTasks, doingTasks, sentTasks, status, process,
				lastLiveTimeReported, lastLiveTimeRecieved);
		
	}
	
	@Override
	protected String getProcessName() {
		return "Hola Process";
	}

	public boolean isAlive() {
		return this.process != null && this.process.isAlive();
	}
	
	public OutputStream getOutputStream() {
		return this.process.getOutputStream();
	}
	
	public InputStream getInputStream() {
		return this.process.getInputStream();
	}

	public void destroyProcess() {
		this.process.destroy();
	}
}
