package epmc.jani.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.type.JANIType;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.Type;

public final class Constants implements JANINode, Iterable<Constant>, ExpressionToType {
	/** Model which this constants belong to. */
	private ModelJANI model;
	/** Map from constant names to constants. */
	private final Map<String,Constant> constants = new LinkedHashMap<>();

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
		assert value != null;
		JsonArray array = UtilJSON.toArray(value);
		for (JsonValue var : array) {
			Constant constant = new Constant();
			constant.setModel(model);
			constant.setValidIdentifiers(constants);
			constant.parse(var);
			constants.put(constant.getName(), constant);
		}
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		for (Constant constant : constants.values()) {
			builder.add(constant.generate());
		}
		return builder.build();
	}

	@Override
	public Iterator<Constant> iterator() {
		return constants.values().iterator();
	}

	public Map<String, Constant> getConstants() {
		return constants;
	}
	
	public void put(String name, Constant constant) {
		constants.put(name, constant);
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}

	@Override
	public Type getType(Expression expression) throws EPMCException {
		assert expression != null;
		ExpressionIdentifierStandard identifier = ExpressionIdentifierStandard.asIdentifierStandard(expression);
		if (identifier == null) {
			return null;
		}
		if (identifier.getScope() != null) {
			return null;
		}
		Constant constant = constants.get(identifier.getName());
		if (constant == null) {
			return null;
		}
		JANIType type = constant.getType();
		if (type == null) {
			return null;
		}
		return type.toType();
	}

	@Override
	public ContextValue getContextValue() {
		return model.getContextValue();
	}
}
