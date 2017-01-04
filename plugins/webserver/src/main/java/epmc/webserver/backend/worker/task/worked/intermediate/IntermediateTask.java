package epmc.webserver.backend.worker.task.worked.intermediate;

import epmc.webserver.backend.worker.task.worked.WorkedTask;
import epmc.webserver.common.TaskOperation;

/**
 * A task representing an intermediate task, i.e., a task that has not yet been finished but that has provided some information
 * @author ori
 */
public class IntermediateTask extends WorkedTask {

	/**
	 * Create a new intermediate task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 */
	public IntermediateTask(int userId, int taskId, TaskOperation operation) {
		super(userId, taskId, operation);
	}
}
