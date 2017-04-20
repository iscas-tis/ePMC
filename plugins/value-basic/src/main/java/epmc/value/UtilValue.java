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
import epmc.error.UtilError;
import epmc.util.Util;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class UtilValue {
	public final static String LOG = "2.71828182845904523536028";

    public static Type algebraicResultNonIntegerType(Operator op, Type[] types) {
        Type upper = upper(types);
        if (allTypesKnown(types) && upper == null) {
            return null;
        } else {
            Type result;
            if (!TypeUnknown.isUnknown(upper) && TypeAlgebra.isAlgebra(upper)) {
                result = upper;
            } else if (TypeArray.isArray(upper)) {
                // TODO dimensions check
                result = upper;
            } else {
                return null;
            }
            if (TypeInteger.isInteger(result)) {
                result = TypeReal.get(result.getContext());
            }
            return result;
        }
    }

    public static Type algebraicResultType(Operator op, Type[] types) {
        Type upper = upper(types);
        if (allTypesKnown(types) && upper == null) {
            return null;
        } else {
            Type result;
            if (!TypeUnknown.isUnknown(upper) && TypeAlgebra.isAlgebra(upper)) {
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
            return TypeInteger.get(types[0].getContext());
        }
        return upper;
    }

    public static Type booleanResultType(Operator operator,
            Type[] types) {
        for (Type type : types) {
            if (type == null || !TypeUnknown.isUnknown(type) && !TypeBoolean.isBoolean(type)) {
                return null;
            }
        }
        Type result = TypeBoolean.get(operator.getContext());
        return result;
    }

    public static boolean allTypesKnown(Type... types) {
        boolean allTypesKnown = true;
        for (Type type : types) {
            if (TypeUnknown.isUnknown(type)) {
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

    public static <T extends ValueArray, U extends TypeArray> T newArray(U type, int valueInt) {
    	assert type != null;
        @SuppressWarnings("unchecked")
		T value = (T) type.newValue();
        value.setSize(valueInt);
        return value;
    }
    
    public static void failUnsupported(Value value) throws EPMCException {
        assert value != null;
        String operation = Thread.currentThread().getStackTrace()[2].getMethodName();
        Class<?> clazz = null;
        String identifier = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            try {
                clazz = Class.forName(stackTrace[i].getClassName(), true,
                        Thread.currentThread().getContextClassLoader());
                if (Operator.class.isAssignableFrom(clazz)) {
                    Operator operator = (Operator) Util.getInstance(clazz);
                    identifier = operator.getIdentifier();
                    break;
                }

            } catch (ClassNotFoundException e) {
            }
        }
        if (identifier != null) {
            operation = identifier;
        }
        UtilError.fail(ProblemsValueBasic.VALUES_UNSUPPORTED_OPERATION,
                operation, value.getType());
    }

    public static Type upper(Type a, Type b) {
    	assert a != null;
        assert b != null;
        assert a.getContext() == b.getContext();
        Type upper = null;
        if (TypeUnknown.isUnknown(a)) {
            upper = a;
        } else if (TypeUnknown.isUnknown(b)) {
            upper = b;
        } else if (TypeInteger.isInteger(a) && TypeInteger.isInteger(b)) {
            int lowerBound = Math.min(TypeInteger.asInteger(a).getLowerInt(),
            		TypeInteger.asInteger(b).getLowerInt());
            int upperBound = Math.max(TypeInteger.asInteger(a).getUpperInt(),
            		TypeInteger.asInteger(b).getUpperInt());
            upper = new TypeInteger(a.getContext(), lowerBound, upperBound);
        } else {
            if (a.canImport(b)) {
                upper = a;
            } else if (b.canImport(a)) {
                upper = b;
            } else {
                upper = TypeUnknown.get(a.getContext());
            }
        }
        if (upper != null) {
            upper = a.getContext().makeUnique(upper);
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
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
	private UtilValue() {
	}
}
