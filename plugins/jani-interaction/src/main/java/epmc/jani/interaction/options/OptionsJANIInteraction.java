package epmc.jani.interaction.options;

/**
 * Class collecting options used for JANI interaction plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsJANIInteraction {
	/** Base name of resource file for options description. */
	OPTIONS_JANI_INTERACTION,
	/** Category used for JANI interaction options. */
	JANI_INTERACTION_CATEGORY,
    /** WebSocket port to listen to. */
	JANI_INTERACTION_WEBSOCKET_SERVER_PORT ,
	/** JANI interaction type. */
	JANI_INTERACTION_TYPE,
	/** Whether to allow anonymous logins in WebSocket mode. */
	JANI_INTERACTION_WEBSOCKET_ANONYMOUS_LOGINS,
	JANI_INTERACTION_EXTENSION_CLASS,
	/** Result formatter classes. */
	JANI_INTERACTION_RESULT_FORMATTER_CLASS,
	/** Server type to use for analysis interaction. */
	JANI_INTERACTION_ANALYSIS_SERVER_TYPE,
	/** Whether to start GUI when server is started. */
	JANI_INTERACTION_START_GUI,
	/** Name of user to modify for jani-interaction-modify-user or to add for
	 * jani-interaction-add-user. */
	JANI_INTERACTION_MODIFIED_USERNAME,
	/** New user name for jani-interaction-modify-user. */
	JANI_INTERACTION_NEW_USERNAME,
	/** New password for jani-interaction-modify-password. */
	JANI_INTERACTION_MODIFIED_PASSWORD,
	/** Whether to print out messages sent from and to server in WebSocket
	 * mode for debugging. */
	JANI_INTERACTION_PRINT_MESSAGES;
	
	/**
	 * Server type to use.
	 * 
	 * @author Ernst Moritz Hahn
	 */
	public enum ServerType {
		/** Run analysis server in same process as EPMC.
		 * This makes testing easier than running the task in a separate
		 * process, but also means that bugs might crash the tool rather than
		 * just the server the analysis runs in.*/
		SAME_PROCESS,
		/** Start a new process to run server. */
		LOCAL
	}	
}
