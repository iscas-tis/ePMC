package epmc.jani.model.type;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.JANINode;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public interface JANIType extends JANINode {
	
	void setContextValue(ContextValue contextValue);
	
	JANIType parseAsJANIType(JsonValue value) throws EPMCException;
	
	Type toType() throws EPMCException;

	Value getDefaultValue() throws EPMCException;
	
	default JANIType replace(Map<Expression, Expression> map) {
		return this;
	}
}
