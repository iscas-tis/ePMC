package epmc.webserver.common;

/**
 * Enumeration representing the status of a task
 * @author ori
 */
public enum TaskStatus {

	/**
	 * Task that has just been created and not yet computed
	 */
	pending,

	/**
	 * Task that is currently under elaboration by the backend
	 */
	computing,

	/**
	 * Task that has been processed with success
	 */
	completed,

	/**
	 * Task that has been processes but has not passed the model checker analysis
	 */
	failedCheck,

	/**
	 * Task that has been processes but has failed the syntax check
	 */
	failedParse,

	/**
	 * Task that has been processes but has failed due to a too lengthly computation
	 */
	failedTimedOut,

	/**
	 * Task that has been processes but has failed due to a too high usage of memory
	 */
	failedMemory,

	/**
	 * Task that has been processes but has failed due to an internal error in the JVM
	 */
	failedInternalJVMError,

	/**
	 * Task that has been processes but has failed due to an unknown error in the JVM
	 */
	failedUnknownJVMError,

	/**
	 * Task that has been processes but has failed due to a different error in the JVM
	 */
	failedJVMError,

	/**
	 * Task that has been processes but has failed due to a too high usage of stack
	 */
	failedStack,

	/**
	 * Task that has been processes but has been rejected since it used invalid options
	 */
	failedInvalidOptions,

	/**
	 * Task that has been processes but has failed due to internal errors in the backend
	 */
	failedInternalError,
	
	/**
	 * Task that has been processes but the resulting task has unknown type
	 */
	unknownFailed
}
