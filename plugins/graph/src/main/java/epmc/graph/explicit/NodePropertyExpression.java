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

package epmc.graph.explicit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.Type;
import epmc.value.Value;

public final class NodePropertyExpression implements NodeProperty {
    private final NodeProperty[] variableNodeProperties;
    private final EvaluatorExplicit evaluator;
    private final GraphExplicit graph;
    private final Value[] values;

    public NodePropertyExpression(GraphExplicit graph, Expression expression) {
        assert expression != null;
        this.graph = graph;
        Expression[] variables = computeVariables(graph, expression).toArray(new Expression[0]);
        variableNodeProperties = computeVariableNodeProperties(variables);
        values = new Value[variables.length];
        evaluator = UtilEvaluatorExplicit.newEvaluator(expression, graph, variables);
    }

    private NodeProperty[] computeVariableNodeProperties(
            Expression[] variables) {
        NodeProperty[] variableNodeProperties = new NodeProperty[variables.length];
        for (int variableNr = 0; variableNr < variables.length; variableNr++) {
            Expression variable = variables[variableNr];
            NodeProperty nodeProperty = graph.getNodeProperty(variable);
            variableNodeProperties[variableNr] = nodeProperty;
        }
        return variableNodeProperties;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        for (int i = 0; i < variableNodeProperties.length; i++) {
            values[i] = variableNodeProperties[i].get(node);
        }
        evaluator.setValues(values);
        evaluator.evaluate();
        return evaluator.getResultValue();
    }

    @Override
    public void set(int node, Value value) {
    }

    @Override
    public Type getType() {
        return evaluator.getType();
    }

    private static Set<Expression> computeVariables(GraphExplicit original, Expression expression) {
        if (original.getNodeProperties().contains(expression)) {
            return Collections.singleton(expression);
        } else {
            Set<Expression> result = new HashSet<>();
            for (Expression child : expression.getChildren()) {
                result.addAll(computeVariables(original, child));
            }
            return result;
        }
    }

}
