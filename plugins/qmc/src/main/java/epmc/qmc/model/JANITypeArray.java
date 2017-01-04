package epmc.qmc.model;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.type.JANIType;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class JANITypeArray implements JANIType {
	private final JANIType entryType;
	private final Positional positional;
	private transient ModelJANI model;

	JANITypeArray(JANIType entryType, Positional positional) {
		this.entryType = entryType;
		this.positional = positional;
	}
	
	@Override
	public JANIType replace(Map<Expression, Expression> map) {
		return this;
	}

	public void checkExpressionConsistency(Map<Expression, Type> types) throws EPMCException {
	}

	@Override
	public TypeArray toType() throws EPMCException {
        return entryType.toType().getTypeArray();
	}
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonValue generate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContextValue(ContextValue contextValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Value getDefaultValue() throws EPMCException {
		TypeArray type = toType();
		Value entryDefault = entryType.getDefaultValue();
		ValueArray result = type.newValue();
		int resultSize = result.size();
		for (int i = 0; i < resultSize; i++) {
			result.set(entryDefault, i);
		}
		return result;
	}
}
