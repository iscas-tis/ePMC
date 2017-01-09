package epmc.jani.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.util.UtilJSON;

public final class Metadata implements JANINode {
	Map<String,String> values = new LinkedHashMap<>();
	private ModelJANI model;

	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}
	
	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		JsonObject object = UtilJSON.toObjectString(value);
		for (Entry<String, JsonValue> entry : object.entrySet()) {
			values.put(entry.getKey(), value.toString());
		}
		return this;
	}

	@Override
	public JsonValue generate() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		for (Entry<String, String> entry : values.entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}
	
	public void put(String key, String value) {
		values.put(key, value);
	}
	
	public String get(String key) {
		return values.get(key);
	}
}
