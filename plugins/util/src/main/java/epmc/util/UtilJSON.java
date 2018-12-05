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

package epmc.util;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParsingException;
import javax.json.stream.JsonParser.Event;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.error.Problem;

// TODO documentation
// TODO reorder methods according to category (e.g. ensure, checked conversion)
// TODO move out of main part

/**
 * Static auxiliary methods for JSON parsing and generating.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilJSON {
    @FunctionalInterface
    public interface ToValueInterface <V> {
        V map(String input);
    }

    /** String with arbitrary content. */
    private final static String ARBITRARY = "arbitrary";
    /** String with empty content. */
    private final static String EMPTY = "";
    /** Textual representation of boolean value &quot;true&quot;. */
    private final static String TRUE = "true";
    /** Textual representation of boolean value &quot;false&quot;. */
    private final static String FALSE = "false";
    /** Regular expression string for valid identifiers. */
    private final static String IDENTIFIER_PATTERN = "[_a-zA-Z][_a-zA-Z0-9]*";
    /** JSON representation of boolean value &quot;true&quot;. */
    private final static JsonValue TRUE_VALUE;
    /** JSON representation of boolean value &quot;false&quot;. */
    private final static JsonValue FALSE_VALUE;
    
    static {
        JsonObjectBuilder trueValue = Json.createObjectBuilder();
        trueValue.add(ARBITRARY, true);
        TRUE_VALUE = trueValue.build().get(ARBITRARY);
        JsonObjectBuilder falseValue = Json.createObjectBuilder();
        falseValue.add(ARBITRARY, false);
        FALSE_VALUE = falseValue.build().get(ARBITRARY);
    }

    /* ensure methods */

    public static void ensureEquals(JsonObject object, String key, String compare) {
        assert object != null;
        assert key != null;
        assert compare != null;
        UtilJSON.ensurePresent(object.get(key), key);
        UtilJSON.ensureString(object.get(key));
        UtilJSON.ensureEquals(key, object.getString(key), compare);
    }

    /**
     * Ensure that given value from JSON is equal to prespecified one.
     * 
     * @param key name of key 
     * @param value actual value
     * @param compare value to compare against
     */
    public static void ensureEquals(String key, String value, String compare) {
        assert key != null;
        assert value != null;
        assert compare != null;
        ensure(value.equals(compare), ProblemsJSON.JSON_NOT_EQUALS, key, value, compare);
    }

    /**
     * Require that there is only one element with given name.
     * None of the parameters may be {@code null} or contain {@code null}
     * entries.
     * 
     * @param name name to check
     * @param map map to check name
     */
    public static <K,V> void ensureUnique(K name, Map<K, V> map) {
        assert name != null;
        assert map != null;
        for (Entry<K, V> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        ensure(!map.containsKey(name), ProblemsJSON.JSON_ELEMENT_UNIQUE);
    }

    public static void ensureInteger(JsonValue value) {
        assert value != null;
        ensureNumber(value);
        ensure(((JsonNumber) value).isIntegral(), ProblemsJSON.JSON_NOT_VALUE_INTEGER);
    }

    public static void ensureBoolean(JsonValue value) {
        assert value != null;

        ensure(value.getValueType() == ValueType.TRUE ||
                value.getValueType() == ValueType.FALSE,
                ProblemsJSON.JSON_NOT_VALUE_BOOLEAN);
    }

    public static <K> void ensureUnique(K name, Set<K> set) {
        assert name != null;
        assert set != null;
        for (K element : set) {
            assert element != null;
        }
        ensure(!set.contains(name), ProblemsJSON.JSON_ELEMENT_UNIQUE);
    }

    public static void ensureKeyIs(JsonObject object, String key, String expected) {
        assert object != null;
        assert key != null;
        assert expected != null;
        String actual = getString(object, key);
        ensureEquals(key, actual, expected);
    }

    /**
     * Require that a JSON element is present.
     * 
     * @param condition condition specifying presence of element
     * @param element name of element
     */
    public static void ensurePresent(boolean condition, String element)
    {
        ensure(condition, ProblemsJSON.JSON_ELEMENT_REQUIRED, element);
    }

    public static void ensurePresent(Object what, String element) {
        ensurePresent(what != null, element);
    }

    public static void ensureObject(JsonValue value) {
        assert value != null;
        ensure(value.getValueType() == ValueType.OBJECT, ProblemsJSON.JSON_VALUE_OBJECT);
    }

    public static void ensureArray(JsonValue value) {
        assert value != null;
        ensure(value.getValueType() == ValueType.ARRAY, ProblemsJSON.JSON_VALUE_ARRAY);
    }

    public static void ensureOnlyOnce(Object object) {
        ensureOnlyOnce(object == null);
    }

    /**
     * Ensure that given JSON object only occurs once in given context.
     * The fact that the object only occurs once is specifyed by the given
     * condition. If the condition is violated, an according
     * {@link EPMCException} is thrown, which also include the position where
     * the duplicate element occurs.
     * 
     * @param condition condition specifying that SON object only occurs once
     */
    public static void ensureOnlyOnce(boolean condition) {
        ensure(condition, ProblemsJSON.JSON_ELEMENT_ONLY_ONCE);
    }

    public static void ensureString(JsonValue value) {
        assert value != null;
        ensure(value.getValueType() == ValueType.STRING, ProblemsJSON.JSON_NOT_VALUE_STRING);
    }

    public static void ensureString(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        UtilJSON.ensurePresent(object.get(name), name);
        UtilJSON.ensureString(object.get(name));
    }

    public static void ensureStringOrObject(JsonValue value) {
        assert value != null;
        ensure(value.getValueType() == ValueType.STRING || value.getValueType() == ValueType.OBJECT, ProblemsJSON.JSON_NOT_VALUE_STRING_OR_OBJECT);
    }

    /**
     * Ensures that given event is {@link Event#VALUE_NUMBER}.
     * If this is not the case, an {@link EPMCException} is thrown.
     * The parameter may not be {@code null}.
     * 
     * @param value event for which to check whether it is a value number
     */
    public static void ensureNumber(JsonValue value) {
        assert value != null;
        ensure(value.getValueType() == ValueType.NUMBER, ProblemsJSON.JSON_NOT_VALUE_NUMBER);
    }

    /**
     * Ensure that given string is contained in a set of strings.
     * If this is not the case, an according {@link EPMCException} will be
     * thrown. The exception generated will contain the current position of the
     * JSON parser. None of the parameters may be {@code null}.
     * 
     * @param composition string for which to check whether it is contained
     * @param compositions possible choices of string
     */
    public static void ensureOneOf(String composition, Set<String> compositions) {
        assert composition != null;
        assert compositions != null;
        for (String choice : compositions) {
            assert choice != null;
        }
        ensureOneOf(composition, compositions, ProblemsJSON.JSON_VALUE_ONE_OF);
    }

    public static void ensureOneOf(String composition, Set<String> compositions, Problem problem) {
        assert composition != null;
        assert compositions != null;
        for (String choice : compositions) {
            assert choice != null;
        }
        ensure(compositions.contains(composition),
                problem, composition, compositions);
    }

    public static void ensureIdentifier(String identifier) {
        assert identifier != null;
        ensure(identifier.matches(IDENTIFIER_PATTERN), ProblemsJSON.JSON_INVALID_IDENTIFIER, identifier);
    }

    public static void ensureMatches(String string, String pattern) {
        assert string != null;
        assert pattern != null;
        ensure(string.matches(pattern), ProblemsJSON.JSON_DOES_NOT_MATCH, string, pattern);
    }

    /* conversion methods */

    public static JsonObject toObject(JsonValue value) {
        assert value != null;
        UtilJSON.ensureObject(value);
        return (JsonObject) value;
    }

    public static JsonObject toObjectString(JsonValue value) {
        assert value != null;
        UtilJSON.ensureObject(value);
        JsonObject object = (JsonObject) value;
        for (String key : object.keySet()) {
            UtilJSON.ensureString(object, key);
        }
        return object;
    }

    public static JsonArray toArray(JsonValue value) {
        assert value != null;
        UtilJSON.ensureArray(value);
        return (JsonArray) value;
    }

    public static JsonArray toArrayObject(JsonValue value) {
        assert value != null;
        JsonArray result = UtilJSON.toArray(value);
        for (JsonValue element : result) {
            UtilJSON.ensureObject(element);
        }
        return result;
    }

    public static JsonArray toArrayString(JsonValue value) {
        assert value != null;
        JsonArray result = UtilJSON.toArray(value);
        for (JsonValue element : result) {
            UtilJSON.ensureString(element);
        }
        return result;
    }

    public static JsonArray toArrayInteger(JsonValue value) {
        assert value != null;
        JsonArray result = UtilJSON.toArray(value);
        for (JsonValue element : result) {
            UtilJSON.ensureInteger(element);
        }
        return result;
    }

    public static String toOneOf(JsonObject object, String key, Set<String> set) {
        assert object != null;
        assert key != null;
        assert set != null;
        for (String string : set) {
            assert string != null;
        }
        UtilJSON.ensurePresent(object.get(key), key);
        UtilJSON.ensureString(object.get(key));
        UtilJSON.ensureOneOf(object.getString(key), set);
        return object.getString(key);
    }

    public static <V> V toOneOf(JsonObject object, String key, Map<String,V> map) {
        assert object != null;
        assert key != null;
        assert map != null;
        for (Entry<String, ?> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        UtilJSON.ensurePresent(object.get(key), key);
        UtilJSON.ensureString(object.get(key));
        UtilJSON.ensureOneOf(object.getString(key), map.keySet());
        return map.get(object.getString(key));
    }

    public static <V> V toOneOfOrNullFailInvalidType(JsonObject object, String key, Map<String,V> map) {
        assert object != null;
        assert key != null;
        assert map != null;
        for (Entry<String, ?> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        if (!object.containsKey(key)) {
            return null;
        }
        UtilJSON.ensureString(object.get(key));
        if (!map.containsKey(object.getString(key))) {
            return null;
        }
        UtilJSON.ensureOneOf(object.getString(key), map.keySet());
        return map.get(object.getString(key));
    }

    public static <V> V toOneOf(JsonObject object, String key, ToValueInterface<V> toValueInterface) {
        assert object != null;
        assert key != null;
        assert toValueInterface != null;
        UtilJSON.ensurePresent(object.get(key), key);
        UtilJSON.ensureString(object.get(key));
        V result = toValueInterface.map(object.getString(key));
        ensurePresent(result != null, key);
        return result;
    }

    public static <V> V toOneOf(JsonValue value, Map<String,V> map, Problem problem) {
        assert value != null;
        assert map != null;
        for (Entry<String, ?> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        UtilJSON.ensureString(value);
        UtilJSON.ensureOneOf(((JsonString) value).getString(), map.keySet(), problem);
        return map.get(((JsonString) value).getString());
    }

    public static <V> V toOneOf(JsonValue value, Map<String,V> map) {
        assert value != null;
        assert map != null;
        for (Entry<String, ?> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        return toOneOf(value, map, ProblemsJSON.JSON_VALUE_ONE_OF);
    }

    public static <V> Set<V> toSubsetOf(JsonValue value, Map<String,V> map) {
        assert value != null;
        assert map != null;
        for (Entry<String, ?> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        UtilJSON.ensureArray(value);
        JsonArray array = UtilJSON.toArrayString(value);
        Set<String> seen = new HashSet<>();
        Set<V> result = new LinkedHashSet<>();
        for (JsonString stringValue : array.getValuesAs(JsonString.class)) {
            String string = stringValue.getString();
            assert !seen.contains(string); // TODO
            V entry = toOneOf(stringValue, map);
            result.add(entry);
        }
        return result;
    }

    public static <V> Set<V> toSubsetOf(JsonObject object, String key, Map<String,V> map) {
        JsonArray array = UtilJSON.getArrayString(object, key);
        return toSubsetOf(array, map);
    }

    public static <V> Set<V> toSubsetOfOrNull(JsonObject object, String key, Map<String,V> map) {
        if (!object.containsKey(key)) {
            return null;
        }
        return toSubsetOf(object, key, map);
    }

    public static JsonString toStringValue(String string) {
        assert string != null;
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(ARBITRARY, string);
        return result.build().getJsonString(ARBITRARY);
    }

    public static JsonString toStringValue(Object object) {
        assert object != null;
        return toStringValue(object.toString());
    }

    public static JsonNumber toNumberValue(double object) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(ARBITRARY, object);
        return result.build().getJsonNumber(ARBITRARY);
    }

    public static JsonNumber toNumberValue(Object object) {
        assert object != null;
        JsonObjectBuilder result = Json.createObjectBuilder();
        BigDecimal value = null;
        if (object instanceof BigDecimal) {
            value = (BigDecimal) object;
        } else if (object instanceof Integer) {
            value = BigDecimal.valueOf((Integer) object);
        } else if (object instanceof Long) {
            value = BigDecimal.valueOf((Long) object);
        } else if (object instanceof Double) {
            value = BigDecimal.valueOf((Double) object);			
        } else {
            String string = object.toString().trim();
            value = new BigDecimal(string);
        }
        result.add(ARBITRARY, value);
        return result.build().getJsonNumber(ARBITRARY);
    }

    public static JsonNumber toIntegerValue(String string) {
        assert string != null;
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(ARBITRARY, new BigInteger(string));
        return result.build().getJsonNumber(ARBITRARY);
    }	

    public static JsonNumber toIntegerValue(int integer) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(ARBITRARY, integer);
        return result.build().getJsonNumber(ARBITRARY);
    }	

    public static JsonNumber toIntegerValue(Integer integer) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(ARBITRARY, integer);
        return result.build().getJsonNumber(ARBITRARY);
    }	

    public static JsonValue toBooleanValue(boolean value) {
        return value ? TRUE_VALUE : FALSE_VALUE;
    }

    public static JsonValue toBooleanValue(Object object) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        boolean value = false;
        if (object instanceof Boolean) {
            value = (Boolean) object;
        } else {
            String string = object.toString().trim().toLowerCase();
            switch (string) {
            case TRUE:
                value = true;
                break;
            case FALSE:
                value = false;
                break;
            default:
                assert false : object.toString();
            }
        }
        result.add(ARBITRARY, value);
        return result.build().get(ARBITRARY);
    }	

    public static String toIdentifierString(JsonValue value) {
        assert value != null;
        UtilJSON.ensureString(value);
        UtilJSON.ensureIdentifier(((JsonString) value).getString());
        return ((JsonString) value).getString();
    }

    public static String toString(JsonValue value) {
        assert value != null;
        UtilJSON.ensureString(value);
        return ((JsonString) value).getString();
    }


    /* checked get methods */

    public static JsonArray getArray(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        return toArray(object.get(key));
    }

    public static JsonArray getArrayOrNull(JsonObject object, String key) {
        try {
            return getArray(object, key);
        } catch (EPMCException e) {
            return null;
        }
    }

    public static JsonObject getObject(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        return toObject(object.get(key));
    }

    public static JsonArray getArrayObject(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        return toArrayObject(object.get(key));
    }

    public static JsonArray getArrayObjectOrNull(JsonObject object, String key) {
        try {
            return getArrayObject(object, key);
        } catch (EPMCException e) {
            return null;
        }
    }

    public static JsonArray getArrayObjectOrEmpty(JsonObject object, String key) {
        try {
            return getArrayObject(object, key);
        } catch (EPMCException e) {
            return Json.createArrayBuilder().build();
        }
    }

    public static JsonArray getArrayString(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        return toArrayString(object.get(key));
    }

    public static JsonArray getArrayStringOrNull(JsonObject object,
            String key) {
        assert object != null;
        assert key != null;
        if (object.get(key) == null) {
            return null;
        }
        return toArrayString(object.get(key));
    }

    public static JsonArray getArrayStringOrEmpty(JsonObject object,
            String key) {
        assert object != null;
        assert key != null;
        if (object.get(key) == null) {
            return Json.createArrayBuilder().build();
        }
        return toArrayString(object.get(key));
    }

    public static Set<String> getStringSetOrEmpty(JsonObject object,
            String key) {
        JsonArray array = getArrayStringOrEmpty(object, key);
        Set<String> result = new LinkedHashSet<>();
        for (JsonValue value : array) {
            JsonString jString = (JsonString) value;
            result.add(jString.getString());
        }
        return result;
    }

    public static JsonArray getArrayInteger(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        return toArrayInteger(object.get(key));
    }

    public static BigInteger[] getArrayBigInteger(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        UtilJSON.ensurePresent(object.get(key), key);
        JsonArray jsonArray = toArrayInteger(object.get(key));
        int numValues = jsonArray.size();
        BigInteger[] result = new BigInteger[numValues];
        for (int valueNr = 0; valueNr < numValues; valueNr++) {
            JsonNumber number = jsonArray.getJsonNumber(valueNr);
            result[valueNr] = number.bigIntegerValue();
        }
        return result;
    }

    public static String getString(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        ensurePresent(object.get(name), name);
        ensureString(object.get(name));
        return object.getString(name);
    }

    public static JsonValue get(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        ensurePresent(object.get(name), name);
        return object.get(name);
    }

    public static String getStringOrNull(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        try {
            return getString(object, name);
        } catch (EPMCException e) {
            return null;
        }
    }

    public static String getStringOrEmpty(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        try {
            return getString(object, name);
        } catch (EPMCException e) {
            return EMPTY;
        }
    }

    
    public static int getInteger(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        ensurePresent(object.get(name), name);
        ensureInteger(object.get(name));
        try {
            return object.getJsonNumber(name).intValueExact();
        } catch (ArithmeticException e) {
            fail(ProblemsJSON.JSON_NOT_VALUE_INTEGER_JAVA, e);
        }
        return object.getInt(name);
    }

    public static Integer getIntegerOrNullFailInvalidType(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        if (!object.containsKey(name)) {
            return null;
        }
        ensureInteger(object.get(name));
        try {
            return object.getJsonNumber(name).intValueExact();
        } catch (ArithmeticException e) {
            fail(ProblemsJSON.JSON_NOT_VALUE_INTEGER_JAVA, e);
        }
        return object.getInt(name);
    }

    public static Long getLongOrNull(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        if (!object.containsKey(name)) {
            return null;
        }
        JsonValue value = object.get(name);
        if (value.getValueType() != ValueType.NUMBER) {
            return null;
        }
        JsonNumber valueNumber = (JsonNumber) value;
        try {
            return valueNumber.longValueExact();
        } catch (ArithmeticException e) {
            return null;
        }
    }

    public static BigInteger getBigInteger(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        ensurePresent(object.get(name), name);
        ensureInteger(object.get(name));
        return object.getJsonNumber(name).bigIntegerValue();
    }

    public static BigInteger getBigIntegerOrNull(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        try {
            return getBigInteger(object, name);
        } catch (EPMCException e) {
            return null;
        }
    }

    public static boolean getBoolean(JsonObject object, String name) {
        assert object != null;
        assert name != null;
        ensurePresent(object.get(name), name);
        ensureBoolean(object.get(name));
        return object.getBoolean(name);
    }

    public static String getIdentifier(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        ensurePresent(object.get(key), key);
        ensureString(object.get(key));
        // TODO check again once final form of identifiers decided
        //		ensureIdentifier(object.getString(key));
        return object.getString(key);
    }

    public static String getMatch(JsonObject object, String key, String pattern) {
        assert object != null;
        assert key != null;
        ensurePresent(object.get(key), key);
        ensureString(object.get(key));
        ensureMatches(object.getString(key), pattern);
        return object.getString(key);
    }

    public static String getIdentifierOrNull(JsonObject object, String key) {
        assert object != null;
        assert key != null;
        try {
            return getIdentifier(object, key);
        } catch (EPMCException e) {
            return null;
        }
    }

    /* other methods */

    /**
     * Transform {@link JsonLocation} to a {@link Positional}.
     * The location parameter may not be {@code null}.
     * 
     * @param location JSON location to transform to positional
     * @return positional converted from JSOn location
     */
    public static Positional jsonToPositional(JsonLocation location) {
        assert location != null;
        return new Positional.Builder()
                .setLine(location.getLineNumber())
                .setColumn(location.getColumnNumber())
                .build();
    }

    public static JsonString writeString(String string) {
        assert string != null;
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(ARBITRARY, string);
        return result.build().getJsonString(ARBITRARY);
    }

    /**
     * Invert a map.
     * For this, in the map parameter of this function, for each value there
     * must be exactly one key which is mapped to this value. In the resulting
     * map, the value will be mapped to its former key. The map parameter may
     * not be {@code null} and may not contain {@code null} keys or values.
     * 
     * @param map map to inverse
     * @return inverted map
     */
    public static <K,V> Map<V,K> invertMap(Map<K,V> map) {
        assert map != null;
        Map<V,K> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : map.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
            assert !result.containsKey(entry.getValue()); // "exactly one key"
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    public static JsonStructure read(InputStream input) {
        assert input != null;
        try (JsonReader reader = Json.createReader(input)) {
            return reader.read();
        } catch (JsonParsingException e) {
            fail(ProblemsJSON.JSON_MALFORMED_JSON, e,
                    UtilJSON.jsonToPositional(e.getLocation()), e);
            return null;
        } catch (JsonException e) {
            fail(ProblemsJSON.JSON_CANNOT_PARSE, e);
            return null;        	
        }
    }

    public static JsonStructure read(String string) {
        assert string != null;
        ByteArrayInputStream input = new ByteArrayInputStream(string.getBytes());
        return read(input);
    }

    public static JsonValue getNull() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.addNull(ARBITRARY);
        return result.build().get(ARBITRARY);
    }

    public static JsonObjectBuilder addOptional(JsonObjectBuilder object, String key, JsonValue value) {
        assert object != null;
        assert key != null;
        if (value == null) {
            return object;
        }
        object.add(key, value);
        return object;
    }

    public static JsonObjectBuilder addOptional(JsonObjectBuilder object, String key, String value) {
        assert object != null;
        assert key != null;
        if (value == null) {
            return object;
        }
        object.add(key, value);
        return object;
    }

    public static JsonObjectBuilder addOptional(JsonObjectBuilder object, String key, Object value) {
        assert object != null;
        assert key != null;
        if (value == null) {
            return object;
        }
        object.add(key, value.toString());
        return object;
    }

    public static JsonArray toStringArray(Iterable<String> operand) {
        assert operand != null;
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (String string : operand) {
            builder.add(string);
        }
        return builder.build();
    }

    public static String prettyString(JsonValue value) {
        assert value != null;
        if (value instanceof JsonStructure) {
            Map<String, Object> properties = new LinkedHashMap<>(1);
            properties.put(JsonGenerator.PRETTY_PRINTING, true);
            StringWriter sw = new StringWriter();
            JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
            JsonWriter jsonWriter = writerFactory.createWriter(sw);
            jsonWriter.write((JsonStructure) value);
            jsonWriter.close();
            return sw.toString();
        } else {
            return value.toString();
        }
    }

    public static boolean isNull(JsonObject object, String entry) {
        assert object != null;
        assert entry != null;
        return object.get(entry).getValueType() == ValueType.NULL;
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilJSON() {
    }
}
