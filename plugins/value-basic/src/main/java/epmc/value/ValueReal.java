package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Value;

public interface ValueReal extends ValueNumber {
	static boolean isReal(Value value) {
		return TypeReal.isReal(value.getType());
	}
	
	static ValueReal asReal(Value value) {
		if (isReal(value)) {
			return (ValueReal) value;
		} else {
			return null;
		}
	}
	
	@Override
	TypeReal getType();
	
    void set(double value);
    
    void exp(Value operand) throws EPMCException;
    
    void pow(Value operand1, Value operand2) throws EPMCException;
    
    void log(Value operand1, Value operand2) throws EPMCException;
    
    void sqrt(Value operand) throws EPMCException;
    
    void pi() throws EPMCException;
}
