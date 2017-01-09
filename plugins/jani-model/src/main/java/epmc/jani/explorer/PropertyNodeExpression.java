package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.value.Type;
import epmc.value.Value;

final class PropertyNodeExpression implements ExplorerNodeProperty {
	private final ExplorerJANI explorer;
	private final EvaluatorExplicit evaluator;
	private final Type type;
	private final Value[] values;

	PropertyNodeExpression(ExplorerJANI explorer, Expression[] identifiers, Expression expression, Type type) throws EPMCException {
		assert explorer != null;
		assert expression != null;
		expression = UtilExpressionStandard.replace(expression, explorer.getModel().getConstants());
		this.explorer = explorer;
		this.evaluator = UtilEvaluatorExplicit.newEvaluator(expression, explorer, identifiers);
		this.type = type;
		this.values = new Value[identifiers.length];
	}
	
	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public Value get() throws EPMCException {
		evaluator.evaluate(values);
		return evaluator.getResultValue();
	}

	public void setVariableValues(Value[] values) {
		for (int valueNr = 0; valueNr < values.length; valueNr++) {
			this.values[valueNr] = values[valueNr];
		}
	}
	
	@Override
	public Type getType() {
		return type;
	}
}
