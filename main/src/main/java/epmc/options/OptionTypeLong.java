package epmc.options;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

// TODO move out of main part

/**
 * Option type for long options.
 * The values will be read by {@link Long#parseLong(String)}. Strings which
 * cannot be parsed correctly this way will result in an
 * {@link EPMCException} being thrown.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeLong implements OptionType {
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<long>";
    /** Long option type. */
    private final static OptionTypeLong INSTANCE = new OptionTypeLong();
    
    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeLong#getTypeLong()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeLong() {
    }
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        try {
            Long.parseLong(value);
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
     * Get long integer option type.
     * 
     * @return long integer option type
     */
    public static OptionTypeLong getTypeLong() {
        return INSTANCE;
    }
}
