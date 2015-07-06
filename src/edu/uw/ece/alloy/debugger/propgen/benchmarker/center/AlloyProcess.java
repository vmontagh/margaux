package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.gen.alloy.Configuration;

public class AlloyProcess {
	
	public enum Status{
		INITIATED, //Initial state is when the process is created but not get an ack from the actual process. 
		IDLE, //Once the process gets the ack or no more process are ready to be processed. 
		WORKING, //Working 
		KILLING, //Suicided to being killed. 
		NOANSWER
	}
	public final InetSocketAddress address;
	public final int doneTasks;
	public final int doingTasks;
	public final int sentTasks;
	public final Status status;
	public final long lastLiveTimeRecieved;
	public final long lastLiveTimeReported;

	public final Process process;

	public AlloyProcess(InetSocketAddress address, Process process) {

		this( new InetSocketAddress( address.getAddress(), address.getPort()),
				0,
				0,
				0,
				Status.INITIATED,
				process, 0, 0);
	}


	public AlloyProcess(InetSocketAddress address, int doneTasks,
			int doingTasks, int sentTasks,
			Status status, Process process, final long lastLiveTimeReported, final long lastLiveTimeRecieved) {
		super();
		this.address = address;
		this.doneTasks = doneTasks;
		this.doingTasks = doingTasks;
		this.sentTasks = sentTasks;
		this.status = status;
		this.process = process;
		this.lastLiveTimeReported = lastLiveTimeReported;
		this.lastLiveTimeRecieved = lastLiveTimeRecieved;
	}


	public AlloyProcess changeDoneTasks(int i){
		return new AlloyProcess(address, i, doingTasks, sentTasks, status, process, lastLiveTimeReported,lastLiveTimeRecieved);
	}

	public AlloyProcess changeStatus(Status s){
		return new AlloyProcess(address, doneTasks, doingTasks, sentTasks, s, process, lastLiveTimeReported,lastLiveTimeRecieved);
	}

	public AlloyProcess changeDoingTasks(int i){
		return new AlloyProcess(address, doneTasks, i, sentTasks, status, process, lastLiveTimeReported,lastLiveTimeRecieved);
	}

	public AlloyProcess changeSentTasks(int i){
		return new AlloyProcess(address, doneTasks, doingTasks, i, status, process, lastLiveTimeReported,lastLiveTimeRecieved);
	}

	public AlloyProcess changeLastLiveTimeReported(long i){
		return new AlloyProcess(address, doneTasks, doingTasks, sentTasks, status, process, i, lastLiveTimeRecieved);
	}

	public AlloyProcess changeLastLiveTimeRecieved(long i){
		return new AlloyProcess(address, doneTasks, doingTasks, sentTasks, status, process, lastLiveTimeReported, i);
	}

	public InetSocketAddress getPId(){return address;}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((address == null) ? 0 : address.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlloyProcess other = (AlloyProcess) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AlloyProcess [address=" + address + ", doneTasks="
				+ doneTasks + ", doingTasks=" + doingTasks + ", sentTasks="
				+ sentTasks + ", status=" + status
				+ ", lastLiveTimeRecieved=" + lastLiveTimeRecieved
				+ ", lastLiveTimeReported=" + lastLiveTimeReported
				+ ", process=" + process + "]";
	}


	public boolean isActive(){
		return status.equals(Status.IDLE) || status.equals(Status.WORKING); 
	}


}
