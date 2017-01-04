package epmc.jani.model.type;

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeInt implements JANIType {
	public final static String IDENTIFIER = "int";
	/** Identifier for integer type. */
	private final static String INT = "int";
	
	private ContextValue contextValue;
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
		return parseAsJANIType(value);
	}
	
	@Override 
	public JANIType parseAsJANIType(JsonValue value) throws EPMCException {
		if (!(value instanceof JsonString)) {
			return null;
		}
		JsonString valueString = (JsonString) value;
		if (!valueString.getString().equals(INT)) {
			return null;
		}
		return this;
	}

	@Override
	public JsonValue generate() {
		return UtilJSON.toStringValue(INT);
	}

	@Override
	public TypeInteger toType() {
		assert contextValue != null;
		return TypeInteger.get(contextValue);
	}

	@Override
	public void setContextValue(ContextValue contextValue) {
		this.contextValue = contextValue;
	}

	@Override
	public Value getDefaultValue() throws EPMCException {
		return UtilValue.newValue(toType(), 0);
	}

	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
