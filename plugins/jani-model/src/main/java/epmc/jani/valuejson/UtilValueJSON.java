package epmc.jani.valuejson;

import java.util.List;

import javax.json.JsonValue;

import epmc.options.Options;
import epmc.util.Util;
import epmc.value.Value;

public final class UtilValueJSON {
	public static JsonValue valueToJson(Value value) {
		assert value != null;
		Options options = value.getType().getContext().getOptions();
		List<Class<? extends ValueJSON>> valueJsonClasses = options.get(OptionsJANIValueJSON.JANI_VALUEJSON_CLASS);
		assert valueJsonClasses != null;
		for (Class<? extends ValueJSON> clazz : valueJsonClasses) {
			ValueJSON valueJson = Util.getInstance(clazz);
			JsonValue result = valueJson.convert(value);
			if (result != null) {
				return result;
			}
		}
		assert false;
		return null;
	}
	
	private UtilValueJSON() {
	}
}
