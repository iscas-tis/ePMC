package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

public interface ResultFormatter {
	String getIdentifier();
	
	void setResult(Object result);
	
	boolean canHandle();
	
	String getLabel();

	JsonValue getType();
	
	JsonValue getValue();
	
	String getFormattedValue();
}
