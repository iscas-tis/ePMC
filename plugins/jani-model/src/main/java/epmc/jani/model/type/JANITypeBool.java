package epmc.jani.model.type;

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;

/**
 * Boolean type.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANITypeBool implements JANIType {
	public final static String IDENTIFIER = "bool";
	/** Identifier for boolean type. */
	private final static String BOOL = "bool";

	private ContextValue contextValue;
	/** Whether the last try to parse type was successful. */
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
		if (!valueString.getString().equals(BOOL)) {
			return null;
		}
		return this;
	}

	@Override
	public JsonValue generate() {
		return UtilJSON.toStringValue(BOOL);
	}

	@Override
	public void setContextValue(ContextValue contextValue) {
		this.contextValue = contextValue;
	}


	@Override
	public Type toType() {
		assert contextValue != null;
		return TypeBoolean.get(contextValue);
	}

	@Override
	public Value getDefaultValue() throws EPMCException {
		assert contextValue != null;
		return TypeBoolean.get(contextValue).getFalse();
	}

	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
