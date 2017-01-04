package epmc.webserver.backend.worker.task.worked.failed;

import epmc.webserver.common.TaskOperation;

/**
 * A task representing a failed task, i.e., a task that has been finished but with errors during the analysis
 * @author ori
 */
public class FailedAnalysisTask extends FailedTask {
	private final String errorKey;
	private final String errorArgument;

	/**
	 * Create a new failed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param errorKey the key of the error describing the failure
	 * @param errorArgument the argument of the error
	 * @param error the error describing the failure
	 */
	public FailedAnalysisTask(int userId, int taskId, TaskOperation operation, String errorKey, Object[] errorArgument, String error) {
		super(userId, taskId, operation, error);
		this.errorKey = errorKey;
		StringBuilder ea = new StringBuilder();
		for (int i = 0; i < errorArgument.length; i++) {
			if (i > 0) {
				ea.append('\n');
			}
			ea.append(i).append('=').append(errorArgument[i]);
		}
		this.errorArgument = ea.toString();
	}
	
	public String getErrorKey() {
		return errorKey;
	}
	
	public String getErrorArgument() {
		return errorArgument;
	}
}
