package epmc.webserver.backend.worker.task.worked.intermediate;

import epmc.modelchecker.RawProperty;
import epmc.webserver.common.TaskOperation;

/**
 * A task representing an intermediate task, i.e., a task that has not yet been finished but that has provided the result for a formula
 * @author ori
 */
public class SingleFormulaTask extends IntermediateTask {
	private final int formulaId;
	private final RawProperty formula;
	private final String value;

	/**
	 * Create a new single formula task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param formulaId the identifier of the formula as from the database
	 * @param formula the actual formula
	 * @param value the corresponding value
	 */
	public SingleFormulaTask(int userId, int taskId, TaskOperation operation, int formulaId, RawProperty formula, String value) {
		super(userId, taskId, operation);
		this.formulaId = formulaId;
		this.formula = formula;
		this.value = value;
	}
	
	/**
	 * Returns the formula identifier of the formula whose result is reported in this task
	 * @return the formula identifier
	 */
	public int getFormulaId() {
		return formulaId;
	}
	
	/**
	 * Returns the value of the formula whose result is reported in this task
	 * @return the value of the formula
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Returns the message corresponding to the current formula
	 * @return the representation of the property value
	 */
	public String getMessage() {
		return formula + ": " + value;
	}
	
	@Override
	public String toString() {
		return super.toString() + " Formula " + formulaId + " " + formula + " value: " + value;
	}
}
