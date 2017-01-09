package epmc.options;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

// TODO move out of main part

public class OptionTypeIntegerInterval implements OptionType {
    private final static String LBRACK = "[";
    private final static String RBRACK = "]";
    private final static String COMMA = ",";

    private final int lower;
    private final int upper;
    private final String info;

    public OptionTypeIntegerInterval(int lower, int upper) {
        assert lower <= upper;
        this.lower = lower;
        this.upper = upper;
        info = LBRACK + lower + COMMA + upper + RBRACK;
    }
    
    @Override
    public Object parse(String value, Object prevValue)
            throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        try {
            int valueInt = Integer.parseInt(value);
            ensure(lower <= valueInt && valueInt <= upper,
                    ProblemsOptions.OPTIONS_VALUE_OUTSIDE_INTERVAL, value, lower, upper);
            return value;
        } catch (NumberFormatException e) {
            fail(ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, e, value);
            return null;
        }
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return getInfo();
    }
}
