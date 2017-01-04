package epmc.webserver.backend.worker.task.worked.completed;

/**
 * A task representing a completed build task, i.e., a task requiring the generation of a model that has been successfully finished
 * @author ori
 */
public class CompletedBuildModelTask extends CompletedTask {
	// TODO
	//	private final ModelPRISM model;
	private CompletedBuildModelTask() {
		super(0, 0, null);
	}
	/**
	 * Create a new completed task identified by taskId for the user identified by userId
	 * @param userId the identifier of the user this task belongs to, as in the database
	 * @param taskId the identifier of this task, as in the database
	 * @param operation the operation to perform on this task, as extracted from the database
	 * @param model the resulting model
	 */
	// TODO
//	public CompletedBuildModelTask(int userId, int taskId, TaskOperation operation, ModelPRISM model) {
	//	super(userId, taskId, operation);
//		this.model = model;
//	}
	
	/**
	 * Return the constructed model
	 * @return the model
	 */
	// TODO
//	public ModelPRISM getModel() {
	//	return model;
//	}
	
	@Override
	public String toString() {
		return "Task " + getTaskId() + " of user " + getUserId() + ": completed";
	}
}
