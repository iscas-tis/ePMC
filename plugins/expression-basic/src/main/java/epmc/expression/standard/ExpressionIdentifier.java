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

package epmc.expression.standard;

import epmc.expression.Expression;

// TODO move to expression-basic as soon as possible

public interface ExpressionIdentifier extends ExpressionPropositional {
    static boolean is(Object expression) {
        return expression instanceof ExpressionIdentifier;
    }

    static ExpressionIdentifier as(Expression expression) {
        if (is(expression)) {
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
