package epmc.webserver.frontend.webtomodelchecker;

/**
 * An object representing a single EPMC option.
 * @author ori
 */
public final class EPMCOption {

	/**
	 * The name if the option
	 */
	public final String name;

	/**
	 * The type of the option
	 */
	public final String type;

	/**
	 * The default value of the option
	 */
	public final String defaultValue;

	/**
	 * The comment for the option
	 */
	public final String comment;

	/**
	 * Create a new EPMC option with the given information
	 * @param name the name if the option
	 * @param type the type of the option
	 * @param defaultValue the default value of the option
	 * @param comment the comment for the option
	 */
	public EPMCOption(String name, String type, String defaultValue, String comment) {
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.comment = comment;
	}
}
