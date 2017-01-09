package epmc.time;

import epmc.value.UtilValue;
import epmc.value.Value;

final class Bounds {
	private Value lower;
	Value upper;

	private Bounds() {
	}
	
	Bounds(Value value) {
		assert value != null;
		lower = UtilValue.clone(value);
		upper = UtilValue.clone(value);
	}

	public Bounds(Value lower, Value upper) {
		assert lower != null;
		assert upper != null;
		this.lower = UtilValue.clone(lower);
		this.upper = UtilValue.clone(upper);
	}
}
