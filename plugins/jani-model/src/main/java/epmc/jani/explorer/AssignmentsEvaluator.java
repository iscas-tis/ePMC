package epmc.jani.explorer;

import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.simplify.ContextExpressionSimplifier;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.Assignment;
import epmc.jani.model.Assignments;
import epmc.jani.model.Variable;
import epmc.options.Options;
import epmc.util.Util;

public final class AssignmentsEvaluator {
    public final static class Builder {
        private Assignments assignments;
        private Map<Variable, Integer> variableMap;
        private Expression[] variables;
        private Map<Expression, Expression> autVarToLocal;
        private ExpressionToType expressionToType;
        private ContextExpressionSimplifier simplifier;
        private EvaluatorCache evaluatorCache;

        public Builder setAssignments(Assignments assignments) {
            this.assignments = assignments;
            return this;
        }

        public Builder setVariableMap(Map<Variable, Integer> variableMap) {
            this.variableMap = variableMap;
            return this;
        }

        public Builder setVariables(Expression[] variables) {
            this.variables = variables;
            return this;
        }

        public Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal) {
            this.autVarToLocal = autVarToLocal;
            return this;
        }

        public Builder setExpressionToType(ExpressionToType expressionToType) {
            this.expressionToType = expressionToType;
            return this;
        }

        public Builder setSimplifier(ContextExpressionSimplifier simplifier) {
            this.simplifier = simplifier;
            return this;
        }
        
        public Builder setEvaluatorCache(EvaluatorCache evaluatorCache) {
            this.evaluatorCache = evaluatorCache;
            return this;
        }
        
        public AssignmentsEvaluator build() {
            return new AssignmentsEvaluator(this);		
        }

    }

    private final AssignmentEvaluator[] evaluators;

    private AssignmentsEvaluator(Builder builder) {
        assert builder != null;
        evaluators = new AssignmentEvaluator[builder.assignments.size()];
        int index = 0;
        for (Assignment assignment : builder.assignments) {
            evaluators[index] = newAssignmentEvaluator(builder.evaluatorCache, builder.autVarToLocal, builder.variableMap, builder.variables, assignment, builder.expressionToType, builder.simplifier);
            index++;
        }
    }

    private static AssignmentEvaluator newAssignmentEvaluator(EvaluatorCache evaluatorCache, Map<Expression, Expression> autVarToLocal, Map<Variable, Integer> variableMap, Expression[] variables, Assignment assignment, ExpressionToType expressionToType, ContextExpressionSimplifier simplifier) {
        assert assignment != null;
        Options options = Options.get();
        Map<String,Class<? extends AssignmentEvaluator.Builder>> assignmentEvaluators = options.get(OptionsJANIExplorer.JANI_EXPLORER_ASSIGNMENT_EVALUATOR_CLASS);
        for (Class<? extends AssignmentEvaluator.Builder> entry : assignmentEvaluators.values()) {
            AssignmentEvaluator.Builder builder = Util.getInstance(entry);
            builder.setAssignment(assignment)
            .setVariables(variables)
            .setVariableMap(variableMap)
            .setAutVarToLocal(autVarToLocal)
            .setExpressionToType(expressionToType)
            .setSimplifier(simplifier)
            .setEvaluatorCache(evaluatorCache);
            if (builder.canHandle()) {
                return builder.build();
            }
        }
        return null;
    }

    public void apply(NodeJANI node, NodeJANI successor) {
        assert node != null;
        assert successor != null;
        for (AssignmentEvaluator evaluator : evaluators) {
            evaluator.apply(node, successor);
        }
    }
}
