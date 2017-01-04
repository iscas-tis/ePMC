package epmc.jani.exporter.options;

/**
 * Class collecting options used for JANI converter plugin.
 * 
 * @author Andrea Turrini
 */
public enum OptionsJANIExporter {
	/** Base name of resource file for options description. */
	OPTIONS_JANI_EXPORTER,
	/** Category used for JANI converter options. */
	JANI_EXPORTER_CATEGORY,
	/** Name of the generated JANI file. */
	JANI_EXPORTER_JANI_FILE_NAME,
	/** Name of the generated JANI model. */
	JANI_EXPORTER_JANI_MODEL_NAME,
	/** Whether to print out messages during the export. */
	JANI_EXPORTER_PRINT_MESSAGES,
	/** Prefix for reward structure names. */
	JANI_EXPORTER_REWARD_NAME_PREFIX;
}
