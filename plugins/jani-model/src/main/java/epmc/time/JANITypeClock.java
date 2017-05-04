package epmc.time;

import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.type.JANIType;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.UtilValue;

/**
 * Clock type.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANITypeClock implements JANIType {
	public final static String IDENTIFIER = "clock";
	/** Identifier for boolean type. */
	private final static String CLOCK = "clock";
	
	private boolean initialized = false;

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
		assert model != null;
		initialized = false;
		if (!(value instanceof JsonString)) {
			return null;
		}
		JsonString valueString = (JsonString) value;
		if (!valueString.getString().equals(CLOCK)) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		return UtilJSON.toStringValue(CLOCK);
	}

	@Override
	public TypeClock toType() {
		return ContextValue.get().makeUnique(new TypeClock());
	}

	@Override
	public ValueClock getDefaultValue() throws EPMCException {
		return UtilValue.newValue(toType(), 0);
	}
}