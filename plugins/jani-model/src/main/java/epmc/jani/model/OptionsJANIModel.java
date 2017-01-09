package epmc.jani.model;

/**
 * Class collecting options used for parser part of JANI model plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsJANIModel {
	/** Base name of resource file for options description. */
	OPTIONS_JANI_MODEL,
	/** Category of JANI model options. */
	JANI_MODEL_CATEGORY,
	/** Option whether deadlocks are allowed and will be fixed automatically. */
    JANI_FIX_DEADLOCKS,
    /** Number of action encoding bits to reserve. */
    JANI_ACTION_BITS,
	/** Storage point for JANI model extensions. */
	JANI_MODEL_EXTENSION_CLASS,
    /** JANI model extension semantics. */
	JANI_MODEL_EXTENSION_SEMANTICS,
}
