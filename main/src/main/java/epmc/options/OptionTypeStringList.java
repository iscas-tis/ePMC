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

import java.util.ArrayList;
import java.util.List;

/**
 * Option type parsing lists of strings.
 * The strings parsed are separated by commas. Options of this type may appear
 * several type on the command line, in which case the lists are concatenated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeStringList implements OptionType {
    /** String for marking unchecked annotation. */
    private final static String UNCHECKED = "unchecked";
    /** String containing ",". */
    private final static String COMMA = ",";
    /** String containing "<". */
    private final static String LESS_THAN = "<";
    /** String containing ">". */
    private final static String GREATER_THAN = ">";
    /** String containing "*". */
    private final static String STAR = "*";

    /** Entry name used in {@link #getInfo()} method. */
    private final String entryName;

    /**
     * Get string list option type.
     * The entry name parameter must not be {@code null}.
     * 
     * @param entryName name of entries of the string list
     * @return string list option type
     */
    public OptionTypeStringList(String entryName) {
        assert entryName != null;
        this.entryName = entryName;
    }

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        List<String> result;
        if (prevValue == null) {
            result = new ArrayList<>();
        } else {
            @SuppressWarnings(UNCHECKED)
            List<String> casted = (List<String>) prevValue;
            result = casted;
        }
        String[] split = value.split(COMMA);
        if (split.length == 1 && split[0].length() == 0) {
            return result;
        }
        for (String c : split) {
            c = c.trim();
            result.add(c);
        }
        return result;
    }

    @Override
    public String getInfo() {
        StringBuilder result = new StringBuilder();
        result.append(LESS_THAN);
        result.append(entryName);
        result.append(GREATER_THAN + STAR);
        return result.toString();
    }

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public String unparse(Object value) {
        assert value != null;
        assert value instanceof List;
        Iterable<?> valueList = (Iterable<?>) value;
        for (Object object : valueList) {
            assert object instanceof String;
        }
        StringBuffer result = new StringBuffer();
        for (Object object : valueList) {
            String string = (String) object;
            result.append(string);
            result.append(COMMA);
        }
        if (result.length() > 0) {
            result.delete(result.length() - 1, result.length());
        }
        return result.toString();
    }

    @Override
    public Object getDefault() {
        return new ArrayList<>();
    }
}
