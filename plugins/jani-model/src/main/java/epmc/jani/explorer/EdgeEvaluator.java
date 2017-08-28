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
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.EvaluatorExplicitBoolean;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expression.standard.simplify.UtilExpressionSimplify;
import epmc.jani.model.Action;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.Edge;
import epmc.jani.model.Variable;
import epmc.jani.value.TypeLocation;
import epmc.value.Value;

/**
 * Class to evaluate an automaton edge.
 * 
 * @author Ernst Moritz Hahn
 */
public final class EdgeEvaluator {
    public final static class Builder {
        private Edge edge;
        private Expression[] variables;
        private Map<Variable, Integer> variablesMap;
        private int locationVariable;
        private TypeLocation typeLocation;
        private Map<Expression, Expression> autVarToLocal;
        private Map<Action, Integer> actionNumbers;
        private ExpressionToType expressionToType;

        public Builder setActionNumbers(Map<Action,Integer> actionNumbers) {
            this.actionNumbers = actionNumbers;
            return this;
        }

        private Map<Action, Integer> getActionNumbers() {
            return actionNumbers;
        }

        public Builder setEdge(Edge edge) {
            this.edge = edge;
            return this;
        }

        private Edge getEdge() {
            return edge;
        }

        public Builder setVariables(Expression[] variables) {
            this.variables = variables;
            return this;
        }

        private Expression[] getVariables() {
            return variables;
        }

        public Builder setVariablesMap(Map<Variable, Integer> variablesMap) {
            this.variablesMap = variablesMap;
            return this;
        }

        private Map<Variable, Integer> getVariablesMap() {
            return variablesMap;
        }

        public Builder setLocationVariable(int locationVariable) {
            this.locationVariable = locationVariable;
            return this;
        }

        private int getLocationVariable() {
            return locationVariable;
        }

        public Builder setTypeLocation(TypeLocation typeLocation) {
            this.typeLocation = typeLocation;
            return this;
        }

        private TypeLocation getTypeLocation() {
            return typeLocation;
        }

        public Builder setAutVarToLocal(Map<Expression, Expression> autVarToLocal) {
            this.autVarToLocal = autVarToLocal;
            return this;
        }

        private Map<Expression, Expression> getAutVarToLocal() {
            return autVarToLocal;
        }

        public Builder setExpressionToType(ExpressionToType expressionToType) {
            this.expressionToType = expressionToType;
            return this;
        }

        private ExpressionToType getExpressionToType() {
            return expressionToType;
        }

        public EdgeEvaluator build() {
            return new EdgeEvaluator(this);
        }
    }

    /** Action of the edge. */
    private final int action;
    /** Guard (reference in {@link #evaluator}). */
    private final EvaluatorExplicitBoolean guardEval;
    /** Array of destination evaluators of destinations of edge. */
    private final DestinationEvaluator[] destinationEvaluators;
    private Value[] variableValues;
    /** Evaluator for the rate of the edge. */
    private EvaluatorExplicit rateEval;

    private EdgeEvaluator(Builder builder) {
        assert builder != null;
        assert builder.getEdge() != null;
        assert builder.getVariablesMap() != null;
        assert builder.getExpressionToType() != null;
        Map<Variable, Integer> variablesMap = builder.getVariablesMap();
        for (Entry<Variable, Integer> entry : variablesMap.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        Edge edge = builder.getEdge();
        Map<Expression, Expression> autVarToLocal = builder.getAutVarToLocal();
        Expression[] variables = builder.getVariables();
        action = builder.getActionNumbers().get(edge.getActionOrSilent());
        Destinations destinations = edge.getDestinations();
        Expression guardExpr = edge.getGuardExpressionOrTrue();
        guardExpr = edge.getModel().replaceConstants(guardExpr);
        guardExpr = UtilExpressionStandard.replace(guardExpr, autVarToLocal);
        guardExpr = UtilExpressionSimplify.simplify(builder.getExpressionToType(), guardExpr);
        guardEval = UtilEvaluatorExplicit.newEvaluatorBoolean(guardExpr, builder.getExpressionToType(), variables);
        destinationEvaluators = new DestinationEvaluator[destinations.size()];
        Expression rateExpr = edge.getRateExpression();
        if (rateExpr != null) {
            rateExpr = edge.getModel().replaceConstants(rateExpr);
            rateExpr = UtilExpressionStandard.replace(rateExpr, autVarToLocal);
            rateExpr = UtilExpressionSimplify.simplify(builder.getExpressionToType(), rateExpr);
            rateEval = UtilEvaluatorExplicit.newEvaluator(rateExpr, builder.getExpressionToType(), variables);
        }
        int destNr = 0;
        for (Destination destination : destinations) {
            destinationEvaluators[destNr] = new DestinationEvaluator.Builder()
                    .setDestination(destination)
                    .setVariables(variables)
                    .setVariableMap(variablesMap)
                    .setLocationVariable(builder.getLocationVariable())
                    .setAutVarToLocal(autVarToLocal)
                    .setTypeLocation(builder.getTypeLocation())
                    .setExpressionToType(builder.getExpressionToType())
                    .build();
            destNr++;
        }
    }

    /**
     * Evaluate guard of the edge.
     * Before doing so, the variable values of the expression evaluator used
     * must have been set.
     * 
     * @return {@code true} iff guard is fulfilled
     */
    boolean evaluateGuard() {
        return guardEval.evaluateBoolean(variableValues);
    }

    boolean hasRate() {
        return rateEval != null;
    }

    Value evaluateRate() {
        rateEval.evaluate(variableValues);
        return rateEval.getResultValue();
    }

    /**
     * Obtain the action of the edge of this evaluator.
     * 
     * @return action of the edge of this evaluator
     */
    int getAction() {
        return action;
    }

    /**
     * Get the number of destinations of the edge of this evaluator.
     * 
     * @return number of destinations of the edge of this evaluator
     */
    int getNumDestinations() {
        return destinationEvaluators.length;
    }

    /**
     * Obtain evaluator of the destination with the given number.
     * The destination number parameter must be nonnegative and strictly smaller
     * than the number of destinations of the edge this evaluator evaluates.
     * 
     * @param destinationNr number of evaluator to obtain
     * @return evaluator for destination with given number
     */
    DestinationEvaluator getDestinationEvaluator(int destinationNr) {
        assert destinationNr >= 0;
        assert destinationNr < destinationEvaluators.length;
        return destinationEvaluators[destinationNr];
    }

    public void setVariableValues(Value[] variableValues) {
        this.variableValues = variableValues;
    }
}
