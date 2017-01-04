package epmc.webserver.backend.worker;

import epmc.webserver.backend.DataStore;
import epmc.webserver.backend.worker.task.towork.InvalidModelTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedInvalidTask;
import epmc.webserver.common.TaskOperation;

/**
 * Helper worker for task corresponding to invalid models
 * @author ori
 */
public class InvalidModelWorker extends Worker {
	private final DataStore dataStore = DataStore.getDataStore();
	private final InvalidModelTask model;
	
	/**
	 * Generate a new worker for the invalid model
	 * @param model the invalid model
	 */
	public InvalidModelWorker(InvalidModelTask model) {
		this.model = model;
	}

	@Override
	public void run() {
		dataStore.addWorkedTask(this, new FailedInvalidTask(model.getUserId(), model.getTaskId(), TaskOperation.invalidOperation, model.getOriginalOperation()));
	}
}
