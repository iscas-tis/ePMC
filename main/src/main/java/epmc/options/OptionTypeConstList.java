/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.options;

import static epmc.error.UtilError.ensure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Option type for lists of constants.
 * Constant definitions are given in the form
 * {@code --<option> value1=const1,value2=const2,...}. As this option type
 * supports being used multiple times to parse constant lists, something like
 * {@code --<option> value1=const1 value2=const2 ... --<option> value3=const3}
 * is also possible. From the {@code value}s and {@code const}s any whitespace
 * before and after will be removed before parsing.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeConstList implements OptionType {
    /** String containing ",". */
    private final static String COMMA = ",";
    /** String containing "=". */
    private final static String EQUALS = "=";
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<name>=<val>(,<name>=<val>)*";
    /** String used to suppress unchecked warning. */
    private final static String UNCHECKED = "unchecked";
    /** Constant list option type. */
    private final static OptionTypeConstList INSTANCE = new OptionTypeConstList();

    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeConstList#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeConstList() {
    }

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        Map<String,Object> result;
        if (prevValue == null) {
            result = new LinkedHashMap<>();
        } else {
            result = uncheckedCast(prevValue);
        }
        if (value.length() == 0) {
            return result;
        }
        String[] pairs = value.split(COMMA);
        for (String pair : pairs) {
            String[] pairSplit = pair.split(EQUALS);
            ensure(pairSplit.length == 2, ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            String key = pairSplit[0].trim();
            String entry = pairSplit[1].trim();
            ensure(!result.containsKey(key), ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            result.put(key, entry);
        }
        return result;
    }

    @SuppressWarnings(UNCHECKED)
    private Map<String, Object> uncheckedCast(Object prevValue) {
        return (Map<String,Object>) prevValue;
    }

    @Override
    public String getInfo() {
        return INFO;
    }

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public String unparse(Object value) {
        assert value != null;
        assert value instanceof Map;
        Map<?,?> valueMap = (Map<?,?>) value;
        for (Entry<?, ?> entry : valueMap.entrySet()) {
            assert entry.getKey() instanceof String;
        }
        StringBuilder builder = new StringBuilder();
        for (Entry<?, ?> entry : valueMap.entrySet()) {
            builder.append(entry.getKey());
            builder.append(EQUALS);
            builder.append(entry.getValue());
            builder.append(COMMA);
        }
        if (builder.length() > 0) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }

    @Override
    public Object getDefault() {
        return new LinkedHashMap<String, Object>();
    }

    /**
     * Get constant list option type.
     * 
     * @return constant list option type
     */
    public static OptionTypeConstList getInstance() {
        return INSTANCE;
    }
}
