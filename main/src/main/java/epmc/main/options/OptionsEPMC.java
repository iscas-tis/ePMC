package epmc.main.options;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Options of main part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsEPMC {
    /** base name of resource bundle */
    OPTIONS_EPMC,
    /** {@link Locale} used in these options */
    LOCALE,
    /** {@link String} {@link List} of model input files */
    MODEL_INPUT_FILES,
    /** {@link String} {@link List} of property input files */
    PROPERTY_INPUT_FILES,
    /** port number ({@link Integer}) to use for analysis */
    PORT,
    /** RMI name used for server started */
    SERVER_NAME,
    /** whether to print stack trace if user exception thrown */
    PRINT_STACKTRACE,
    /** run mode of EPMC {@link OptionsTypesEPMC.RunMode}) */
    RUN_MODE,
    /** {@link Map} from command {@link String} to available command {@link Class} */
    COMMAND_CLASS,    
}
