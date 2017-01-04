package epmc.webserver.backend.worker.task.worked.completed;

import epmc.webserver.backend.worker.task.worked.WorkedTask;
import epmc.webserver.common.TaskOperation;

/**
 * A task representing a completed task, i.e., a task that has been successfully finished
 * @author ori
 */
public abstract class CompletedTask extends WorkedTask {
	
	/**
	 * Create a new completed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 */
	protected CompletedTask(int userId, int taskId, TaskOperation operation) {
		super(userId, taskId, operation);
	}
}
