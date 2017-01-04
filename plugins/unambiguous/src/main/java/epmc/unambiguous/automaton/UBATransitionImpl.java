package epmc.unambiguous.automaton;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.value.Value;

final class UBATransitionImpl implements UBATransition {
	
	private Expression expression;
	private BitSet labeling;
	private Value result;
	
	public UBATransitionImpl(Expression expr, BitSet lab) {
		this.expression = expr;
		this.labeling = lab;
	}

	@Override
	public Expression getExpression() {
		return expression;
	}

	@Override
	public BitSet getLabeling() {
		return labeling;
	}

	@Override
	public boolean guardFulfilled() throws EPMCException {
		return result.getBoolean();
	}

	@Override
	public void setResult(Value result) {
		this.result = result;
	}
	
	public String toString() {
		return expression + " " + labeling.toString();
	}

}
