package epmc.value;

import epmc.util.BitStoreable;
import epmc.value.Value;

public interface ValueBitStoreable extends Value, BitStoreable {
	static boolean isBitStoreable(Value value) {
		return value instanceof ValueBitStoreable;
	}
	
	static ValueBitStoreable asBitStoreable(Value value) {
		if (isBitStoreable(value)) {
			return (ValueBitStoreable) value;
		} else {
			return null;
		}
	}
}
