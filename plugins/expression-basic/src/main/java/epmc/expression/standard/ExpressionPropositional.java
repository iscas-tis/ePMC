package epmc.expression.standard;

import epmc.expression.Expression;

//TODO move to expression-basic as soon as possible

public interface ExpressionPropositional extends Expression {
    boolean isPropositional();
    
    static boolean isPropositional(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionPropositional)) {
            return false;
        }
        ExpressionPropositional expressionPropositional = (ExpressionPropositional) expression;
        return expressionPropositional.isPropositional();
    }
}
