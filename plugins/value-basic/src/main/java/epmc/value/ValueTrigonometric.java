package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Value;

public interface ValueTrigonometric extends ValueAlgebra {
	static boolean isTrigonometric(Value value) {
		return value instanceof ValueTrigonometric;
	}
	
	static ValueTrigonometric asTrigonometric(Value value) {
		if (isTrigonometric(value)) {
			return (ValueTrigonometric) value;
		} else {
			return null;
		}
	}
	
    void cos(Value operand) throws EPMCException;
    
    void sin(Value operand) throws EPMCException;

    void tanh(Value operand) throws EPMCException;

    void cosh(Value operand) throws EPMCException;

    void sinh(Value operand) throws EPMCException;

    void atan(Value operand) throws EPMCException;

    void acos(Value operand) throws EPMCException;

    void asin(Value operand) throws EPMCException;

    void tan(Value operand) throws EPMCException;

    void acosh(Value operand) throws EPMCException;

    void atanh(Value operand) throws EPMCException;
    
    void asinh(Value operand) throws EPMCException;
}
