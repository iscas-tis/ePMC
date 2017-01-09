package epmc.jani.valuejson;

import javax.json.Json;
import javax.json.JsonValue;

import epmc.value.Value;
import epmc.value.ValueInteger;

public class ValueJSONInt implements ValueJSON {
    final static String ARBITRARY = "arbitrary";

	@Override
	public JsonValue convert(Value value) {
		assert value != null;
		if (!ValueInteger.isInteger(value)) {
			return null;
		}
        return Json.createObjectBuilder().add(ARBITRARY,
        		ValueInteger.asInteger(value).getInt())
                .build().get(ARBITRARY);
	}

}
