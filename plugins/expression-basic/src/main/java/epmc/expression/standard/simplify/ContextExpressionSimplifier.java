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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.expressionevaluator.ExpressionToType;
import epmc.options.Options;
import epmc.util.Util;

public final class ContextExpressionSimplifier {
    private final ExpressionSimplifier[] simplifiers;
    private final Set<Expression> couldntSimplify = new HashSet<>();

    public ContextExpressionSimplifier(ExpressionToType expressionToType,
            EvaluatorCache evaluatorCache) {
        Options options = Options.get();
        Map<String,Class<? extends ExpressionSimplifier.Builder>> simplifiers =
                options.get(OptionsExpressionBasic.EXPRESSION_SIMPLIFIER_CLASS);
        this.simplifiers = new ExpressionSimplifier[simplifiers.size()];
        int simplifierNr = 0;
        if (evaluatorCache == null) {
            evaluatorCache = new EvaluatorCache();
        }
        for (Class<? extends ExpressionSimplifier.Builder> clazz : simplifiers.values()) {
            this.simplifiers[simplifierNr] = Util.getInstance(clazz)
                    .setExpressionToType(expressionToType)
                    .setEvaluatorCache(evaluatorCache)
                    .setSimplifier(this)
                    .build();
            simplifierNr++;
        }
    }

    public Expression simplify(Expression expression) {
        if (couldntSimplify.contains(expression)) {
            return expression;
        }
        Expression result = expression;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ExpressionSimplifier simplifier : simplifiers) {
                Expression simplified = simplifier.simplify(result);
                if (simplified != null && !simplified.equals(result)) {
                    result = simplified;
                    changed = true;
                    break;
                }
            }
        }
        if (expression.equals(result)) {
            couldntSimplify.add(expression);
        }
        return result;
    }
}
