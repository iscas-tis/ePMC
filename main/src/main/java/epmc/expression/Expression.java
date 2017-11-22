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

package epmc.expression;

import java.util.List;

import epmc.error.Positional;

/**
 * Base interface for expressions.
 * Expressions are intended to be used for (at least) two purposes.
 * On the one hand, they are intended to be used to form the base of expressions
 * to be analysed.
 * On the other hand, they are intended to be used as parts of models, such as
 * for specifying guards, the set of initial states, etc.
 * <br/>
 * Note: Do not introduce dependencies of specific expression types to
 * values (or types, etc.). Evaluating expressions should be performed by
 * evaluators.
 * <br/>
 * I'm aware that such dependencies currently exist, trying to get rid of
 * them.
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

    Expression replacePositional(Positional positional);

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
}
