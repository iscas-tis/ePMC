package epmc.value;

import epmc.value.Value;

public interface ValueContentIntArray { // extends Value
	static boolean isIntArray(Value value) {
		return value instanceof ValueContentIntArray;
	}
	
	static ValueContentIntArray asIntArray(Value value) {
		if (isIntArray(value)) {
			return (ValueContentIntArray) value;
		} else {
			return null;
		}
	}
	
	static int[] getContent(Value value) {
		ValueContentIntArray valueContentIntArray = asIntArray(value);
		if (valueContentIntArray != null) {
			return valueContentIntArray.getIntArray();
		} else {
			return null;
		}
	}
	
	int[] getIntArray();
}
