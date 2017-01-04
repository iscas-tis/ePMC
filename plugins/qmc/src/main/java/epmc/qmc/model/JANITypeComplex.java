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
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class JANITypeComplex implements JANIType {
	private ContextExpressionQMC contextExpression;
	private final Positional positional;
	private transient ModelJANI model;

	JANITypeComplex(ContextExpressionQMC contextExpression, Positional positional) {
		this.contextExpression = contextExpression;
		this.positional = positional;
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
	public JANIType replace(Map<Expression, Expression> map) {
		return this;
	}

	public void checkExpressionConsistency(Map<Expression, Type> types) throws EPMCException {
	}

	@Override
	public TypeAlgebra toType() throws EPMCException {
        return contextExpression.getContextValueQMC().getTypeComplex();
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
		return UtilValue.newValue(toType(), 0);
	}
}
