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

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class UtilValue {
	public final static String LOG = "2.71828182845904523536028";

    public static Type algebraicResultNonIntegerType(Type[] types) {
        Type upper = upper(types);
        if (allTypesKnown(types) && upper == null) {
            return null;
        } else {
            Type result;
            if (TypeAlgebra.isAlgebra(upper)) {
                result = upper;
            } else if (TypeArray.isArray(upper)) {
                // TODO dimensions check
                result = upper;
            } else {
                return null;
            }
            if (TypeInteger.isInteger(result)) {
                result = TypeReal.get();
            }
            return result;
        }
    }

    public static Type algebraicResultType(Type[] types) {
        Type upper = upper(types);
        if (allTypesKnown(types) && upper == null) {
            return null;
        } else {
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
    }

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

    public static Type booleanResultType(Type[] types) {
        for (Type type : types) {
            if (type == null || !TypeBoolean.isBoolean(type)) {
                return null;
            }
        }
        Type result = TypeBoolean.get();
        return result;
    }

    public static boolean allTypesKnown(Type... types) {
        boolean allTypesKnown = true;
        for (Type type : types) {
            if (type == null) {
                allTypesKnown = false;
            }
        }
        return allTypesKnown;
    }
    
    public static <T extends Value, U extends Type> T newValue(U type, String valueString) throws EPMCException {
        @SuppressWarnings("unchecked")
		T value = (T) type.newValue();
        value.set(valueString);
        return value;
    }

    public static <T extends ValueReal, U extends TypeReal> T newValueDouble(U type, double valueDouble) throws EPMCException {
        @SuppressWarnings("unchecked")
		T value = (T) type.newValue();
        value.set(valueDouble);
        return value;
    }

    public static <T extends ValueAlgebra, U extends TypeAlgebra> T newValue(U type, int valueInt) {
        @SuppressWarnings("unchecked")
		T value = (T) type.newValue();
        value.set(valueInt);
        return value;
    }

    public static <T extends ValueArray, U extends TypeArray> T newArray(U type, int size) {
    	assert type != null;
    	assert size >= 0;
        @SuppressWarnings("unchecked")
		T value = (T) type.newValue();
        value.setSize(size);
        return value;
    }
    
    @SuppressWarnings("unchecked")
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
    	@SuppressWarnings("unchecked")
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

    public static boolean arrayEquals(ValueArray array, Object obj) {
        if (!(obj instanceof ValueArray)) {
            return false;
        }
        ValueArray other = (ValueArray) obj;
        if (array.size() != other.size()) {
            return false;
        }
        int totalSize = array.size();
        Value entryAccThis = array.getType().getEntryType().newValue();
        Value entryAccOther = array.getType().getEntryType().newValue();
        for (int entry = 0; entry < totalSize; entry++) {
            try {
            	array.get(entryAccThis, entry);
                other.get(entryAccOther, entry);
                if (!entryAccThis.isEq(entryAccOther)) {
                    return false;
                }
            } catch (EPMCException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    
    public static String arrayToString(ValueArray array) {
        StringBuilder builder = new StringBuilder();
        Value entry = array.getType().getEntryType().newValue();
        builder.append("[");
        for (int entryNr = 0; entryNr < array.size(); entryNr++) {
        	array.get(entry, entryNr);
        	builder.append(entry);
        	if (entryNr < array.size() - 1) {
        		builder.append(",");
        	}
        }
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
	private UtilValue() {
	}
}
