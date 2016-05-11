/**
 * 
 */
package edu.uw.ece.alloy.debugger.propgen.benchmarker.center;

import java.util.List;
import java.util.Set;

/**
 * APIs for logging the messages that are sent or received
 * 
 * @author vajih
 *
 */
public interface RemoteProcessLogger {

	RemoteProcessRecord getRemoteProcessRecord(final RemoteProcess process);

	void changeLastLiveTimeRecieved(final RemoteProcess process);

	void changeLastLiveTimeRecieved(final RemoteProcess process,
			long lastLiveTimeRecieved);

	void changeLastLiveTimeReported(final RemoteProcess process);

	void changeLastLiveTimeReported(final RemoteProcess process,
			long lastLiveTimeReported);

	void IncreaseDoneTasks(final RemoteProcess process);

	void IncreaseDoneTasks(final RemoteProcess process, int doneTasks);

	void changeDoneTasks(final RemoteProcess process, int doneTasks);

	void DecreaseDoingTasks(final RemoteProcess process);

	void DecreaseDoingTasks(final RemoteProcess process, int doingTasks);

	void IncreaseDoingTasks(final RemoteProcess process);

	void IncreaseDoingTasks(final RemoteProcess process, int doingTasks);

	void changeDoingTasks(final RemoteProcess process, int doingTasks);

	void IncreaseSentTasks(final RemoteProcess process);

	void IncreaseSentTasks(final RemoteProcess process, int sentTasks);

	void changeSentTasks(final RemoteProcess process, int sentTasks);

	void changeStatusToNOANSWER(final RemoteProcess process);

	void changeStatusToKILLING(final RemoteProcess process);

	void changeStatusToWORKING(final RemoteProcess process);

	void changeStatusToIDLE(final RemoteProcess process);

	String getStatus();

	boolean allProcessesNotWorking();

	Set<RemoteProcess> getLiveProcessIDs();

	Set<RemoteProcess> getAllRegisteredProcesses();

	List<RemoteProcess> getTimedoutProcess(int threshold);

	void killAndReplaceProcess(final RemoteProcess process);

}
