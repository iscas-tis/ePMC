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

package epmc.value;

import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class UtilValue {
    private final static String LBRACK = "[";
    private final static String RBRACK = "]";
    private final static String COMMA = ",";
    private final static String UNCHECKED = "unchecked";
    
    // TODO get rid of this method
    public static Type algebraicResultType(Type[] types) {
        Type upper = upper(types);
        Type result;
        if (TypeAlgebra.isAlgebra(upper)) {
            result = upper;
        } else if (TypeArray.isArray(upper)) {
            // TODO dimensions check
            result = upper;
        } else {
            return null;
        }
        return result;
    }

    // TODO get rid of this method
    public static Type upper(Type... types) {
        Type upper = types[0];
        for (Type type : types) {
            if (upper != null) {
                upper = upper(upper, type);
            }
        }
        /* Maintaining type bounds will lead to problems, for instance for
         * optimised array generation. Type bounds should only be specified
         * explicitly e.g. for Values which are contained in explorer nodes.
         * */
        if (TypeInteger.isIntegerWithBounds(upper)) {
            return TypeInteger.get();
        }
        return upper;
    }

    public static <T extends Value, U extends Type> T newValue(U type, String valueString) {
        @SuppressWarnings(UNCHECKED)
        T value = (T) type.newValue();
        ValueSetString.asValueSetString(value).set(valueString);
        return value;
    }

    public static <T extends ValueAlgebra, U extends TypeAlgebra> T newValue(U type, int valueInt) {
        @SuppressWarnings(UNCHECKED)
        T value = (T) type.newValue();
        value.set(valueInt);
        return value;
    }

    public static <T extends ValueArray, U extends TypeArray> T newArray(U type, int size) {
        assert type != null;
        assert size >= 0;
        @SuppressWarnings(UNCHECKED)
        T value = (T) type.newValue();
        value.setSize(size);
        return value;
    }

    // TODO get rid of this method
    @SuppressWarnings(UNCHECKED)
    public static <T extends Type> T upper(T a, T b) {
        assert a != null;
        assert b != null;
        T upper = null;
        if (TypeInteger.isInteger(a) && TypeInteger.isInteger(b)) {
            int lowerBound = Math.min(TypeInteger.asInteger(a).getLowerInt(),
                    TypeInteger.asInteger(b).getLowerInt());
            int upperBound = Math.max(TypeInteger.asInteger(a).getUpperInt(),
                    TypeInteger.asInteger(b).getUpperInt());
            upper = (T) new TypeInteger(lowerBound, upperBound);
        } else {
            if (a.canImport(b)) {
                upper = a;
            } else if (b.canImport(a)) {
                upper = b;
            } else {
                upper = null;
            }
        }
        if (upper != null) {
            upper = ContextValue.get().makeUnique(upper);
        }
        return upper;
    }

    public static <T extends Value> T clone(T value) {
        assert value != null;
        @SuppressWarnings(UNCHECKED)
        T clone = (T) value.getType().newValue();
        clone.set(value);
        return clone;
    }

    public static <T extends ValueArray> T ensureSize(T array, int size) {
        if (size <= array.size()) {
            return array;
        }
        int newSize = 1;
        while (newSize < size) {
            newSize <<= 1;
        }
        T result = newArray(array.getType(), newSize);
        Value entry = array.getType().getEntryType().newValue();
        for (int i = 0; i < array.size(); i++) {
            array.get(entry, i);
            result.set(entry, i);
        }
        return result;
    }

    public static String arrayToString(ValueArray array) {
        StringBuilder builder = new StringBuilder();
        Value entry = array.getType().getEntryType().newValue();
        builder.append(LBRACK);
        for (int entryNr = 0; entryNr < array.size(); entryNr++) {
            array.get(entry, entryNr);
            builder.append(entry);
            if (entryNr < array.size() - 1) {
                builder.append(COMMA);
            }
        }
        builder.append(RBRACK);
        return builder.toString();
    }

    public static double getDoubleOrInt(Value value) {
        assert value != null;
        assert ValueDouble.isDouble(value) || ValueInteger.isInteger(value)
        : value.getType();
        if (ValueDouble.isDouble(value)) {
            return ValueDouble.asDouble(value).getDouble();
        } else if (ValueInteger.isInteger(value)) {
            return ValueInteger.asInteger(value).getInt();
        } else {
            assert false;
            return Double.NaN;
        }
    }
    
    public static double getDouble(Value value) {
        assert value != null;
        assert ValueDouble.isDouble(value) : value.getType();
        return ValueDouble.asDouble(value).getDouble();
    }

    public static int getInt(Value value) {
        assert value != null;
        assert ValueInteger.isInteger(value) : value.getType();
        return ValueInteger.asInteger(value).getInt();
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilValue() {
    }
}
