package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class TypeInterval implements TypeWeightTransition, TypeAlgebra {
	public static boolean isInterval(Type type) {
		return type instanceof TypeInterval;
	}
	
	public static TypeInterval asInterval(Type type) {
		if (type instanceof TypeInterval) {
			return (TypeInterval) type;
		} else {
			return null;
		}
	}
	
    private final ContextValue context;
    private final ValueInterval one;
    private final ValueInterval zero;
    private final ValueInterval posInf;

    public static TypeInterval get(ContextValue context) {
        assert context != null;
        return context.getType(TypeInterval.class);
    }
    
    public static void set(TypeInterval type) {
        assert type != null;
        ContextValue context = type.getContext();
        context.setType(TypeInterval.class, context.makeUnique(type));
    }
    
    public TypeInterval(ContextValue context) {
        assert context != null;
        this.context = context;
        TypeReal typeReal = TypeReal.get(context);
        one = new ValueInterval(this, typeReal.getOne(), typeReal.getOne());
        zero = new ValueInterval(this, typeReal.getZero(), typeReal.getZero());
        posInf = new ValueInterval(this, typeReal.getPosInf(), typeReal.getPosInf());
    }
    
    @Override
    public boolean canImport(Type type) {
        assert type != null;
        if (type instanceof TypeInterval) {
            return true;
        }
        if (type instanceof TypeInteger) {
            return true;
        }
        if (TypeReal.isReal(type)) {
            return true;
        }
        return false;
    }

    @Override
    public ValueInterval newValue() {
        return new ValueInterval(this);
    }
    
    public ValueInterval newValue(Value lower, Value upper) {
        assert lower != null;
        assert upper != null;
        assert lower.getType().getContext() == getContext();
        assert upper.getType().getContext() == getContext();
        assert ValueReal.isReal(lower) || ValueInteger.isInteger(lower);
        assert ValueReal.isReal(upper) || ValueInteger.isInteger(upper);
        ValueInterval result = newValue();
        result.getIntervalLower().set(lower);
        result.getIntervalUpper().set(upper);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("interval");
        return builder.toString();
    }
    
    @Override
    public ContextValue getContext() {
        return context;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type) obj;
        if (this.getContext() != other.getContext()) {
            return false;
        }
        if (!canImport(other) || !other.canImport(this)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public ValueInterval getOne() {
        return one;
    }
    
    @Override
    public ValueInterval getZero() {
        return zero;
    }
    
    public TypeReal getEntryType() {
        return TypeReal.get(getContext());
    }
    
    public ValueInterval getPosInf() {
        return posInf;
    }
    
    @Override
    public TypeArrayInterval getTypeArray() {
        return context.makeUnique(new TypeArrayInterval(this));
    }
}
