package epmc.constraintsolver;

import epmc.value.Value;

public final class LinearExpression {
	private final int[] variables;
	private final Value[] rows;
	private final ConstraintType type;
	
	public LinearExpression(int[] variables, Value[] rows, ConstraintType type) {
		this.variables = variables;
		this.rows = rows;
		this.type = type;
	}
}
