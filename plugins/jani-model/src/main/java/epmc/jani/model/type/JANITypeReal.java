package epmc.jani.model.type;

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeReal implements JANIType {
	public final static String IDENTIFIER = "real";
	/** Identifier for real type. */
	private final static String REAL = "real";
	
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
		if (!valueString.getString().equals(REAL)) {
			return null;
		}
		return this;
	}

	@Override
	public JsonValue generate() {
		return UtilJSON.toStringValue(REAL);
	}

	@Override
	public TypeReal toType() {
		assert contextValue != null;
		return TypeReal.get(contextValue);
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
