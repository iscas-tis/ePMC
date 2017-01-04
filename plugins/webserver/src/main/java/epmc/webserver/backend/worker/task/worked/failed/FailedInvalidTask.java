package epmc.webserver.backend.worker.task.worked.failed;

import epmc.webserver.common.TaskOperation;
import epmc.webserver.error.ProblemsWebserver;

/**
 * A task representing a failed task, i.e., a task that has been finished but with errors due to an invalid operation
 * @author ori
 */
public class FailedInvalidTask extends FailedTask {

	private final String originalOperation;

	/**
	 * Create a new failed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param originalOperation the required operation from the database
	 */
	public FailedInvalidTask(int userId, int taskId, TaskOperation operation, String originalOperation) {
		super(userId, taskId, operation, ProblemsWebserver.WORKER_MODEL_CHECKER_INVALID_OPERATION.toString());
		this.originalOperation = originalOperation;
	}
	
	/**
	 * Return the invalid operation that was used to create the task
	 * @return the required operation
	 */
	public String getOriginalOperation() {
		return originalOperation;
	}
	
	@Override
	public String toString() {
		return super.toString() + "; actual operation found: " + originalOperation;	
	}
}
