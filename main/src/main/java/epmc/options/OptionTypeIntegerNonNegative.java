package epmc.options;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

//TODO move out of main part

/**
 * Option type for nonnegative integer options.
 * The values will be read by {@link Integer#parseInt(String)}. Strings which
 * cannot be parsed correctly this way will result in an
 * {@link EPMCException} being thrown. An {@link EPMCException} will also
 * be thrown if the string could be parsed but represents a negative number.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeIntegerNonNegative implements OptionType {
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<integer>";
    /** Nonnegative integer option type. */
    private final static OptionTypeIntegerNonNegative INSTANCE = new OptionTypeIntegerNonNegative();
    
    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeIntegerNonNegative#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeIntegerNonNegative() {   
    }
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        try {
            int intValue = Integer.parseInt(value);
            ensure(intValue >= 0, ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
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
     * Get nonnegative integer option type.
     * 
     * @return nonnegative integer option type
     */
    public static OptionTypeIntegerNonNegative getInstance() {
        return INSTANCE;
    }
}
