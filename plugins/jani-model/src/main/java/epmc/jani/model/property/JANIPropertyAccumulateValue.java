package epmc.jani.model.property;

import java.util.LinkedHashMap;
import java.util.Map;

public enum JANIPropertyAccumulateValue {
	STEPS("steps"),
	TIME("time");
	
	private final String string;
	
	private final static Map<String, JANIPropertyAccumulateValue> values = new LinkedHashMap<>();
	static {
		values.put("steps",JANIPropertyAccumulateValue.STEPS);
		values.put("time", JANIPropertyAccumulateValue.TIME);
	}
	
	private JANIPropertyAccumulateValue(String string) {
		this.string = string;
	}
	
	public static final Map<String, JANIPropertyAccumulateValue> getAccumulateValues() {
		return values;
	}
	
	@Override
	public String toString() {
		return string;
	}
}
