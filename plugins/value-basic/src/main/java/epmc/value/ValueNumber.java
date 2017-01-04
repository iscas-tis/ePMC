package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Value;

public interface ValueNumber extends ValueAlgebra {
	
	static boolean isNumber(Value value) {
		return value instanceof ValueNumber;
	}
	
	static ValueNumber asNumber(Value value) {
		if (isNumber(value)) {
			return (ValueNumber) value;
		} else {
			return null;
		}
	}
	
	int floorInt() throws EPMCException;
	
    int ceilInt() throws EPMCException;
    
    int signInt() throws EPMCException;
    
    void abs(Value operand) throws EPMCException;
    
    int intcastInt() throws EPMCException;
    
    double getDouble();
    
    int getInt();
}
