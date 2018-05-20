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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.util.SerializableKeyset;

// TODO finish description
// TODO check whether parsing should return values instead of string keys

/**
 * 
 * @author Ernst Moritz Hahn
 *
 * @param <V>
 */
public final class OptionTypeStringListSubsetDirect<V> implements OptionType {
    /** Empty string. */
    private final static String EMPTY = "";
    /** String for marking unchecked annotation. */
    private final static String UNCHECKED = "unchecked";
    /** String containing ",". */
    private final static String COMMA = ",";
    /** String containing "<". */
    private final static String LESS_THAN = "<";
    /** String containing "|". */
    private final static String PIPE = "|";
    /** String containing ">". */
    private final static String GREATER_THAN = ">";

    private Map<String,V> choices;
    private Map<V,String> reversed;

    /**
     * Get string list subset option type.
     * The map parameter may not be {@code null} or contain {@code null} keys
     * or values.
     * 
     * @param map map to obtain possible choices from
     * @return string list subset option type
     */
    public OptionTypeStringListSubsetDirect(Map<String,V> map) {
        assert map != null;
        for (Entry<String, V> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        this.choices = map;
        this.reversed = new LinkedHashMap<>();
        for (Entry<String, V> entry : map.entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }
    }

    // TODO check whether elements are actually map keys

    @Override
    public Object parse(String value, Object prevValue) {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        List<V> result;
        if (prevValue == null) {
            result = new ArrayList<>();
        } else {
            assert prevValue instanceof List;
            @SuppressWarnings(UNCHECKED)
            List<V> casted = (List<V>) prevValue;
            for (Object entry : casted) {
//                assert entry instanceof V;
            }
            result = casted;
        }
        value = value.trim();
        if (value.equals(EMPTY)) {
            return result;
        }
        String[] split = value.split(COMMA);
        for (String c : split) {
            c = c.trim();
            V v = choices.get(c);
            assert v != null; // TODO make user exception
            result.add(v);
        }
        return result;
    }


    @Override
    public String getInfo() {
        StringBuilder result = new StringBuilder();
        result.append(LESS_THAN);
        int entryNr = 0;
        for (String string : choices.keySet()) {
            result.append(string);
            if (entryNr + 1 < choices.size()) {
                result.append(PIPE);
            }
            entryNr++;
        }
        result.append(GREATER_THAN);
        return result.toString();
    }

    @Override
    public Object getDefault() {
        return new SerializableKeyset<>(reversed);
    }

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public String unparse(Object value) {
        assert value != null;
        assert value instanceof Iterable<?> : value.getClass();
        Iterable<V> valueList = (Iterable<V>) value;
        for (Object object : valueList) {
//            assert object instanceof String;
        }
        StringBuffer result = new StringBuffer();
        for (Object object : valueList) {
            String string = reversed.get(object);
            result.append(string);
            result.append(COMMA);
        }
        if (result.length() > 0) {
            result.delete(result.length() - 1, result.length());
        }
        return result.toString();
    }
}
