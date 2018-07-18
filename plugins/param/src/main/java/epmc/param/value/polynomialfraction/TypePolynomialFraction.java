package epmc.param.value.polynomialfraction;

import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArrayAlgebra;

public class TypePolynomialFraction implements TypeFunction {
    public final static String IDENTIFIER = "polynomial-fraction";
    
    public final static class Builder implements TypeFunction.Builder {
        private ParameterSet parameters;

        @Override
        public Builder setParameters(ParameterSet parameters) {
            this.parameters = parameters;
            return this;
        }

        @Override
        public TypePolynomialFraction build() {
            return ContextValue.get().makeUnique(new TypePolynomialFraction(this));
        }
    }
    
    public static boolean is(Type type) {
        return type instanceof TypePolynomialFraction;
    }

    public static TypePolynomialFraction as(Type type) {
        if (is(type)) {
            return (TypePolynomialFraction) type;
        } else {
            return null;
        }
    }
    
	private final TypePolynomial typePolynomial;
	private final ValuePolynomialFraction posInf;
	private final ValuePolynomialFraction negInf;

	public TypePolynomialFraction(TypePolynomial typePolynomial) {
		assert typePolynomial != null;
		this.typePolynomial = typePolynomial;
		this.posInf = newValue();
		this.posInf.getNumerator().set(1);
		this.posInf.getDenominator().set(0);
		this.negInf = newValue();
		this.negInf.getNumerator().set(-1);
		this.negInf.getDenominator().set(0);
    }
    
	private TypePolynomialFraction(Builder builder) {
	    this(new TypePolynomial(builder.parameters));
    }

    @Override
	public ValuePolynomialFraction newValue() {
		return new ValuePolynomialFraction(this);
	}

	public TypePolynomial getTypePolynomial() {
		return this.typePolynomial;
	}
	
	@Override
	public ParameterSet getParameterSet() {
		return typePolynomial.getParameterSet();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("polynomialFraction");
		builder.append(getParameterSet().getParameters());
		return builder.toString();
	}
	
	@Override
	public TypeArrayAlgebra getTypeArray() {
	    return ContextValue.get().makeUnique(new TypeArrayPolynomialFraction(this));
	}
}
