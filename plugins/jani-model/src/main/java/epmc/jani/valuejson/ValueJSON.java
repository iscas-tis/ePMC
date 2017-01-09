package epmc.jani.valuejson;

import javax.json.JsonValue;

import epmc.value.Value;

public interface ValueJSON {
	JsonValue convert(Value value);
}
