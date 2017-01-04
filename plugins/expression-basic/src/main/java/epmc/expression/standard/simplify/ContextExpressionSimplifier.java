package epmc.expression.standard.simplify;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.ContextValue;

public final class ContextExpressionSimplifier {
    private final ExpressionSimplifier[] simplifiers;

    public ContextExpressionSimplifier(ContextValue contextValue) {
        Options options = contextValue.getOptions();
        Map<String,Class<? extends ExpressionSimplifier>> simplifiers =
                options.get(OptionsExpressionBasic.EXPRESSION_SIMPLIFIER_CLASS);
        this.simplifiers = new ExpressionSimplifier[simplifiers.size()];
        int simplifierNr = 0;
        for (Class<? extends ExpressionSimplifier> clazz : simplifiers.values()) {
            this.simplifiers[simplifierNr] = Util.getInstance(clazz);
            simplifierNr++;
        }
    }
    
    public Expression simplify(ExpressionToType expressionToType, Expression expression) throws EPMCException {
        Expression result = expression;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ExpressionSimplifier simplifier : simplifiers) {
                Expression simplified = simplifier.simplify(expressionToType, result);
                if (simplified != null) {
                    result = simplified;
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }
}
