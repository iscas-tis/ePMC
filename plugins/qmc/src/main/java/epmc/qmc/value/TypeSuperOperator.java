package epmc.qmc.value;

import epmc.qmc.options.OptionsQMC;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

// TODO integrate JSci functionality within rest of setting

public final class TypeSuperOperator implements TypeWeightTransition, TypeWeight, TypeNumBitsKnown {
    private static final long serialVersionUID = 1L;
    private final int dimensions;
    public static final int DIMENSIONS_UNSPECIFIED = -1;
    private final int numBits;
    private final ValueSuperOperator zero;
    private final ValueSuperOperator one;
    private final ValueSuperOperator posInf;
    private final ValueSuperOperator negInf;
    private final transient ContextValueQMC contextValueQMC;

    TypeSuperOperator(ContextValueQMC contextQMC, int dimensions) {
        this.contextValueQMC = contextQMC;
        assert dimensions == DIMENSIONS_UNSPECIFIED || dimensions >= 0;
        this.dimensions = dimensions;
        if (dimensions > 0) {
            ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
            numBits = (dimensions * dimensions) * (dimensions * dimensions)
                    * contextValueQMC.getTypeComplex().getNumBits();
        } else {
            numBits = -1;
        }
        this.zero = newValue();
        this.one = newValue();
        this.one.getMatrix().set(TypeReal.get(getContext()).getOne(), 0);
        this.posInf = newValue();
        this.posInf.getMatrix().set(TypeReal.get(getContext()).getPosInf(), 0);
        this.negInf = newValue();
        this.negInf.getMatrix().set(TypeReal.get(getContext()).getNegInf(), 0);
    }
    
    public TypeSuperOperator(ContextValueQMC contextQMC) {
        this(contextQMC, DIMENSIONS_UNSPECIFIED);
    }

    public TypeReal getTypeReal() {
        return TypeReal.get(getContext());
    }

    TypeComplex getTypeComplex() {
        return contextValueQMC.getTypeComplex();
    }
    
    TypeArrayAlgebra getTypeMatrix() {
        assert contextValueQMC.getTypeComplex() != null;
        return contextValueQMC.getTypeComplex().getTypeArray();
    }

    @Override
    public ValueSuperOperator newValue() {
    	return new ValueSuperOperator(this);
    }

    @Override
    public boolean canImport(Type type) {
        assert type != null;
        if (type instanceof TypeSuperOperator) {
            TypeSuperOperator typeSuperOperator = (TypeSuperOperator) type;
            if (getTypeComplex() == null) {
                // TODO HACK for deserialisation
                return true;
            } else {
                return getTypeComplex().canImport(typeSuperOperator.getTypeComplex());
            }
        } else if (getTypeComplex().canImport(type)) {
            return true;
        }
        return false;
    }
    
    public Type getTypeList() {
        return getTypeMatrix().getTypeArray();
    }

    public boolean hasDimensions() {
        return dimensions != DIMENSIONS_UNSPECIFIED;
    }
    
    public int getSuperoperatorDimensions() {
        return dimensions;
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!super.equals(obj)) {
            return false;
        }
        TypeSuperOperator other = (TypeSuperOperator) obj;
        return this.dimensions == other.dimensions;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = super.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = dimensions + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("superoperator(" + dimensions + ")");
        return builder.toString();
    }
    
    @Override
    public int getNumBits() {
        return numBits;
    }
    
    @Override
    public ValueSuperOperator getZero() {
        return zero;
    }
    
    @Override
    public ValueSuperOperator getOne() {
        return one;
    }
    
    @Override
    public ValueSuperOperator getPosInf() {
        return posInf;
    }
    
    @Override
    public ValueSuperOperator getNegInf() {
        return negInf;
    }

    @Override
    public ContextValue getContext() {
        return contextValueQMC.getContextValue();
    }

	@Override
	public TypeArrayAlgebra getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
