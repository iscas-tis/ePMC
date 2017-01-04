package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

import epmc.util.UtilJSON;

public final class ResultFormatterGeneral implements ResultFormatter {
	public final static String IDENTIFIER = "generic";
	private final static String STRING = "string";
	private final static JsonValue STRING_VALUE = UtilJSON.toStringValue(STRING);
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
		assert result != null;
		return true;
	}

	@Override
	public String getLabel() {
		assert result != null;
		return null;
	}

	@Override
	public JsonValue getType() {
		assert result != null;
		return STRING_VALUE;
	}

	@Override
	public JsonValue getValue() {
		assert result != null;
		return UtilJSON.toStringValue(result.toString());
	}

	@Override
	public String getFormattedValue() {
		assert result != null;
		return result.toString();
	}

}
