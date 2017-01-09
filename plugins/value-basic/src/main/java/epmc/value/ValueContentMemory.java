package epmc.value;

import java.nio.ByteBuffer;

import epmc.value.Value;

public interface ValueContentMemory { // extends Value
	static boolean isMemory(Value value) {
		return value instanceof ValueContentMemory;
	}
	
	static ValueContentMemory asMemory(Value value) {
		if (isMemory(value)) {
			return (ValueContentMemory) value;
		} else {
			return null;
		}
	}
	
	static ByteBuffer getMemory(Value value) {
		ValueContentMemory valueMemory = asMemory(value);
		if (valueMemory != null) {
			return valueMemory.getMemory();
		} else {
			return null;
		}
	}
	
    ByteBuffer getMemory();
}
