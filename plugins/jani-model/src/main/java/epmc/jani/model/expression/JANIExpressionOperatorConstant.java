package epmc.jani.model.expression;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperator;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;
import epmc.value.Operator;

/**
 * JANI expression for constants, that is zero-ary operators.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIExpressionOperatorConstant implements JANIExpression {
	public final static String IDENTIFIER = "operator-constant";
	private final static String CONSTANT = "constant";
	
	private ModelJANI model;
	
	private boolean initialized;
	private JANIOperator operator;
	
	private void resetFields() {
		initialized = false;
		operator = null;
	}
	
	public JANIExpressionOperatorConstant() {
		resetFields();
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
		JsonObject object = (JsonObject) value;
		if (!object.containsKey(CONSTANT)) {
			return null;
		}
		if (!(object.get(CONSTANT) instanceof JsonString)) {
			return null;
		}
		JANIOperators operators = model.getJANIOperators();
		operator = UtilJSON.toOneOf(object, CONSTANT, operators::getOperatorByJANI);
		if (operator.getArity() != 0) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		assert initialized;
		assert model != null;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(CONSTANT, operator.getJANI());
		return builder.build();
	}


	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) {
		assert expression != null;
		assert model != null;
		resetFields();
		if (!(expression instanceof ExpressionOperator)) {
			return null;
		}
		ExpressionOperator expressionOperator = (ExpressionOperator) expression;
		operator = getJANIOperators().getOperator(expressionOperator.getOperator());
		if (operator.getArity() != 0) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() throws EPMCException {
		assert initialized;
		assert model != null;
		Operator operator = this.operator.getOperator(model.getContextValue());
		return new ExpressionOperator.Builder()
				.setOperator(operator)
				.setOperands()
				.build();
	}

	private JANIOperators getJANIOperators() {
		assert model != null;
		return model.getJANIOperators();
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
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
	}	

	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
