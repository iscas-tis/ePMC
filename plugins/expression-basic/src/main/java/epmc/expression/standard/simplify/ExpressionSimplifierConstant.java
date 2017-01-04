package epmc.expression.standard.simplify;

import java.util.ArrayList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.Value;

public final class ExpressionSimplifierConstant implements ExpressionSimplifier {
    public final static String IDENTIFIER = "constant";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) throws EPMCException {
        assert expression != null;
        if (expression instanceof ExpressionLiteral) {
            return null;
        }
        if (UtilExpressionStandard.collectIdentifiers(expression).size() == 0) {
            return new ExpressionLiteral.Builder()
            		.setValue(evaluateValue(expressionToType, expression))
            		.build();
        }
        List<Expression> newChildren = new ArrayList<>();
        boolean simplified = false;
        for (Expression child : expression.getChildren()) {
            Expression childSimplified = simplify(expressionToType, child);
            if (childSimplified == null) {
                childSimplified = child;
            } else {
                simplified = true;
            }
            newChildren.add(childSimplified);
        }
        if (simplified) {
            return expression.replaceChildren(newChildren);
        } else {
            return null;
        }
    }

    private static Value evaluateValue(ExpressionToType expressionToType, Expression expression) throws EPMCException {
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression, expressionToType, new Expression[0]);
        return evaluator.evaluate();
    }
}
