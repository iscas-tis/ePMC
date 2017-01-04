package epmc.qmc.model;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.type.JANIType;
import epmc.qmc.expression.ContextExpressionQMC;
import epmc.qmc.value.TypeSuperOperator;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeSuperoperator implements JANIType {
	private final ContextExpressionQMC contextExpression;
	private final int hilbertDimension;
	private final Positional positional;
	private transient ModelJANI model;

	public JANITypeSuperoperator(ContextExpressionQMC contextExpression, int hilbertDimension, Positional positional) {
		this.contextExpression = contextExpression;
		this.hilbertDimension = hilbertDimension;
		this.positional = positional;
	}

	@Override
	public JANIType replace(Map<Expression, Expression> map) {
		return this;
	}

	public void checkExpressionConsistency(Map<Expression, Type> types) throws EPMCException {
	}

	@Override
	public TypeSuperOperator toType() throws EPMCException {
		if (hilbertDimension <= 0) {
			return contextExpression.getContextValueQMC().getTypeSuperOperator();
		} else {
			return contextExpression.getContextValueQMC().getTypeSuperOperator(hilbertDimension);
		}
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
		return UtilValue.newValue(toType(), 0);
	}
}
