package epmc.webserver.backend.worker.task.worked.intermediate;

import epmc.EPMCServer;
import epmc.webserver.common.TaskOperation;

/**
 * A task representing an intermediate task, i.e., a task that has not yet been finished but that has provided some partial information
 * @author ori
 */
public class PartialTask extends IntermediateTask {
	private final String partialStatus;

	/**
	 * Create a new partial task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param partialStatus the partial status as provided by the {@linkplain EPMCServer}
	 */
	public PartialTask(int userId, int taskId, TaskOperation operation, String partialStatus) {
		super(userId, taskId, operation);
		this.partialStatus = partialStatus;
	}
	
	/**
	 * Return the partial status represented by this task
	 * @return the partial status
	 */
	public String getPartialStatus() {
		return partialStatus;
	}
	
	@Override
	public String toString() {
		return super.toString() + " Status: " + partialStatus;
	}
}
