package epmc.jani.interaction.options;

/**
 * Class collecting options used for JANI interaction plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsJANIInteractionJDBC {
	/** Base name of resource file for options description. */
	OPTIONS_JANI_INTERACTION_JDBC,
	/** Category used for JANI interaction options. */
	JANI_INTERACTION_JDBC_CATEGORY,
	/** JAR file containing a JDBC driver to load. */
	JANI_INTERACTION_JDBC_DRIVER_JAR,
	/** JDBC driver class to load. */
	JANI_INTERACTION_JDBC_DRIVER_CLASS,
    /** URL to use to establish JDBC connection. */
	JANI_INTERACTION_JDBC_URL,
    /** User name to use to establish JDBC connection. */
	JANI_INTERACTION_JDBC_USERNAME,
    /** Password to use to establish JDBC connection. */
	JANI_INTERACTION_JDBC_PASSWORD,
    /** Type to use for auto-incrementing primary key. */
	JANI_INTERACTION_JDBC_DBTYPE_PRIMARY_KEY_AUTOINCREMENT,
	;
	
	/**
	 * Server type to use.
	 * 
	 * @author Ernst Moritz Hahn
	 */
	public enum ServerType {
		/** Run analysis server in same process as EPMC .*/
		SAME_PROCESS,
		/** Start a new process to run server. */
		LOCAL
	}
}
