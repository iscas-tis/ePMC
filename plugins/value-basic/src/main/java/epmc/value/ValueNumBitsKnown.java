package epmc.value;

import epmc.value.Value;

public interface ValueNumBitsKnown extends Value {
	static boolean isNumBitsKnown(Value value) {
		if (!(value instanceof ValueNumBitsKnown)) {
			return false;
		}
		ValueNumBitsKnown valueNumBitsKnown = (ValueNumBitsKnown) value;
		if (valueNumBitsKnown.getNumBits() == TypeNumBitsKnown.UNKNOWN) {
			return false;
		}
		return true;
	}
	
	static ValueNumBitsKnown asNumBitsKnown(Value value) {
		if (isNumBitsKnown(value)) {
			return (ValueNumBitsKnown) value;
		} else {
			return null;
		}
	}
	
	static int getNumBits(Value value) {
		ValueNumBitsKnown valueNumBitsKnown = ValueNumBitsKnown.asNumBitsKnown(value);
		if (valueNumBitsKnown != null) {
			return valueNumBitsKnown.getNumBits();
		} else {
			return TypeNumBitsKnown.UNKNOWN;
		}
	}
	
    default int getNumBits() {
    	return TypeNumBitsKnown.getNumBits(getType());
    }
}
