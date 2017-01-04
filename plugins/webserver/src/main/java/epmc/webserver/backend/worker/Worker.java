package epmc.webserver.backend.worker;

import epmc.EPMCServer;

/**
 * A generic worker
 * @author ori
 */
public abstract class Worker extends Thread {

	/**
	 * Used to terminate the workers that have to be always active
	 */
	protected boolean canRun = true;
	private Process mc = null;
	
	/**
	 * Shutdown the current thread in a grace way and kills the associated {@linkplain Process}
	 */
	public void shutdown() {
		canRun = false;
		killProcess();
	}
	
	/**
	 * Set the associated process, like the remote instance of the {@linkplain EPMCServer EPMC server}
	 * @param mc
	 */
	protected synchronized void setProcess(Process mc) {
		this.mc = mc;
	}
	
	/**
	 * Kill the associated process via a destroy actions 
	 */
	protected synchronized final void killProcess() {
		if (mc != null) {
			mc.destroy();
			mc = null;
		}
	}
	
	/**
	 * Generates an exception if the process is still active (cf. {@linkplain Process#exitValue()}
	 * @throws IllegalThreadStateException if the process has not yet exited
	 */
	protected synchronized void checkExitValue() throws IllegalThreadStateException {
		if (mc != null) {
			mc.exitValue();
		}
	}
}
