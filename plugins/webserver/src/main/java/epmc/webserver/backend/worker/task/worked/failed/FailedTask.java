package epmc.webserver.backend.worker.task.worked.failed;

import epmc.webserver.backend.worker.task.worked.WorkedTask;
import epmc.webserver.common.TaskOperation;

/**
 * A task representing a failed task, i.e., a task that has been finished but with errors
 * @author ori
 */
public abstract class FailedTask extends WorkedTask {
	private final String error;
	
	/**
	 * Create a new failed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param error the error describing the failure
	 */
	public FailedTask(int userId, int taskId, TaskOperation operation, String error) {
		super(userId, taskId, operation);
		this.error = error;
	}
	
	/**
	 * Return the error describing the motivation of the failure
	 * @return the error
	 */
	public String getError() {
		return error;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\nError message: " + error;
	}
}
