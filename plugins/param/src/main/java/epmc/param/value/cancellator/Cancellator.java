package epmc.param.value.cancellator;

import epmc.param.value.ValueFunctionPolynomial;

public interface Cancellator {
	void cancel(ValueFunctionPolynomial operand1, ValueFunctionPolynomial operand2);
}
