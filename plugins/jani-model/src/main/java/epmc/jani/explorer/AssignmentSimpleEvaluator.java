/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani.explorer;

import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.simplify.ContextExpressionSimplifier;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.Assignment;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Variable;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
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
        private ContextExpressionSimplifier simplifier;
        private EvaluatorCache evaluatorCache;

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
        
        public Builder setSimplifier(ContextExpressionSimplifier simplifier) {
            this.simplifier = simplifier;
            return this;
        }

        private ContextExpressionSimplifier getSimplifier() {
            return simplifier;
        }
        
        @Override
        public Builder setEvaluatorCache(EvaluatorCache evaluatorCache) {
            this.evaluatorCache = evaluatorCache;
            return this;
        }

        @Override
        public AssignmentSimpleEvaluator build() {
            assert canHandle();
            return new AssignmentSimpleEvaluator(this);
        }

    }

    private final int variable;
    private final EvaluatorExplicit expression;
    private final OperatorEvaluator set;
    private final Value value;

    private AssignmentSimpleEvaluator(Builder builder) {
        assert builder != null;
        Map<Variable, Integer> variableMap = builder.getVariableMap();
        AssignmentSimple entry = builder.getAssignment();
        Map<Expression, Expression> autVarToLocal = builder.getAutVarToLocal();
        Expression[] variables = builder.getVariables();
        variable = variableMap.get(entry.getRef());
        Expression assignment = entry.getValue();
        assignment = entry.getModel().replaceConstants(assignment);
        assignment = UtilExpressionStandard.replace(assignment, autVarToLocal);
        value = entry.getRef().getType().toType().newValue();
//                assignment.getType(builder.getExpressionToType()).newValue();
        assignment = builder.getSimplifier().simplify(assignment);
//                UtilExpressionSimplify.simplify(builder.getExpressionToType(), assignment);
        expression = UtilEvaluatorExplicit.newEvaluator(null, assignment, variables, builder.evaluatorCache, builder.getExpressionToType());
        set = ContextValue.get().getEvaluator(OperatorSet.SET, expression.getResultValue().getType(), value.getType());
    }

    @Override
    public void apply(NodeJANI node, NodeJANI successor) {
        assert node != null;
        assert successor != null;
        Value[] variableValues = node.getValues();
        expression.setValues(variableValues);
        expression.evaluate();
        set.apply(value, expression.getResultValue());
        successor.setVariable(variable, value);
    }
}
