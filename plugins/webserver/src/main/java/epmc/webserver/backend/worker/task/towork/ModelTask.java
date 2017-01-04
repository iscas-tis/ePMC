package epmc.webserver.backend.worker.task.towork;

import epmc.webserver.backend.worker.task.Task;
import epmc.webserver.common.TaskOperation;

/**
 * An abstract class representing a task for a model to be computed
 * @author ori
 */
public abstract class ModelTask extends Task {
	private final String model;
	private final String modelType;
	private final String options;
	
	/**
	 * Create a new task identified by taskId for the user identified by userId for the given model and options
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param modelType the type of the task, such as prism, qmc, ...
	 * @param model the actual model to work on
	 * @param options the options for this model
	 */
	protected ModelTask(int userId, int taskId, TaskOperation operation, String modelType, String model, String options) {
		super(userId, taskId, operation);
		this.model = model;
		this.modelType = modelType;
		this.options = options;
	}
	
	/**
	 * Return the model associated to this task
	 * @return the model
	 */
	public String getModel() {
		return model;
	}
	
	/**
	 * Return the model type associated to this task
	 * @return the model type
	 */
	public String getModelType() {
		return modelType;
	}
	
	/**
	 * Return the options associated to this task
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}
}
