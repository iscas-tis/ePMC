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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

/**
 * Option type allowing to choose a value by an identifier.
 * The option type is constructed using a {@link Map} from {@link String} to
 * a specified class. The parser then allows to parse any {@link String} from
 * the {@link Map#entrySet()} of the map provided, and returns the value to
 * which the string is mapped to.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeMap<V> implements OptionType {
    /** Empty string. */
    private final static String EMPTY = "";
    /** String containing "<". */
    private final static String LESS_THAN = "<";
    /** String containing ">". */
    private final static String GREATER_THAN = ">";
    /** String containing "|". */
    private final static String PIPE = "|";

    /** Choices to choose from. */
    private final Map<String, V> map;
    private final Set<String> keys;

    /**
     * Get map option type.
     * The map parameter may not be {@code null} or contain {@code null} keys
     * or values.
     * 
     * @param map map to obtain choices from
     * @return map option type
     */
    public OptionTypeMap(Map<String,V> map) {
        assert map != null;
        for (Entry<String, V> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        this.map = map;
        this.keys = Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        if (map.containsKey(value)) {
            return map.get(value);
        } else {
            fail(ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, value);
            return null;
        }
    }

    @Override
    public String getInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(LESS_THAN);
        Set<String> keys = map.keySet();
        int keyNr = 0;
        if (keys == null) {
            return EMPTY;
        }
        for (String key : keys) {
            builder.append(key);
            if (keyNr < keys.size() - 1) {
                builder.append(PIPE);
            }
            keyNr++;
        }
        builder.append(GREATER_THAN);
        return builder.toString();
    }

    @Override
    public Object getDefault() {
        Collection<V> values = map.values();
        if (values == null || values.size() == 0) {
            return null;
        }
        return values.iterator().next();
    }

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public String unparse(Object value) {
        for (Entry<String, V> entry : map.entrySet()) {
            if (entry.getValue() == value) {
                return entry.getKey();
            }
            if (entry.getKey().equals(value)) {
                return entry.getKey();
            }
        }
        assert false : value + " " + map;
        return null;
    }

    /**
     * Add an entry to the map of this type.
     * None of the parameters may be {@code null}.
     * 
     * @param key key of entry to add
     * @param value value of entry to add
     */
    public void put(String key, V value) {
        assert key != null;
        assert value != null;
        map.put(key, value);
    }

    public Set<String> getKeys() {
        return keys;
    }
}
