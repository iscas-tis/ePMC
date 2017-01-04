package epmc.jani.model.expression;

import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.util.UtilJSON;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.ValueReal;

public final class JANIExpressionReal implements JANIExpression {
	public final static String IDENTIFIER = "real";

	private boolean initialized = false;
	
	private ModelJANI model;

	private String number;

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
		initialized = false;
		if (!(value instanceof JsonNumber)) {
			return null;
		}
		JsonNumber number = (JsonNumber) value;
		if (number.isIntegral()) {
			return null;
		}
		this.number = number.toString();
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		assert model != null;
		return UtilJSON.toNumberValue(number);
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) {
		assert expression != null;
		assert model != null;
		initialized = false;
		if (!(expression instanceof ExpressionLiteral)) {
			return null;
		}
		ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
		if (!ValueReal.isReal(expressionLiteral.getValue())) {
			return null;
		}
		number = expressionLiteral.getValue().toString();
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() throws EPMCException {
		assert initialized;
		assert model != null;
		return new ExpressionLiteral.Builder()
				.setValue(UtilValue.newValue(TypeReal.get(model.getContextValue()), number))
				.build();
	}

	@Override
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
	}
}
