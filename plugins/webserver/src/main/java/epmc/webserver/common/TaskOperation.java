package epmc.webserver.common;

/**
 * Enumeration for the supported operations that can be used for tasks
 * @author ori
 */
public enum TaskOperation {

	/**
	 * Special marker for invalid operations
	 */
	invalidOperation,

	/**
	 * To require the generation of the model
	 */
	build,

	/**
	 * To require the model checking of formulas
	 */
	checkFormula,

	/**
	 *  equivalent to {@linkplain #checkFormula}
	 */
	analyze
}
