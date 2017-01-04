package epmc.jani.valuejson;

import javax.json.Json;
import javax.json.JsonValue;

import epmc.value.Value;
import epmc.value.ValueDouble;

public class ValueJSONDouble implements ValueJSON {
    final static String ARBITRARY = "arbitrary";

	@Override
	public JsonValue convert(Value value) {
		assert value != null;
		if (!(ValueDouble.isDouble(value))) {
			return null;
		}
        return Json.createObjectBuilder().add(ARBITRARY, ValueDouble.asDouble(value).getDouble())
                .build().get(ARBITRARY);
	}

}
