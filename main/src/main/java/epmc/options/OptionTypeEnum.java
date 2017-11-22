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
import static epmc.error.UtilError.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Option type for enums.
 * For the enum classes parsed by this parser, it is assumed that their
 * constants only consist of capital letters and underscore ('_'). The parser
 * however parses {@link String}s of lower-case letters and dash ('-'), thereby
 * turning the {@link String} to parse into upper case and replacing '-' by '_'.
 * The string obtained this way will be interpreted as the name of enum constant
 * to be obtained.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeEnum implements OptionType {
    /** String containing "<". */
    private final static String LESS_THAN = "<";
    /** String containing "|". */
    private final static String PIPE = "|";
    /** String containing ">". */
    private final static String GREATER_THAN = ">";
    /** Underscore character. */
    private final static char UNDERSCORE = '_';
    /** Minus character. */
    private final static char MINUS = '-';

    /** Map mapping user-readable string to according enum constant. */
    private final Map<String,Enum<?>> validToConstant = new LinkedHashMap<>();
    private final List<String> keys;
    /** Default value of this type. */
    private Object defaultValue;

    /**
     * Construct enum option type.
     * The enum class parameter must not be {@code null}.
     * 
     * @param enumType enum class for the choice of the option
     * @return enum option type
     */
    public <T extends Enum<T>> OptionTypeEnum(Class<T> enumType) {
        assert enumType != null;
        assert enumType.getEnumConstants() != null : enumType.getClass();
        if (enumType.getEnumConstants().length == 0) {
            this.defaultValue = null;
        } else {
            this.defaultValue = enumType.getEnumConstants()[0];
        }
        List<String> keys = new ArrayList<>();
        for (T enumConstant : enumType.getEnumConstants()) {
            String name = enumConstant.name();
            String readableName = name.toLowerCase().replace(UNDERSCORE, MINUS);
            validToConstant.put(readableName, enumConstant);
            keys.add(readableName);
        }
        this.keys = Collections.unmodifiableList(keys);
    }

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        if (validToConstant.containsKey(value)) {
            return validToConstant.get(value);
        } else {
            fail(ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            return null;
        }
    }

    @Override
    public String getInfo() {
        StringBuilder result = new StringBuilder();
        result.append(LESS_THAN);

        int entryNr = 0;
        for (String key : this.validToConstant.keySet()) {            
            result.append(key);
            if (entryNr + 1 < validToConstant.size()) {
                result.append(PIPE);
            }
            entryNr++;
        }
        result.append(GREATER_THAN);
        return result.toString();
    }    

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public String unparse(Object value) {
        assert value != null;
        assert value instanceof Enum;
        return value.toString().toLowerCase().replace(UNDERSCORE, MINUS);
    }

    @Override
    public Object getDefault() {
        return defaultValue;
    }

    public List<String> getKeys() {
        return keys;
    }
}
