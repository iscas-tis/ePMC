package epmc.value;

import epmc.value.Value;

public interface ValueContentDoubleArray { // extends Value
	static boolean isDoubleArray(Value value) {
		return value instanceof ValueContentDoubleArray;
	}
	
	static ValueContentDoubleArray asDoubleArray(Value value) {
		if (isDoubleArray(value)) {
			return (ValueContentDoubleArray) value;
		} else {
			return null;
		}
	}
	
	static double[] getContent(Value value) {
		ValueContentDoubleArray valueContentDoubleArrayContent = asDoubleArray(value);
		if (valueContentDoubleArrayContent != null) {
			return valueContentDoubleArrayContent.getDoubleArray();
		} else {
			return null;
		}
	}
	
	double[] getDoubleArray();
}
