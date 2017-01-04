package epmc.expression;

import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.value.Type;

/**
 * Base interface for expressions.
 * Expressions are intended to be used for (at least) two purposes.
 * On the one hand, they are intended to be used to form the base of expressions
 * to be analysed.
 * On the other hand, they are intended to be used as parts of models, such as
 * for specifying guards, the set of initial states, etc.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Expression {
    // TODO check whether this method is indeed needed for all expression types
    /**
     * Get the list of subexpressions of this expression.
     * This method is intended to 
     * 
     * @return list of subexpressions of this expression
     */
    List<Expression> getChildren();

    // TODO check whether this method is indeed needed for all expression types
    /**
     * Return expression in which subexpressions have been replaced.
     * Calls to this method have to be sure the the expression types and other
     * restrictions of the children are still fulfilled.
     * The list of new children must not be {@code null} and must not contain
     * {@code null} elements.
     * 
     * @param newChildren list of children to replace with
     * @return expression in which expression
     */
    Expression replaceChildren(List<Expression> newChildren);
    
    // TODO it might later be useful to attach more general information to
    // expressions which do not influence equality of two expressions. For
    // example, for user feedback it might be useful to store the exact string
    // from which the expression was created during parsing.
    
    /**
     * Obtain positional information if available.
     * These positional information usually refer to the position in the input
     * file the expression was constructed from.
     * If no positional information are available, e.g. because this information
     * was not set while reading the expression from the input file or because
     * it was constructed later on without using position information, this
     * function will return {@code null}.
     * 
     * @return positional information if available
     */
    Positional getPositional();
    
    // TODO might make sense to modify this method in such a way that it gets
    // a map from expressions to their type. The reason is that the types change
    // often after the model has been constructed, and using a map there would
    // make it more transparent from where the type is actually obtained.
    
    // TODO adapt documentation
    /**
     * Get (or compute) type of the expression.
     * 
     * @return type of expression
     * @throws EPMCException throw in case of problems
     */
    Type getType(ExpressionToType expressionToType) throws EPMCException;
}
