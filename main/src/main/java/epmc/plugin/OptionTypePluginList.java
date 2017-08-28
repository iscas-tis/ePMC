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

package epmc.plugin;

import java.util.ArrayList;
import java.util.List;

import epmc.options.OptionType;

/**
 * Options type for plugin lists.
 * The option type can parse lists of plugins in which the different plugins are
 * separated by commas. Options of this option type can appear several times on
 * the command line, in which case the lists parsed so far are concatenated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypePluginList implements OptionType {
    /** Empty string. */
    private final static String EMPTY = "";
    /** String containing ",". */
    private final static String COMMA = ",";
    /** String containing "unchecked". */
    private final static String UNCHECKED = "unchecked";
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<filename>(,<filename>)*";
    /** Plugin list option type. */
    private final static OptionTypePluginList INSTANCE = new OptionTypePluginList();

    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypePluginList#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypePluginList() {
    }

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        List<String> result;
        if (prevValue == null) {
            result = new ArrayList<>();
        } else {
            result = new ArrayList<>(uncheckedCast(prevValue));
        }
        value = value.trim();
        if (value.equals(EMPTY)) {
            return result;
        }
        String[] pairs = value.split(COMMA);
        for (String pair : pairs) {
            result.add(pair);
        }
        return result;
    }

    @SuppressWarnings(UNCHECKED)
    private List<String> uncheckedCast(Object prevValue) {
        return (List<String>) prevValue;
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
        return new ArrayList<String>();
    }

    /**
     * Get plugin list option type.
     * 
     * @return plugin list option type
     */
    public static OptionTypePluginList getInstance() {
        return INSTANCE;
    }
}
