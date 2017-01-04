package epmc.webserver.backend.worker.task.worked.system;

import epmc.webserver.common.TaskOperation;

/**
 * A task representing a system shutdown task, i.e., a task that has been finished due to a system shutdown
 * @author ori
 */
public class SystemShutdownTask extends SystemTask {

	/**
	 * Create a new worked task that has suffered a system shutdown identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 */
	public SystemShutdownTask(int userId, int taskId, TaskOperation operation) {
		super(userId, taskId, operation);
	}
	
	
}
