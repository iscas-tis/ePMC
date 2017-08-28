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

package epmc.expression.standard.simplify;

import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.util.Util;

public final class ContextExpressionSimplifier {
    private final ExpressionSimplifier[] simplifiers;

    public ContextExpressionSimplifier() {
        Options options = Options.get();
        Map<String,Class<? extends ExpressionSimplifier>> simplifiers =
                options.get(OptionsExpressionBasic.EXPRESSION_SIMPLIFIER_CLASS);
        this.simplifiers = new ExpressionSimplifier[simplifiers.size()];
        int simplifierNr = 0;
        for (Class<? extends ExpressionSimplifier> clazz : simplifiers.values()) {
            this.simplifiers[simplifierNr] = Util.getInstance(clazz);
            simplifierNr++;
        }
    }
    
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
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
