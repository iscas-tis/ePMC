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
import epmc.value.Value;

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

	/** Value context used. */
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
		assert model != null;
		initialized = false;
		if (!(value instanceof JsonString)) {
			return null;
		}
		JsonString valueString = (JsonString) value;
		if (!valueString.getString().equals(CLOCK)) {
			return null;
		}
		contextValue = model.getContextValue();
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		return UtilJSON.toStringValue(CLOCK);
	}

	@Override
	public void setContextValue(ContextValue contextValue) {
		this.contextValue = contextValue;
	}

	@Override
	public TypeClock toType() {
		return getContextValue().makeUnique(new TypeClock(getContextValue()));
	}

	/**
	 * Get value context from expression context of this JANI type.
	 * 
	 * @return value context from expression context of this JANI type
	 */
	private ContextValue getContextValue() {
		return contextValue;
	}

	@Override
	public Value getDefaultValue() throws EPMCException {
		return UtilValue.newValue(toType(), 0);
	}
}
