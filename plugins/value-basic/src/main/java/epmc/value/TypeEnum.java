package epmc.value;

import java.util.Arrays;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;

public final class TypeEnum implements TypeEnumerable, TypeNumBitsKnown {
    public static TypeEnum get(ContextValue context, Class<? extends Enum<?>> enumClass) {
        return context.makeUnique(new TypeEnum(context, enumClass));
    }
    
    public static TypeEnum asEnum(Type type) {
    	if (type instanceof TypeEnum) {
    		return (TypeEnum) type;
    	} else {
    		return null;
    	}
    }

    public static boolean isEnum(Type type) {
    	return type instanceof TypeEnum;
    }
    
    private final ContextValue context;
    private final Class<? extends Enum<?>> enumClass;
    private final int numBits;
    private final int numConstants;

    TypeEnum(ContextValue context, Class<? extends Enum<?>> enumClass) {
        assert context != null;
        this.context = context;
        assert enumClass != null;
        this.enumClass = enumClass;
        numConstants = enumClass.getEnumConstants().length;
        numBits = Integer.SIZE - Integer.numberOfLeadingZeros(numConstants - 1);
    }
    
    
    public Class<? extends Enum<?>> getEnumClass() {
        return enumClass;
    }
    
    @Override
    public ValueEnum newValue() {
        return new ValueEnum(this);
    }

    public Value newValue(Enum<?> enumConst) {
        assert enumConst != null;
        Value value = newValue();
        ValueEnum.asEnum(value).set(enumConst);
        return value;
    }

    @Override
    public boolean canImport(Type type) {
        assert type != null;
        if (!TypeEnum.isEnum(type)) {
            return false;
        }
        return enumClass == asEnum(type).getEnumClass();
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        TypeEnum other = (TypeEnum) obj;
        if (this.getContext() != other.getContext()) {
            return false;
        }
        if (!canImport(other) || !other.canImport(this)) {
            return false;
        }
        return this.enumClass == other.enumClass;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = enumClass.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("enum(");
        builder.append(Arrays.toString(enumClass.getEnumConstants()));
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public int getNumBits() {
        return numBits;
    };
    
    @Override
    public int getNumValues() {
        return numConstants;
    }
    
    @Override
    public ContextValue getContext() {
        return context;
    }
    
    @Override
    public TypeArray getTypeArray() {
        return context.makeUnique(new TypeArrayEnum(this));
    }
}
