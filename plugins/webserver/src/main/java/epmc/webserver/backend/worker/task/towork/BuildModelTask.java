package epmc.webserver.backend.worker.task.towork;

import epmc.webserver.common.TaskOperation;

/**
 * A class representing a model to be syntactically checked and constructed
 * @author ori
 */
public class BuildModelTask extends ModelTask {

	/**
	 * Create a new task identified by taskId for the user identified by userId for the given model and options.
	 * The task requires a build operation.
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param model_type the type of the task, such as prism, qmc, ...
	 * @param model the actual model to work on
	 * @param options the options for this model
	 */
	public BuildModelTask(int userId, int taskId, String model_type, String model, String options) {
		super(userId, taskId, TaskOperation.build, model_type, model, options);
	}
	
}
