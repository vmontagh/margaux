/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

/**
 * RemoteProcess is an abstract class containing information for the a process
 * made on another JVM
 * 
 * @author vajih
 *
 */
public class RemoteProcessRecord {

	public static enum Status {
		INITIATED, // Initial state is when the process is created but not get an
							 // ack from the actual process.
		IDLE, // Once the process gets the ack or no more process are ready to be
					// processed.
		WORKING, // Working
		KILLING, // Suicided to being killed.
		NOANSWER
	}

	public final RemoteProcess id;
	/* How many messages are DONE. The task is sent and a response is received. */
	public final int doneTasks;
	/* How many tasks a processing now. It is increased after each */
	public final int doingTasks;
	/* How many tasks are sent. No matter that are received back. */
	public final int sentTasks;
	public final Status status;
	public final long lastLiveTimeRecieved;
	public final long lastLiveTimeReported;
	public final long creationTime;
	public final Process process;

	protected RemoteProcessRecord(RemoteProcess id, int doneTasks, int doingTasks,
			int sentTasks, Status status, Process process,
			final long lastLiveTimeReported, final long lastLiveTimeRecieved,
			final long creationTime) {
		super();
		this.id = id;
		this.doneTasks = doneTasks;
		this.doingTasks = doingTasks;
		this.sentTasks = sentTasks;
		this.status = status;
		this.process = process;
		this.lastLiveTimeReported = lastLiveTimeReported;
		this.lastLiveTimeRecieved = lastLiveTimeRecieved;
		this.creationTime = creationTime;
	}

	public RemoteProcessRecord(RemoteProcess id, int doneTasks, int doingTasks,
			int sentTasks, Status status, Process process,
			final long lastLiveTimeReported, final long lastLiveTimeRecieved) {
		this(id, doneTasks, doingTasks, sentTasks, status, process,
				lastLiveTimeReported, lastLiveTimeRecieved, System.currentTimeMillis());
	}

	public RemoteProcessRecord(RemoteProcess id, Process process) {

		this(id, 0, 0, 0, Status.INITIATED, process, 0, 0);
	}

	public RemoteProcessRecord changeDoneTasks(int i) {
		return new RemoteProcessRecord(id, i, doingTasks, sentTasks, status,
				process, lastLiveTimeReported, lastLiveTimeRecieved, creationTime);
	}

	public RemoteProcessRecord changeStatus(Status s) {
		return new RemoteProcessRecord(id, doneTasks, doingTasks, sentTasks, s,
				process, lastLiveTimeReported, lastLiveTimeRecieved, creationTime);
	}

	public RemoteProcessRecord changeDoingTasks(int i) {
		return new RemoteProcessRecord(id, doneTasks, i, sentTasks, status, process,
				lastLiveTimeReported, lastLiveTimeRecieved, creationTime);
	}

	public RemoteProcessRecord changeSentTasks(int i) {
		return new RemoteProcessRecord(id, doneTasks, doingTasks, i, status,
				process, lastLiveTimeReported, lastLiveTimeRecieved, creationTime);
	}

	public RemoteProcessRecord changeLastLiveTimeReported(long i) {
		return new RemoteProcessRecord(id, doneTasks, doingTasks, sentTasks, status,
				process, i, lastLiveTimeRecieved, creationTime);
	}

	public RemoteProcessRecord changeLastLiveTimeRecieved(long i) {
		return new RemoteProcessRecord(id, doneTasks, doingTasks, sentTasks, status,
				process, lastLiveTimeReported, i, creationTime);
	}

	public RemoteProcess getPId() {
		return id;
	}

	public boolean isActive() {
		return status.equals(Status.IDLE) || status.equals(Status.WORKING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
		result = prime * result + doingTasks;
		result = prime * result + doneTasks;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ (int) (lastLiveTimeRecieved ^ (lastLiveTimeRecieved >>> 32));
		result = prime * result
				+ (int) (lastLiveTimeReported ^ (lastLiveTimeReported >>> 32));
		result = prime * result + sentTasks;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RemoteProcessRecord other = (RemoteProcessRecord) obj;
		if (creationTime != other.creationTime) {
			return false;
		}
		if (doingTasks != other.doingTasks) {
			return false;
		}
		if (doneTasks != other.doneTasks) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (lastLiveTimeRecieved != other.lastLiveTimeRecieved) {
			return false;
		}
		if (lastLiveTimeReported != other.lastLiveTimeReported) {
			return false;
		}
		if (sentTasks != other.sentTasks) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemoteProcessRecord [id=" + id + ", doneTasks=" + doneTasks
				+ ", doingTasks=" + doingTasks + ", sentTasks=" + sentTasks
				+ ", status=" + status + ", lastLiveTimeRecieved="
				+ lastLiveTimeRecieved + ", lastLiveTimeReported="
				+ lastLiveTimeReported + ", creationTime=" + creationTime + ", process="
				+ process + "]";
	}

}
