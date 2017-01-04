package epmc.value;

import epmc.error.EPMCException;
import epmc.value.Value;

public interface ValueRange extends Value {
	static boolean isRange(Value value) {
		return value instanceof ValueRange;
	}
	
	static ValueRange asRange(Value value) {
		if (isRange(value)) {
			return (ValueRange) value;
		} else {
			return null;
		}
	}

	static boolean checkRange(Value value) throws EPMCException {
		ValueRange valueRange = asRange(value);
		if (valueRange == null) {
			return true;
		}
		return valueRange.checkRange();
	}
	
    boolean checkRange() throws EPMCException;
}
