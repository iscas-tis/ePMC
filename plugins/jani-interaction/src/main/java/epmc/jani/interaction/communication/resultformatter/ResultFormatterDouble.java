package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

import epmc.util.UtilJSON;
import epmc.value.Value;
import epmc.value.ValueDouble;


public final class ResultFormatterDouble implements ResultFormatter {
	public final static String IDENTIFIER = "double";
	private final static String DECIMAL = "decimal";
	private final static JsonValue DECIMAL_VALUE = UtilJSON.toStringValue(DECIMAL);
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
		if (!ValueDouble.isDouble(valueResult)) {
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
		return DECIMAL_VALUE;
	}

	@Override
	public JsonValue getValue() {
		assert canHandle();
		ValueDouble valueResult = ValueDouble.asDouble((Value) result);
		return UtilJSON.toNumberValue(valueResult.getDouble());
	}

	@Override
	public String getFormattedValue() {
		assert canHandle();
		return result.toString();
	}

}
