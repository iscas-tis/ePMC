package epmc.expression.standard;

import epmc.expression.Expression;

// TODO move to expression-basic as soon as possible

public interface ExpressionIdentifier extends ExpressionPropositional {
    static boolean isIdentifier(Object expression) {
        return expression instanceof ExpressionIdentifier;
    }
    
    static ExpressionIdentifier asIdentifier(Expression expression) {
        if (isIdentifier(expression)) {
            return (ExpressionIdentifier) expression;
        } else {
            return null;
        }
    }
    
    @Override
    default boolean isPropositional() {
        return true;
    }
}
