package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

import epmc.util.UtilJSON;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class ResultFormatterInt implements ResultFormatter {
	public final static String IDENTIFIER = "int";
	private final static String INT = "int";
	private final static JsonValue INT_VALUE = UtilJSON.toStringValue(INT);
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
		if (!ValueInteger.isInteger(valueResult)) {
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
		return INT_VALUE;
	}

	@Override
	public JsonValue getValue() {
		assert canHandle();
		Value valueResult = (Value) result;
		return UtilJSON.toIntegerValue(ValueInteger.asInteger(valueResult).getInt());
	}

	@Override
	public String getFormattedValue() {
		assert canHandle();
		return result.toString();
	}

}
