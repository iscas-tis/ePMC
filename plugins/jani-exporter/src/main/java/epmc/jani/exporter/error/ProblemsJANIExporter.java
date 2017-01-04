package epmc.jani.exporter.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in JANI converter plugin.
 * 
 * @author Andrea Turrini
 */
public final class ProblemsJANIExporter {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String ERROR_JANI_EXPORTER = "ErrorJANIExporter";
    /** Missing PRISM file name. */
    public final static Problem JANI_EXPORTER_MISSING_INPUT_MODEL_FILENAMES = newProblem("jani-exporter-missing-input-model-filenames");
    /** Unaccessible PRISM file name. */
    public final static Problem JANI_EXPORTER_UNACCESSIBLE_FILENAME = newProblem("jani-exporter-unaccessible-filename");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(ERROR_JANI_EXPORTER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIExporter() {
    }
}
