package epmc.jani.valuejson;

import javax.json.Json;
import javax.json.JsonValue;

import epmc.value.Value;

public class ValueJSONGeneral implements ValueJSON {
    final static String ARBITRARY = "arbitrary";

	@Override
	public JsonValue convert(Value value) {
		assert value != null;
        return Json.createObjectBuilder().add(ARBITRARY, value.toString())
                .build().get(ARBITRARY);
	}

}
