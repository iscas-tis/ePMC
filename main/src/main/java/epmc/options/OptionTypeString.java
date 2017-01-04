package epmc.options;

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;

/**
 * Option type parsing any string.
 * This option type allows any string to be parsed and stored, after trimming.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeString implements OptionType {
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<string>";
    /** String option type. */
    final static OptionTypeString INSTANCE = new OptionTypeString();

    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeString#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeString() {
    }
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        return value;
    }
    
    @Override
    public String getInfo() {
        return INFO;
    }
    
    @Override
    public String toString() {
        return getInfo();
    }

    /**
     * Get string option type.
     * 
     * @return string option type
     */
    public static OptionTypeString getInstance() {
        return INSTANCE;
    };    
}
