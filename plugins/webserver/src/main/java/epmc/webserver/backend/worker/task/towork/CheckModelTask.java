package epmc.webserver.backend.worker.task.towork;

import java.util.List;

import epmc.webserver.common.Formula;
import epmc.webserver.common.TaskOperation;

/**
 * A class representing a model to be model checked with respect to a given list of formulae
 * @author ori
 */
public class CheckModelTask extends ModelTask {
	private final List<Formula> formulae;
	private final int timeout;

	/**
	 * Create a new task identified by taskId for the user identified by userId for the given model and options.
	 * The task requires to verify properties.
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param modelType the type of the task, such as prism, qmc, ...
	 * @param modelContent  the actual model to work on
	 * @param formulae the list of formulae to check
	 * @param timeout the timeout for this task in minutes
	 * @param options the options for this model
	 */
	public CheckModelTask(int userId, int taskId, String modelType, String modelContent, List<Formula> formulae, int timeout, String options) {
		super(userId, taskId, TaskOperation.checkFormula, modelType, modelContent, options);
		this.formulae = formulae;
		this.timeout = timeout;
	}
	
	/**
	 * Return the timeout associated with this task, in minutes.
	 * @return the timeout in minutes
	 */
	public int getTimeOutInMinutes() {
		return timeout;
	}
	
	/**
	 * Return the list of formulae to be checked
	 * @return the list of formulae
	 */
	public List<Formula> formulae() {
		return formulae;
	}
}
