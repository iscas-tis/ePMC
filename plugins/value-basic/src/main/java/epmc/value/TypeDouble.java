package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class TypeDouble implements TypeWeight, TypeWeightTransition, TypeReal, TypeBounded, TypeHasNativeArray, TypeNumBitsKnown {
    public static boolean isDouble(Type type) {
    	return type instanceof TypeDouble;
    }

    public static boolean isDouble(Value value) {
    	return isDouble(value.getType());
    }

    private final static String DOUBLE = "double";
    
    private final ContextValue context;
    private final ValueDouble valueOne = new ValueDouble(this, 1.0);
    private final ValueDouble valueZero = new ValueDouble(this, 0.0);
    private final ValueDouble valuePosInf = new ValueDouble(this, Double.POSITIVE_INFINITY);
    private final ValueDouble valueNegInf = new ValueDouble(this, Double.NEGATIVE_INFINITY);
    private final ValueDouble valueUnderflow = new ValueDouble(this, Double.MIN_NORMAL);
    private final ValueDouble valueOverflow = new ValueDouble(this, Double.MAX_VALUE);
    private final ValueDouble lower;
    private final ValueDouble upper;

    public TypeDouble(ContextValue context, ValueDouble lower, ValueDouble upper) {
        assert context != null;
        this.context = context;
        valueOne.setImmutable();
        valueZero.setImmutable();
        valuePosInf.setImmutable();
        valueNegInf.setImmutable();
        valueUnderflow.setImmutable();
        valueOverflow.setImmutable();
        this.lower = lower == null ? null : UtilValue.clone(lower);
        this.upper = upper == null ? null : UtilValue.clone(upper);
        if (this.lower != null) {
            this.lower.setImmutable();
        }
        if (this.upper != null) {
            this.upper.setImmutable();
        }
    }
    
    @Override
    public boolean canImport(Type a) {
        assert a != null;
        return a instanceof TypeDouble || a instanceof TypeInteger;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DOUBLE);
        return builder.toString();
    }

    @Override
    public ValueDouble newValue() {
        return new ValueDouble(this);
    }
    
    @Override
    public ValueDouble getZero() {
        return valueZero;
    }

    @Override
    public ValueDouble getOne() {
        return valueOne;
    }

    @Override
    public ValueDouble getUnderflow() {
        return valueUnderflow;
    }

    @Override
    public ValueDouble getOverflow() {
        return valueOverflow;
    }

    @Override
    public ValueDouble getPosInf() {
        return valuePosInf;
    }

    @Override
    public ValueDouble getNegInf() {
        return valueNegInf;
    }
    
    @Override
    public int getNumBits() {
        return Double.SIZE;
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
    public ContextValue getContext() {
        return context;
    }
    
    @Override
    public TypeArrayDoubleNative getTypeArrayNative() {
        TypeArrayDoubleNative arrayType = new TypeArrayDoubleNative(this);
        return getContext().makeUnique(arrayType);
    }
    
    @Override
    public ValueDouble getLower() {
        return lower;
    }
    
    @Override
    public ValueDouble getUpper() {
        return upper;
    }
    
    @Override
    public TypeArrayDouble getTypeArray() {
        return context.makeUnique(new TypeArrayDouble(this));
    }
}
