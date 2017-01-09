package epmc.jani.explorer;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.simplify.UtilExpressionSimplify;
import epmc.jani.model.Assignment;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Variable;
import epmc.value.Value;

public final class AssignmentSimpleEvaluator implements AssignmentEvaluator {
	public final static String IDENTIFIER = "simple";
	
	// Note: looks a bit over-engineered now. However, these code structure
	// serves as a means to later on support more complex classes of assignments
	public final static class Builder implements AssignmentEvaluator.Builder {
		private Assignment assignment;
		private Map<Variable, Integer> variableMap;
		private Map<Expression, Expression> autVarToLocal;
		private Expression[] variables;
		private ExpressionToType expressionToType;

		@Override
		public Builder setAssignment(Assignment assignment) {
			this.assignment = assignment;
			return this;
		}
		
		private AssignmentSimple getAssignment() {
			return (AssignmentSimple) assignment;
		}
		
		@Override
		public Builder setVariableMap(Map<Variable, Integer> variableMap) {
			this.variableMap = variableMap;
			return this;
		}
		
		private Map<Variable, Integer> getVariableMap() {
			return variableMap;
		}
		
		@Override
		public Builder setVariables(Expression[] variables) {
			this.variables = variables;
			return this;
		}

		private Expression[] getVariables() {
			return variables;
		}
		
		@Override
		public Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal) {
			this.autVarToLocal = autVarToLocal;
			return this;
		}

		private Map<Expression, Expression> getAutVarToLocal() {
			return autVarToLocal;
		}
		
		@Override
		public boolean canHandle() {
			if (!(assignment instanceof AssignmentSimple)) {
				return false;
			}
			AssignmentSimple assignmentSimple = (AssignmentSimple) assignment;
			if (!variableMap.containsKey(assignmentSimple.getRef())) {
				return false;
			}
			return true;
		}
		
		@Override
		public Builder setExpressionToType(ExpressionToType expressionToType) {
			this.expressionToType = expressionToType;
			return this;
		}
		
		private ExpressionToType getExpressionToType() {
			return expressionToType;
		}

		@Override
		public AssignmentSimpleEvaluator build() throws EPMCException {
			assert canHandle();
			return new AssignmentSimpleEvaluator(this);
		}
	}

	private final int variable;
	private final EvaluatorExplicit expression;
	
	private AssignmentSimpleEvaluator(Builder builder) throws EPMCException {
		assert builder != null;
		Map<Variable, Integer> variableMap = builder.getVariableMap();
		AssignmentSimple entry = builder.getAssignment();
		Map<Expression, Expression> autVarToLocal = builder.getAutVarToLocal();
		Expression[] variables = builder.getVariables();
		variable = variableMap.get(entry.getRef());
		Expression assignment = entry.getValue();
		assignment = entry.getModel().replaceConstants(assignment);
		assignment = UtilExpressionStandard.replace(assignment, autVarToLocal);
		assignment = UtilExpressionSimplify.simplify(builder.getExpressionToType(), assignment);
		expression = UtilEvaluatorExplicit.newEvaluator(assignment, builder.getExpressionToType(), variables);
	}
	
	@Override
	public void apply(NodeJANI node, NodeJANI successor) throws EPMCException {
		assert node != null;
		assert successor != null;
		Value[] variableValues = node.getValues();
		expression.evaluate(variableValues);
		successor.setVariable(variable, expression.getResultValue());
	}
}
