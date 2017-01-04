package epmc.qmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.ValueReal;

final class TypeComplex implements TypeAlgebra, TypeNumBitsKnown {
    private static final long serialVersionUID = 1L;
    private final TypeReal typeReal;
    private final ValueComplex valueZero;
    private final ValueComplex valueOne;
    private final int numBits;
    private ContextValue context;
    
    TypeComplex(ContextValue context, TypeReal typeReal) {
        this.context = context;
        assert typeReal != null;
        this.typeReal = typeReal;
        this.valueZero = newValue();
        this.valueOne = newValue();
        this.valueOne.set(1);
        int numRealBits = TypeNumBitsKnown.getNumBits(typeReal);
        if (numRealBits == TypeNumBitsKnown.UNKNOWN) {
        	this.numBits = TypeNumBitsKnown.UNKNOWN;
        } else {
        	this.numBits = 2 * numRealBits;
        }
        this.valueZero.setImmutable();
        this.valueOne.setImmutable();
    }
    
    TypeComplex(ContextValue context) {
        this(context, TypeReal.get(context));
    }

    @Override
    public int getNumBits() {
        return numBits;
    }    
    
    public TypeReal getTypeReal() {
        return typeReal;
    }
    
    @Override
    public ValueComplex newValue() {
    	return new ValueComplex(this, getTypeReal().newValue(),
    			getTypeReal().newValue());
    }

    public ValueComplex newValue(ValueReal real, ValueReal imag) {
        return new ValueComplex(this, UtilValue.clone(real), UtilValue.clone(imag));
    }
    
    @Override
    public boolean canImport(Type type) {
        assert type != null;
        if (type instanceof TypeComplex) {
            TypeComplex otherTypeComplex = (TypeComplex) type;
            return typeReal.canImport(otherTypeComplex.getTypeReal());
        } else if (TypeReal.isReal(type)) {
            return typeReal.canImport(type);
        } else {
            return false;            
        }
    }
    
    @Override
    public ValueComplex getOne() {
        return valueOne;
    }

    @Override
    public ValueComplex getZero() {
        return valueZero;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("complex");
        return builder.toString();
    }

    @Override
    public ContextValue getContext() {
        return context;
    }
    
    @Override
    public TypeArrayComplex getTypeArray() {
    	// TODO 
    	return null;
//    	return (TypeArrayComplex) TypeAlgebra.super.getTypeArray();
    }
}
