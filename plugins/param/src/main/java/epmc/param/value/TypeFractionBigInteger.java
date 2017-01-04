package epmc.param.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;

public final class TypeFractionBigInteger extends TypeFraction implements TypeNumBitsKnown {
    private ContextValuePARAM context;
    private final ValueFractionBigInteger underflow;
    private final ValueFractionBigInteger overflow;
    private final ValueFractionBigInteger posInf;
    private final ValueFractionBigInteger negInf;

    public TypeFractionBigInteger(ContextValuePARAM context) {
    	assert context != null;
        this.context = context;
        this.underflow = newValue(0);
        this.overflow = newValue();
        this.overflow.setNumerator(1);
        this.overflow.setDenominator(0);
        this.negInf = newValue();
        this.negInf.setNumerator(-1);
        this.negInf.setDenominator(0);
        this.posInf = newValue();
        this.posInf.setNumerator(1);
        this.posInf.setDenominator(0);
    }

    @Override
    public boolean canImport(Type type) {
    	assert type != null;
    	if (type instanceof TypeFractionBigInteger) {
    		return true;
    	}
    	if (TypeReal.isReal(type)) {
    		return true;
    	}
    	if (TypeInteger.isInteger(type)) {
    		return true;
    	}
    	return false;
    }
    
    @Override
    public ValueFractionBigInteger newValue() {
        return new ValueFractionBigInteger(this);
    }

    public ValueFractionBigInteger newValue(int value) {
    	ValueFractionBigInteger result = newValue();
    	result.set(value);
    	return result;
    }
    
    @Override
    public ValueFractionBigInteger getUnderflow() {
    	return underflow;
    }

    @Override
    public ValueFractionBigInteger getOverflow() {
    	return overflow;
    }


    @Override
    public ContextValue getContext() {
    	return context.getContextValue();
    }

    @Override
    public int getNumBits() {
    	// TODO HACK
    	return 0;
    }
    
    @Override
    public ValueFractionBigInteger getPosInf() {
    	return posInf;
    }
    
    @Override
    public ValueFractionBigInteger getNegInf() {
    	return negInf;
    }

    @Override
    public TypeArrayFractionBigInteger getTypeArray() {
        assert getContext() != null;
        return (TypeArrayFractionBigInteger) context.getTypeArray(this);
    }

	@Override
	public ValueFractionBigInteger getZero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueFractionBigInteger getOne() {
		// TODO Auto-generated method stub
		return null;
	}
}
