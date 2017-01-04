package epmc.webserver.backend.worker;

import epmc.webserver.backend.BackendEngine;
import epmc.webserver.backend.DataStore;
import epmc.webserver.backend.dbms.DBMS;
import epmc.webserver.backend.mailer.MailServer;
import epmc.webserver.backend.worker.task.worked.WorkedTask;
import epmc.webserver.backend.worker.task.worked.completed.CompletedTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedAnalysisTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedInvalidTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedParseTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedRuntimeTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedTimedOutTask;
import epmc.webserver.backend.worker.task.worked.intermediate.IntermediateTask;
import epmc.webserver.backend.worker.task.worked.intermediate.PartialTask;
import epmc.webserver.backend.worker.task.worked.intermediate.SingleFormulaTask;
import epmc.webserver.backend.worker.task.worked.system.SystemFailureTask;
import epmc.webserver.backend.worker.task.worked.system.SystemIrreversibleFailureTask;
import epmc.webserver.backend.worker.task.worked.system.SystemShutdownTask;
import epmc.webserver.backend.worker.task.worked.system.SystemTask;
import epmc.webserver.common.EPMCDBException;

/**
 * Worker that takes the results from the {@linkplain DataStore} and stores the in the database via the {@link DBMS}.
 * It is supposed to run forever
 * @author ori
 */
public class ResultCollector extends Worker {
	private final DataStore dataStore = DataStore.getDataStore();
	private final DBMS dbms;
	
	/**
	 * Construct a new result collector that interacts with the given database
	 * @param dbms the dbms to use for the database activity
	 */
	public ResultCollector(DBMS dbms) {
		this.dbms = dbms;
	}
	
	private void manageCompletedTask(CompletedTask workedTask) throws EPMCDBException {
		dbms.putCompletedModel(workedTask);
	}
	
	private void manageSystemTask(SystemTask workedTask) throws EPMCDBException {
		if (workedTask instanceof SystemFailureTask) {
			dbms.putSystemFailure((SystemFailureTask)workedTask);
		} else if (workedTask instanceof SystemIrreversibleFailureTask) {
			dbms.putSystemIrreversibleFailure((SystemIrreversibleFailureTask)workedTask);
		} else if (workedTask instanceof SystemShutdownTask) {
			dbms.putSystemShutdown((SystemShutdownTask)workedTask);
		}
	}
	
	private void manageIntermediateTask(IntermediateTask workedTask) throws EPMCDBException {
		if (workedTask instanceof PartialTask) {
			dbms.putPartialModel((PartialTask)workedTask);
		} else if (workedTask instanceof SingleFormulaTask) {
			dbms.putSingleFormulaModel((SingleFormulaTask)workedTask);
		}
	}
	
	private void manageFailedTask(FailedTask workedTask) throws EPMCDBException {
		if (workedTask instanceof FailedParseTask) {
			dbms.putFailedParseModel((FailedParseTask)workedTask);
		} else if (workedTask instanceof FailedRuntimeTask) {
			FailedRuntimeTask failed = (FailedRuntimeTask)workedTask;
			if (BackendEngine.sendMail()) {
				System.err.println("System runtime mail sent status: " + MailServer.sendRuntimeErrorMail(failed));
			}
			dbms.putFailedRuntimeModel(failed);
		} else if (workedTask instanceof FailedInvalidTask) {
			dbms.putFailedInvalidModel((FailedInvalidTask)workedTask);
		} else if (workedTask instanceof FailedTimedOutTask) {
			dbms.putFailedTimedOutModel((FailedTimedOutTask)workedTask);
		} else if (workedTask instanceof FailedAnalysisTask) {
			dbms.putFailedAnalysisModel((FailedAnalysisTask)workedTask);
		} else {
			if (BackendEngine.sendMail()) {
				System.err.println("Unknown task mail sent status: " + MailServer.sendUnknownTaskMail(workedTask));
			}
			//unknown task type, it should never be reached
			System.err.println("Dropping unknown task: " + workedTask);
			dbms.putUnknownTask(workedTask);
		}
	}
	
	@Override
	public void run() {
		while (canRun) {
			WorkedTask workedTask = null;
			try {
				synchronized (dataStore) {
					dataStore.wait(2500);
				}
				while ((workedTask = dataStore.getWorkedTask()) != null) {
					if (workedTask instanceof CompletedTask) {
						manageCompletedTask((CompletedTask)workedTask);
					} else if (workedTask instanceof SystemTask) {
						manageSystemTask((SystemTask)workedTask);
					} else if (workedTask instanceof IntermediateTask) {
						manageIntermediateTask((IntermediateTask)workedTask);
					} else if (workedTask instanceof FailedTask) {
						manageFailedTask((FailedTask)workedTask);
					} else {
						//unknown task type, it should never be reached
						System.err.println("Dropping unknown task: " + workedTask);
					}
					
					synchronized(dataStore) {
						dataStore.notifyAll();
					}
				}
			} catch (EPMCDBException ime) {
				System.err.println("Database error occurred: " + ime.getMessage());
				dataStore.readdWorkedTask(workedTask);
			} 
			catch (InterruptedException ie) {}
		}
	}
}
