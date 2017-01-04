package epmc.options;


import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

// TODO move out of main part

/**
 * Option type for nonnegative integer or default value options.
 * If the value feed to this type is "default", it will return this particular
 * string. Otherwise, values will be read by {@link Integer#parseInt(String)}.
 * Strings which cannot be parsed correctly this way will result in an
 * {@link EPMCException} being thrown. An {@link EPMCException} will also
 * be thrown if the string could be parsed but represents a negative number.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeIntegerNonNegativeOrDefault implements OptionType {
    /** String containing "default". */
	private final static String DEFAULT = "default";
    /** String containing "|". */
	private final static String PIPE = "|";
    /** String containing "<". */
	private final static String LANGLE = "<";
    /** String containing ">". */
	private final static String RANGLE = ">";
    /** String containing "non-neg-integer". */
	private final static String NON_NEG_INTEGER = "non-neg-integer";
    /** Nonnegative integer option type or "default". */    
    private final static OptionTypeIntegerNonNegativeOrDefault INSTANCE = new OptionTypeIntegerNonNegativeOrDefault();
	
    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeIntegerNonNegativeOrDefault#getTypeIntegerNonNegativeOrDefault()} rather than by
     * directly calling the constructor.
     */
	private OptionTypeIntegerNonNegativeOrDefault() {
    }
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        if (value.equals(DEFAULT)) {
            return value;
        }
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
        return DEFAULT + PIPE + LANGLE + NON_NEG_INTEGER + RANGLE;
    }
    
    @Override
    public String toString() {
        return getInfo();
    }
    
    @Override
    public Object getDefault() {
        return DEFAULT;
    }

    /**
     * Get nonnegative integer or default option type.
     * The option type returned accepts either nonnegative integers or the word
     * "default".
     * 
     * @return nonnegative integer or default option type
     */
    public static OptionTypeIntegerNonNegativeOrDefault getTypeIntegerNonNegativeOrDefault() {
        return INSTANCE;
    }
}
