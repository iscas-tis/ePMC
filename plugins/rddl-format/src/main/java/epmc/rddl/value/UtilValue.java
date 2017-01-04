package epmc.rddl.value;

import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

public final class UtilValue {
    public static String toString(Type type) {
        assert type != null;
        if (TypeBoolean.isBoolean(type)) {
            return "bool";
        } else if (TypeInteger.isInteger(type)) {
            return "int";
        } else if (TypeReal.isReal(type)) {
            return "real";
        } else if (type instanceof TypeRDDLEnum) {
            TypeRDDLEnum typeEnum = (TypeRDDLEnum) type;
            return typeEnum.getName();
        } else if (type instanceof TypeRDDLObject) {
            TypeRDDLObject typeObject = (TypeRDDLObject) type;
            return typeObject.getName();
        } else {
            assert false : type.toString();
            return null;
        }
    }
    
    public static String toString(Value value) {
        assert value != null;
        if (ValueReal.isReal(value) && !ValueInteger.isInteger(value)) {
            String result = value.toString();
            if (!result.contains(".")) {
                result = result + ".0";
            }
            return result;
        } else {
            return value.toString();
        }
    }
    
    private UtilValue() {
    }
}
