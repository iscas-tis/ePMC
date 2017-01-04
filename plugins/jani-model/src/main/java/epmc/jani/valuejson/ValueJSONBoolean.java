package epmc.jani.valuejson;

import javax.json.Json;
import javax.json.JsonValue;

import epmc.value.Value;
import epmc.value.ValueBoolean;

public class ValueJSONBoolean implements ValueJSON {
    final static String ARBITRARY = "arbitrary";

	@Override
	public JsonValue convert(Value value) {
		assert value != null;
		if (!(ValueBoolean.isBoolean(value))) {
			return null;
		}
        return Json.createObjectBuilder().add(ARBITRARY, ValueBoolean.asBoolean(value).getBoolean())
                .build().get(ARBITRARY);
	}

}
