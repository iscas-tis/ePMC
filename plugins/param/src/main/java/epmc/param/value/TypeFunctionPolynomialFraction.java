package epmc.param.value;

import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

import epmc.value.ContextValue;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorDivide;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeArrayReal;
import epmc.value.TypeInteger;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;
import epmc.value.ValueReal;

class TypeFunctionPolynomialFraction implements TypeFunction, TypeNumBitsKnown {
	private final static Set<String> SUPPORTED_OPERATORS;
	static {
		Set<String> supported = new THashSet<>();
		supported.add(OperatorAdd.IDENTIFIER);
		supported.add(OperatorAddInverse.IDENTIFIER);
		supported.add(OperatorSubtract.IDENTIFIER);
		supported.add(OperatorMultiply.IDENTIFIER);
		supported.add(OperatorDivide.IDENTIFIER);
		supported.add(OperatorMultiplyInverse.IDENTIFIER);
		SUPPORTED_OPERATORS = Collections.unmodifiableSet(supported);
	}
	private final TypeFunctionPolynomial typePolynomial;
	private final ValueFunctionPolynomialFraction one;
	private final ValueFunctionPolynomialFraction zero;
	private final ValueFunctionPolynomialFraction posInf;
	private final ValueFunctionPolynomialFraction negInf;

	TypeFunctionPolynomialFraction(TypeFunctionPolynomial typePolynomial) {
		assert typePolynomial != null;
		this.typePolynomial = typePolynomial;
		this.one = newValue();
		this.one.set(1);
		this.zero = newValue();
		this.zero.set(0);
		this.posInf = newValue();
		this.posInf.getNumerator().set(1);
		this.posInf.getDenominator().set(0);
		this.negInf = newValue();
		this.negInf.getNumerator().set(-1);
		this.negInf.getDenominator().set(0);
    }
    
	@Override
	public ContextValue getContext() {
		return typePolynomial.getContext();
	}

	@Override
	public ValueFunctionPolynomialFraction newValue() {
		return new ValueFunctionPolynomialFraction(this);
	}

	public TypeFunctionPolynomial getTypePolynomial() {
		return this.typePolynomial;
	}
	
	@Override
	public boolean canImport(Type type) {
		assert type != null;
		if (type instanceof TypeFunctionPolynomialFraction) {
			TypeFunctionPolynomialFraction other = (TypeFunctionPolynomialFraction) type;
			return this.typePolynomial.canImport(other.typePolynomial);
		}
		if (TypeInteger.isInteger(type)) {
			return true;
		}
		if (TypeReal.isReal(type)) {
			return true;
		}
		return false;
	}
	
	@Override
	public ValueFunctionPolynomialFraction getOne() {
		return one;
	}

	@Override
	public ValueFunctionPolynomialFraction getZero() {
		return zero;
	}
	
	@Override
	public int getNumBits() {
		// TODO HACK
		return 0;
	}

	@Override
	public ContextValuePARAM getContextPARAM() {
		return typePolynomial.getContextPARAM();
	}
	
	@Override
	public ValueFunctionPolynomialFraction getPosInf() {
		return this.posInf;
	}
	
	@Override
	public ValueFunctionPolynomialFraction getNegInf() {
		return this.negInf;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("polynomialFraction");
		builder.append(getParameters());
		return builder.toString();
	}
	
	@Override
	public boolean isSupportOperator(String identifier) {
		assert identifier != null;
		return SUPPORTED_OPERATORS.contains(identifier);
	}

	@Override
	public ValueReal getUnderflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueReal getOverflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeArrayReal getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
