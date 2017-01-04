package epmc.webserver.backend.worker.task.towork;

import epmc.webserver.common.TaskOperation;

/**
 * A model task representing an invalid model. 
 * Usually this model is constructed when a task contains an operation that is not recognized by {@linkplain TaskOperation}
 * @author ori
 */
public class InvalidModelTask extends ModelTask {
	private final String originalOperation;

	/**
	 * Create a new invalid task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param originalOperation the original operation
	 */
	public InvalidModelTask(int userId, int taskId, String originalOperation) {
		super(userId, taskId, TaskOperation.invalidOperation, null, null, null);
		this.originalOperation = originalOperation;
	}
	
	/**
	 * Return the original operation as stored in the database.
	 * @return the original operation
	 */
	public String getOriginalOperation() {
		return originalOperation;
	}
}
