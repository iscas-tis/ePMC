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

package epmc.expressionevaluator;

import epmc.expression.Expression;
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
    /**
     * Get type of the expression.
     * If the type is not stored or unknown, {@code null} should be returned.
     * 
     * @param expression expression to get type of
     * @return type of the expression, or {@code null}
     */
    Type getType(Expression expression);
}
