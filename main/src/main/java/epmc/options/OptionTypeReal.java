package epmc.options;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

// TODO move out of main part

/**
 * Option type for real options.
 * The values will be read by {@link Double#parseDouble(String)}. Strings which
 * cannot be parsed correctly this way will result in an
 * {@link EPMCException} being thrown.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeReal implements OptionType {
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<real>";
    /** Real option type. */
    private final static OptionTypeReal INSTANCE = new OptionTypeReal();
    
    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeReal#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeReal() {
    }
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException e) {
            fail(ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, e, value);
            return null;
        }
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
     * Get real option type.
     * 
     * @return real option type
     */
    public static OptionTypeReal getInstance() {
        return INSTANCE;
    }
}
