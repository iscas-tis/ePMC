package epmc.jani.type.qmc;

import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.ValueInteger;

public final class JANIExpressionComplex implements JANIExpression {
	public final static String IDENTIFIER = "complex";
	private final static String COMPLEX = "complex";

	private ModelJANI model;
	
	private boolean initialized;
	
	private int number;
	
	private void resetFields() {
		initialized = false;
	}
	
	public JANIExpressionComplex() {
		resetFields();
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
		return parseAsJANIExpression(value);
	}
	
	@Override 
	public JANIExpression parseAsJANIExpression(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		resetFields();
		if (!(value instanceof JsonObject)) {
			return null;
		}
		JsonObject object = UtilJSON.toObject(value);
		if (!object.containsKey(COMPLEX)) {
			return null;
		}
		if (!(object.get(COMPLEX) instanceof JsonString)) {
			return null;
		}
/*
		if (!number.isIntegral()) {
			return null;
		}
		valid = true;
		this.number = number.intValue();
		*/
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		return UtilJSON.toIntegerValue(number);
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) {
		assert expression != null;
		resetFields();
		if (!(expression instanceof ExpressionLiteral)) {
			return null;
		}
		ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
		if (!ValueInteger.isInteger(expressionLiteral.getValue())) {
			return null;
		}
		number = ValueInteger.asInteger(expressionLiteral.getValue()).getInt();
		initialized = true;
		return this;
	}

	@Override
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
	}
	
	@Override
	public Expression getExpression() throws EPMCException {
		assert initialized;
		TypeInteger typeInteger = TypeInteger.get(model.getContextValue());
		return new ExpressionLiteral.Builder()
				.setValue(UtilValue.newValue(typeInteger, number))
				.build();
		}
}
