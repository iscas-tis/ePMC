package epmc.mpfr.value;

import com.sun.jna.NativeLong;

import epmc.mpfr.MPFR;
import epmc.mpfr.options.OptionsMPFR;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArrayReal;
import epmc.value.TypeInteger;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

public final class TypeMPFR implements TypeReal, TypeWeight, TypeWeightTransition, TypeNumBitsKnown {
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
	private ContextValue context;
	
    private final ValueMPFR valueOne;
    private final ValueMPFR valueZero;
    private final ValueMPFR valuePosInf;
    private final ValueMPFR valueNegInf;
    private final ValueMPFR valueUnderflow;
    private final ValueMPFR valueOverflow;

	private final int precision;

	public TypeMPFR(ContextValue context) {
		assert context != null;
        assert context != null;
        Options options = context.getOptions();
		this.precision = options.getInteger(OptionsMPFR.MPFR_PRECISION);

        this.context = context;
        
        valueOne = new ValueMPFR(this);
        valueZero = new ValueMPFR(this);
        valuePosInf = new ValueMPFR(this);
        valueNegInf = new ValueMPFR(this);
        valueUnderflow = new ValueMPFR(this);
        valueOverflow = new ValueMPFR(this);

        MPFR.mpfr_set_si(valueOne.getMpfr(), new NativeLong(1), MPFR.MPFR_RNDN);
        MPFR.mpfr_set_zero(valueZero.getMpfr(), 0);
        MPFR.mpfr_set_inf(valuePosInf.getMpfr(), 1);
        MPFR.mpfr_set_inf(valueNegInf.getMpfr(), -1);
        MPFR.mpfr_set_d(valueUnderflow.getMpfr(), 0.0, MPFR.MPFR_RNDN);
        MPFR.mpfr_nextabove(valueUnderflow.getMpfr());        
        MPFR.mpfr_set_d(valueOverflow.getMpfr(), Double.POSITIVE_INFINITY, MPFR.MPFR_RNDN);
        MPFR.mpfr_nextbelow(valueOverflow.getMpfr());

        valueOne.setImmutable();
        valueZero.setImmutable();
        valuePosInf.setImmutable();
        valueNegInf.setImmutable();
        valueUnderflow.setImmutable();
        valueOverflow.setImmutable();
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
    public boolean canImport(Type a) {
        assert a != null;
        return TypeReal.isReal(a) || TypeInteger.isInteger(a);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("mpfr");
        return builder.toString();
    }

    @Override
    public ValueMPFR newValue() {
    	ValueMPFR result;
    	result = new ValueMPFR(this);
    	return result;
    }

    @Override
    public ValueMPFR getZero() {
        return valueZero;
    }

    @Override
    public ValueMPFR getOne() {
        return valueOne;
    }

    @Override
    public ValueMPFR getUnderflow() {
        return valueUnderflow;
    }

    @Override
    public ValueMPFR getOverflow() {
        return valueOverflow;
    }

    @Override
    public ValueMPFR getPosInf() {
        return valuePosInf;
    }

    @Override
    public ValueMPFR getNegInf() {
        return valueNegInf;
    }
    
    @Override
    public int getNumBits() {
    	return NativeLong.SIZE * 8 + precision;
    }
    
    int getPrecision() {
    	return precision;
    }

	@Override
	public TypeArrayReal getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
