package epmc.graph.explicit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.Type;
import epmc.value.Value;

public final class NodePropertyExpression implements NodeProperty {
    private final NodeProperty[] variableNodeProperties;
    private final EvaluatorExplicit evaluator;
    private final GraphExplicit graph;
    private final Type type;
    private final Value[] values;

    public NodePropertyExpression(GraphExplicit graph, Expression expression) throws EPMCException {
        assert expression != null;
        this.graph = graph;
        type = expression.getType(graph);
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
    public Value get() throws EPMCException {
        for (int i = 0; i < variableNodeProperties.length; i++) {
            values[i] = variableNodeProperties[i].get();
        }
        evaluator.evaluate(values);
        return evaluator.getResultValue();
    }

    @Override
    public void set(Value value) throws EPMCException {
    }

    @Override
    public Type getType() {
        return type;
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
