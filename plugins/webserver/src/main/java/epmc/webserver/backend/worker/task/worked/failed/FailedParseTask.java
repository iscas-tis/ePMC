package epmc.webserver.backend.worker.task.worked.failed;

import epmc.webserver.common.TaskOperation;

/**
 * A task representing a parsing failed task, i.e., a task that has been finished but with errors at syntactic level
 * @author ori
 */
public class FailedParseTask extends FailedTask {
	private final String errorIdentifier;
	private final int errorColumn;
	private final int errorLine;

	/**
	 * Create a new failed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param code the error code describing the failure
	 * @param line the line where the error occurred
	 * @param column the column where the error occurred
	 * @param identifier the identifier, if any, that has caused the error (eg., a duplicated variable)
	 */
	public FailedParseTask(int userId, int taskId, TaskOperation operation, String code, int line, int column, String identifier) {
		super(userId, taskId, operation, code);
		errorColumn = column;
		errorIdentifier = identifier;
		errorLine = line;
	}
	
	/**
	 * Return the line of the syntax error
	 * @return the line
	 */
	public int getLine() {
		return errorLine;
	}
	
	/**
	 * Return the column of the syntax error
	 * @return the column
	 */
	public int getColumn() {
		return errorColumn;
	}
	
	/**
	 * Return the identifier causing the syntax error
	 * @return the identifier, null if no identifier involved in the syntax error
	 */
	public String getIdentifier() {
		return errorIdentifier;
	}
}
