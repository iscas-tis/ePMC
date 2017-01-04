package epmc.param.expression;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionOperator;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
import epmc.value.Operator;

public final class ExpressionToFunction {
	private final TypeFunction type;

	ExpressionToFunction(TypeFunction type) {
		assert type != null;
		this.type = type;
	}
	
	public ValueFunction expressionToFunction(Expression expression) throws EPMCException {
		assert expression != null;
		ValueFunction result = this.type.newValue();
		if (expression instanceof ExpressionIdentifier) {
			ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
			result.setParameter(expressionIdentifier.getName());
		} else if (expression instanceof ExpressionOperator) {
			List<ValueFunction> operands = new ArrayList<>();
			ExpressionOperator expressionOperator = (ExpressionOperator) expression;
			for (Expression operand : expressionOperator.getOperands()) {
				operands.add(expressionToFunction(operand));
			}
			Operator operator = expressionOperator.getOperator();
			operator.apply(result, operands.toArray(new ValueFunction[operands.size()]));
		} else {
			assert false;
		}
		return result;
	}
}
