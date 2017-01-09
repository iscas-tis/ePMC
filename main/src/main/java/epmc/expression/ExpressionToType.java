package epmc.expression;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Type;

/**
 * Interface to obtain type of a certain expression.
 * The interface is intended to obtain the type of expressions for which the
 * type cannot be derived from the expression itself.
 * One example for this is e.g. an identifier.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ExpressionToType {
    // TODO
    ContextValue getContextValue();
    /**
     * Get type of the expression.
     * If the type is not stored or unknown, {@code null} should be returned.
     * 
     * @param expression expression to get type of
     * @return type of the expression, or {@code null}
     * @throws EPMCException thrown in case of problems
     */
    Type getType(Expression expression) throws EPMCException;
}
