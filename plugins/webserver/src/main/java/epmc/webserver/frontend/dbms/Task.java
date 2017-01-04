package epmc.webserver.frontend.dbms;

import epmc.webserver.common.TaskOperation;
import epmc.webserver.common.TaskStatus;

/**
 *
 * @author ori
 */
public class Task {
	private int taskId;
	private int userId;
	private int modelId;
	private String modelName;
	private String modelContent;
	private String modelComment;
	private TaskStatus taskStatus;
	private String taskOptions;
	private TaskOperation taskOperation;
	private String taskCreationTimestamp;
	private String taskStartElaborationTimestamp;
	private String taskCompletionTimestamp;

	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the modelId
	 */
	public int getModelId() {
		return modelId;
	}

	/**
	 * @param modelId the modelId to set
	 */
	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	/**
	 * @return the modelName
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * @param modelName the modelName to set
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * @return the modelContent
	 */
	public String getModelContent() {
		return modelContent;
	}

	/**
	 * @param modelContent the modelContent to set
	 */
	public void setModelContent(String modelContent) {
		this.modelContent = modelContent;
	}

	/**
	 * @return the modelComment
	 */
	public String getModelComment() {
		return modelComment;
	}

	/**
	 * @param modelComment the modelComment to set
	 */
	public void setModelComment(String modelComment) {
		this.modelComment = modelComment;
	}

	/**
	 * @return the taskStatus
	 */
	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	/**
	 * @param taskStatus the taskStatus to set
	 */
	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	/**
	 * @return the taskOptions
	 */
	public String getTaskOptions() {
		return taskOptions;
	}

	/**
	 * @param taskOptions the taskOptions to set
	 */
	public void setTaskOptions(String taskOptions) {
		this.taskOptions = taskOptions;
	}

	/**
	 * @return the taskOperation
	 */
	public TaskOperation getTaskOperation() {
		return taskOperation;
	}

	/**
	 * @param taskOperation the taskOperation to set
	 */
	public void setTaskOperation(TaskOperation taskOperation) {
		this.taskOperation = taskOperation;
	}

	/**
	 * @return the taskCreationTimestamp
	 */
	public String getTaskCreationTimestamp() {
		return taskCreationTimestamp;
	}

	/**
	 * @param taskCreationTimestamp the taskCreationTimestamp to set
	 */
	public void setTaskCreationTimestamp(String taskCreationTimestamp) {
		this.taskCreationTimestamp = taskCreationTimestamp;
	}

	/**
	 * @return the taskStartElaborationTimestamp
	 */
	public String getTaskStartElaborationTimestamp() {
		return taskStartElaborationTimestamp;
	}

	/**
	 * @param taskStartElaborationTimestamp the taskStartElaborationTimestamp to set
	 */
	public void setTaskStartElaborationTimestamp(String taskStartElaborationTimestamp) {
		this.taskStartElaborationTimestamp = taskStartElaborationTimestamp;
	}

	/**
	 * @return the taskCompletionTimestamp
	 */
	public String getTaskCompletionTimestamp() {
		return taskCompletionTimestamp;
	}

	/**
	 * @param taskCompletionTimestamp the taskCompletionTimestamp to set
	 */
	public void setTaskCompletionTimestamp(String taskCompletionTimestamp) {
		this.taskCompletionTimestamp = taskCompletionTimestamp;
	}
}
