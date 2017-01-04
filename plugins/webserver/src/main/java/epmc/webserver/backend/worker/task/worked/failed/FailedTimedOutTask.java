package epmc.webserver.backend.worker.task.worked.failed;

import epmc.webserver.common.TaskOperation;

/**
 * A task representing a failed task, i.e., a task that has been finished due to a timeout
 * @author ori
 */
public class FailedTimedOutTask extends FailedTask {
	private final int timeOut;
	
	/**
	 * Create a new failed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param error the error describing the failure
	 * @param timeOut the used timeout
	 */
	public FailedTimedOutTask(int userId, int taskId, TaskOperation operation, String error, int timeOut) {
		super(userId, taskId, operation, error);
		this.timeOut = timeOut;
	}
	
	/**
	 * Return the timeout used as limit for the computation
	 * @return the timeout
	 */
	public int getTimeOut() {
		return timeOut;
	}
	
}
