package epmc.webserver.backend.worker.task.worked.system;

import epmc.webserver.common.TaskOperation;

/**
 * A task that has been completed as the effect of a system failure
 * @author ori
 */
public class SystemFailureTask extends SystemTask {
	private final String error;
	
	/**
	 * Create a new task that has suffered of a system failure identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param error the error message representing the system failure
	 */
	public SystemFailureTask(int userId, int taskId, TaskOperation operation, String error) {
		super(userId, taskId, operation);
		this.error = error;
	}
	
	/**
	 * Return the error message representing the system failure stored in this task
	 * @return the error message
	 */
	public String getError() {
		return error;
	}
	
	@Override
	public String toString() {
		return super.toString() + " Error: " + error;		
	}
}
