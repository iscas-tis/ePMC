package epmc.webserver.backend.worker.task;

import epmc.webserver.common.TaskOperation;

/**
 * An abstract class representing a task
 * @author ori
 */
public abstract class Task {

	private final int userId;

	private final int taskId;

	private final TaskOperation operation;
	
	/**
	 * Create a new task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 */
	protected Task(int userId, int taskId, TaskOperation operation) {
		this.taskId = taskId;
		this.userId = userId;
		this.operation = operation;
	}
	
	/**
	 * Return the user identifier for this task
	 * @return the identifier of the user this task belongs to
	 */
	public int getUserId() {
		return userId;
	}
	
	/**
	 * Return the task identifier for this task
	 * @return the identifier of the task corresponding to this task
	 */
	public int getTaskId() {
		return taskId;
	}
	
	/**
	 * Return the task operation
	 * @return the operation to perform on this task
	 */
	public TaskOperation getOperation() {
		return operation;
	}

	@Override
	public String toString() {
		return "Task " + taskId + " of user " + userId + ": operation " + operation.toString();
	}
}
