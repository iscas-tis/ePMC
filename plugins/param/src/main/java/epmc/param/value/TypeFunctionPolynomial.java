package epmc.param.value;

import gnu.trove.set.hash.THashSet;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import epmc.param.value.cancellator.Cancellator;
import epmc.param.value.cancellator.CancellatorGiNaC;
import epmc.value.ContextValue;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeArrayReal;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.ValueReal;

public final class TypeFunctionPolynomial implements TypeFunction {
	private final static Set<String> SUPPORTED_OPERATORS;
	static {
		Set<String> supported = new THashSet<>();
		supported.add(OperatorAdd.IDENTIFIER);
		supported.add(OperatorAddInverse.IDENTIFIER);
		supported.add(OperatorSubtract.IDENTIFIER);
		supported.add(OperatorMultiply.IDENTIFIER);
		SUPPORTED_OPERATORS = Collections.unmodifiableSet(supported);
	}
	private ContextValuePARAM contextValue;
	private final Cancellator cancellator;

	TypeFunctionPolynomial(ContextValuePARAM contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
//		this.cancellator = new CancellatorCoCoALib(this);
		this.cancellator = new CancellatorGiNaC(this);
// 		this.cancellator = new CancellatorJAS(this);
	}

	@Override
	public ContextValue getContext() {
		return this.contextValue.getContextValue();
	}
	
	@Override
	public ContextValuePARAM getContextPARAM() {
		return this.contextValue;
	}

	@Override
	public ValueFunctionPolynomial newValue() {
		ValueFunctionPolynomial function = new ValueFunctionPolynomial(this);
		return function;
	}
	
	public ValueFunctionPolynomial newValue(int oldNumParameters, int numTerms,
			int[] monomials, BigInteger[] coefficients) {
		return new ValueFunctionPolynomial(this, oldNumParameters,
				numTerms, monomials, coefficients);
	}
	
	public void cancelCommonFactors(ValueFunctionPolynomial operand1, ValueFunctionPolynomial operand2) {
		assert operand1 != null;
		assert operand2 != null;
		this.cancellator.cancel(operand1, operand2);
	}

	@Override
	public boolean canImport(Type type) {
		if (type instanceof TypeFunctionPolynomial) {
			TypeFunctionPolynomial other = (TypeFunctionPolynomial) type;
			return this.getContextPARAM() == other.getContextPARAM();
		} else if (TypeInteger.isInteger(type)) {
			return true;
		} else if (TypeReal.isReal(type)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("polynomial");
		builder.append(getParameters());
		return builder.toString();
	}

	@Override
	public boolean isSupportOperator(String identifier) {
		assert identifier != null;
		return SUPPORTED_OPERATORS.contains(identifier);
	}

	@Override
	public ValueFunctionPolynomial getZero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueFunctionPolynomial getOne() {
		// TODO Auto-generated method stub
		return null;
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
