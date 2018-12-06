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

import static epmc.error.UtilError.ensure;

import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.simplify.ContextExpressionSimplifier;
import epmc.expressionevaluator.ExpressionToType;
import epmc.jani.model.Destination;
import epmc.jani.model.Variable;
import epmc.jani.value.TypeLocation;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;

/**
 * Evaluator for edge destinations.
 * 
 * @author Ernst Moritz Hahn
 */
public final class DestinationEvaluator {
    public final static class Builder {
        private Destination destination;
        private Expression[] variables;
        private Map<Variable, Integer> variableMap;
        private int locationVariable;
        private Map<Expression, Expression> autVarToLocal;
        private TypeLocation typeLocation;
        private ExpressionToType expressionToType;
        private ContextExpressionSimplifier simplifier;
        private EvaluatorCache evaluatorCache;

        public Builder setDestination(Destination destination) {
            this.destination = destination;
            return this;
        }

        private Destination getDestination() {
            return destination;
        }

        public Builder setVariables(Expression[] variables) {
            this.variables = variables;
            return this;
        }

        private Expression[] getVariables() {
            return variables;
        }

        public Builder setVariableMap(Map<Variable, Integer> variableMap) {
            this.variableMap = variableMap;
            return this;
        }

        private Map<Variable, Integer> getVariableMap() {
            return variableMap;
        }

        public Builder setLocationVariable(int locationVariable) {
            this.locationVariable = locationVariable;
            return this;
        }

        private int getLocationVariable() {
            return locationVariable;
        }

        public Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal) {
            this.autVarToLocal = autVarToLocal;
            return this;
        }

        private Map<Expression, Expression> getAutVarToLocal() {
            return autVarToLocal;
        }

        public Builder setTypeLocation(TypeLocation typeLocation) {
            this.typeLocation = typeLocation;
            return this;
        }

        private TypeLocation getTypeLocation() {
            return typeLocation;
        }

        public Builder setExpressionToType(ExpressionToType expressionToType) {
            this.expressionToType = expressionToType;
            return this;
        }
        
        public ExpressionToType getExpressionToType() {
            return expressionToType;
        }

        public Builder setSimplifier(ContextExpressionSimplifier simplifier) {
            this.simplifier = simplifier;
            return this;
        }

        public ContextExpressionSimplifier getSimplifier() {
            return simplifier;
        }
        
        public Builder setEvaluatorCache(EvaluatorCache evaluatorCache) {
            this.evaluatorCache = evaluatorCache;
            return this;
        }

        public DestinationEvaluator build() {
            return new DestinationEvaluator(this);
        }
    }

    /** Probability (reference in {@link #evaluator}). */
    private final EvaluatorExplicit probability;
    /** Location to which the destination moves. */
    private final int location;
    /** Assignments performed by this evaluator. */
    private final AssignmentsEvaluator assignments;
    /** Zero real. */
    private final ValueAlgebra zeroWeight;
    /** Number of location variable in the automaton evaluator belongs to. */
    private final int locationVariable;
    private final OperatorEvaluator ge;
    private final ValueBoolean cmp = TypeBoolean.get().newValue();
    private final ValueAlgebra probabilityV = TypeWeightTransition.get().newValue();
    private final OperatorEvaluator setProbability;

    private DestinationEvaluator(Builder builder) {
        assert builder != null;
        assert builder.getDestination() != null;
        assert builder.getExpressionToType() != null;
        this.locationVariable = builder.getLocationVariable();
        Destination destination = builder.getDestination();
        Map<Expression, Expression> autVarToLocal = builder.getAutVarToLocal();
        Map<Variable, Integer> variableMap = builder.getVariableMap();
        Expression[] variables = builder.getVariables();
        Expression probExpr = destination.getProbabilityExpressionOrOne();
        probExpr = destination.getModel().replaceConstants(probExpr);
        probExpr = UtilExpressionStandard.replace(probExpr, autVarToLocal);
        probExpr = builder.getSimplifier().simplify(probExpr);
        probability = UtilEvaluatorExplicit.newEvaluator(null, probExpr, variables, builder.evaluatorCache, builder.getExpressionToType());
        TypeLocation typeLocation = builder.getTypeLocation();

        location = typeLocation.getNumber(destination.getLocation());
        assignments = new AssignmentsEvaluator.Builder()
                .setAssignments(destination.getAssignmentsOrEmpty())
                .setAutVarToLocal(autVarToLocal)
                .setExpressionToType(builder.getExpressionToType())
                .setVariableMap(variableMap)
                .setVariables(variables)
                .setEvaluatorCache(builder.evaluatorCache)
                .setSimplifier(builder.simplifier)
                .build();
        zeroWeight = UtilValue.newValue(TypeWeightTransition.get(), 0);
        ge = ContextValue.get().getEvaluatorOrNull(OperatorGe.GE, TypeWeightTransition.get(), TypeWeightTransition.get());
        setProbability = ContextValue.get().getEvaluator(OperatorSet.SET, probability.getResultValue().getType(), TypeWeightTransition.get());
    }

    ValueAlgebra evaluateProbability(NodeJANI node) {
        probability.setValues(node.getValues());
        probability.evaluate();
        ValueAlgebra result = ValueAlgebra.as(probability.getResultValue());
        /* make sure that we return values of correct type to avoid problems 
         * with operator evaluators. */
        setProbability.apply(probabilityV, result);
        if (ge != null) {
            ge.apply(cmp, probabilityV, zeroWeight);
            ensure(cmp.getBoolean(), ProblemsJANIExplorer.JANI_EXPLORER_NEGATIVE_WEIGHT);
        }
        return probabilityV;
    }

    /**
     * Assign to an explorer node the effect of the destination.
     * The parameter may not be {@code null}.
     * 
     * @param toNode node to assign effect to
     */
    void assignTo(NodeJANI fromNode, NodeJANI toNode) {
        assert toNode != null;
        assignments.apply(fromNode, toNode);
        if (locationVariable != -1) {
            toNode.setVariable(locationVariable, location);
        }
    }
}
