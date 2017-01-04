package epmc.webserver.backend;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import epmc.EPMCServer;
import epmc.webserver.backend.worker.Worker;
import epmc.webserver.backend.worker.task.worked.WorkedTask;
import epmc.webserver.backend.worker.task.worked.intermediate.PartialTask;
import epmc.webserver.backend.worker.task.worked.intermediate.SingleFormulaTask;

/**
 * Object containing all data generated and exchanged in the {@link BackendEngine}.
 * To ensure the correct functionality, only a single instance of the DataStore is instantiated independently on the number of objects requiring it.
 * @author ori
 */
public class DataStore {
	private final Queue<WorkedTask> workedTask;
	private final Queue<Integer> modelCheckerPorts;
	private final Set<Worker> workers;
	private static final DataStore ds = new DataStore();
	
	private DataStore() {
		workedTask = new ArrayDeque<WorkedTask>();
		workers = Collections.synchronizedSet(new HashSet<Worker>());
		modelCheckerPorts = new ArrayDeque<Integer>(50000);
		for (int i = 10000; i < 60000; i++) {
				modelCheckerPorts.add(i);
		}
	}
	
	/**
	 * Provides an instance of the DataStore that is the same for each invocation
	 * @return the unique instance of the DataStore
	 */
	public static DataStore getDataStore() {
		return ds;
	}
	
	/**
	 * Extracts a port number from the set of available port numbers and ensures that no duplicated port number is used at the same time.
	 * @return the required port number
	 */
	public int getPortNumber() {
		Integer port;
		synchronized(modelCheckerPorts){
			port = modelCheckerPorts.poll();
		}		
		if (port != null) {
			return port;
		}
		return -1;
	}
	
	/**
	 * Releases a port number in order to make it available for a new {@linkplain EPMCServer}
	 * @param port the port to release
	 */
	public void releasePortNumber(int port) {
		synchronized(modelCheckerPorts) {
			if (!modelCheckerPorts.contains(port)) {
				modelCheckerPorts.add(port);
			}
		}
	}
	
	/**
	 * Adds a new {@link Worker} to the list of active workers
	 * @param worker the worker 
	 */
	public void addWorker(Worker worker) {
		synchronized(workers) {
			workers.add(worker);
		}
	}
	
	private void removeWorker(Worker w) {
		synchronized(workers) {
			workers.remove(w);
		}
	}
	
	/**
	 * Returns whether it is possible to run a worker
	 * @param maxWorkers the maximum number of workers that can be active simultaneously
	 * @return true if a worker can be started, false otherwise
	 */
	public boolean canRunWorker(int maxWorkers) {
		int nw;
		synchronized(workers) {
			nw = workers.size();
		}
		int np;
		synchronized(modelCheckerPorts) {
			np = modelCheckerPorts.size();
		}
		return (nw < maxWorkers) && (np > 0); 
	}

	/**
	 * Returns the number of active workers currently registered in the {@linkplain DataStore}
	 * @return the number of workers
	 */
	public int getWorkers() {
		int n;
		synchronized(workers) {
			n = workers.size();
		}
		return n;
	}
	
	/**
	 * Sends to all registered {@linkplain Worker workers} the shutdown signal
	 */
	public void shutdownWorkers() {
		for (Worker w : workers) {
			w.shutdown();
		}
	}

	/**
	 * Joins all active {@linkplain Worker workers} until they terminate
	 */
	public void waitForWorkers() {
		for (Worker w : workers) {
			try {
				w.join();
			} catch (InterruptedException ie) {};
		}
	}
	
	/**
	 * Extract and return a {@linkplain WorkedTask worked task} that is in this {@linkplain DataStore}.
	 * @return a worked task; null if no worked task available
	 */
	public WorkedTask getWorkedTask() {
		WorkedTask pair;
		synchronized(workedTask) {
			pair = workedTask.poll();
		}
		return pair;
	}
	
	/**
	 * Add a {@linkplain WorkedTask worked task} to the list of worked tasks and remove the corresponding {@linkplain Worker worker} from the registered workers.
	 * @param worker the worker to remove
	 * @param task the task to add
	 */
	public void addWorkedTask(Worker worker, WorkedTask task) {
		if (task != null) {
			synchronized(workedTask) {
				workedTask.add(task);
			}
		}
		removeWorker(worker);
		synchronized(ds) {
			ds.notifyAll();
		}
	}
	
	/**
	 * Readd a previously retrieved {@linkplain WorkedTask worked task} to the available worked tasks. 
	 * Useful in case of database problems.
	 * @param task the task to be added
	 */
	public void readdWorkedTask(WorkedTask task) {
		if (task != null) {
			synchronized(workedTask) {
				workedTask.add(task);
			}
		}
		synchronized(ds) {
			ds.notifyAll();
		}
	}
	
	/**
	 * Add a {@linkplain PartialTask partial task} to the available worked tasks.
	 * @param task the task to be added
	 */
	public void addPartialTask(PartialTask task) {
		if (task != null) {
			synchronized(workedTask) {
				workedTask.add(task);
			}
		}
		synchronized(ds) {
			ds.notifyAll();
		}
	}
	
	/**
	 * Add a {@linkplain SingleFormulaTask single formula task} to the available worked tasks.
	 * @param task the task to be added
	 */
	public void addSingleFormulaTask(SingleFormulaTask task) {
		if (task != null) {
			synchronized(workedTask) {
				workedTask.add(task);
			}
		}
		synchronized(ds) {
			ds.notifyAll();
		}
	}
}
