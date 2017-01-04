package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

import epmc.util.UtilJSON;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class ResultFormatterBool implements ResultFormatter {
	public final static String IDENTIFIER = "bool";
	private final static String BOOL = "bool";
	private final static JsonValue BOOL_VALUE = UtilJSON.toStringValue(BOOL);
	private Object result;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setResult(Object result) {
		assert result != null;
		this.result = result;
	}

	@Override
	public boolean canHandle() {
		if (!(result instanceof Value)) {
			return false;
		}
		Value valueResult = (Value) result;
		if (!ValueBoolean.isBoolean(valueResult)) {
			return false;
		}
		return true;
	}

	@Override
	public String getLabel() {
		assert canHandle();
		return null;
	}

	@Override
	public JsonValue getType() {
		assert canHandle();
		return BOOL_VALUE;
	}

	@Override
	public JsonValue getValue() {
		assert canHandle();
		Value valueResult = (Value) result;
		return UtilJSON.toBooleanValue(ValueBoolean.asBoolean(valueResult).getBoolean());
	}

	@Override
	public String getFormattedValue() {
		assert canHandle();
		return result.toString();
	}

}
