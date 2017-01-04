package epmc.webserver.backend.worker.task.worked.system;

import epmc.webserver.common.TaskOperation;
import epmc.webserver.common.TaskStatus;

/**
 * A task that has been completed as the effect of an irreversible system failure
 * @author ori
 */
public class SystemIrreversibleFailureTask extends SystemTask {
	private final String error;
	private final TaskStatus status;
	
	/**
	 * Create a new task that has suffered of a system failure identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param status the status of the task, containing the error
	 * @param error the error message representing the system failure
	 */
	public SystemIrreversibleFailureTask(int userId, int taskId, TaskOperation operation, TaskStatus status, String error) {
		super(userId, taskId, operation);
		this.error = error;
		this.status = status;
	}
	
	/**
	 * Return the error message representing the system failure stored in this task
	 * @return the error message
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * Return the status representing the system failure stored in this task
	 * @return the status
	 */
	public TaskStatus getStatus() {
		return status;
	}
	
	@Override
	public String toString() {
		return super.toString() + " Status: " + status + " Error: " + error;		
	}
}
