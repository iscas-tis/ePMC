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

package epmc.prism.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.UtilExpressionStandard;

/**
 * A single stochastically chosen alternative of a {@link Command}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Alternative {
    /** Weight (rate or probability) of this command. */
    private final Expression weight;
    /** Effect of this command.
     * Maps each variable which is changed to an expression about its new value.
     * */
    private final Map<Expression,Expression> effect;
    /** Position information about the alternative. */
    private final Positional positional;

    public Alternative(Expression weight, Map<Expression,Expression> effect, Positional positional) {
        assert weight != null;
        assert effect != null;
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            assert entry.getKey() instanceof ExpressionIdentifier;
        }
        this.weight = weight;
        this.effect = new HashMap<>();
        this.effect.putAll(effect);
        this.positional = positional;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(weight + " : ");
        if (effect.isEmpty()) {
            builder.append("true");
        }
        int effectNr = 0;
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            builder.append("(");
            builder.append(entry.getKey() + "'=" + entry.getValue());
            builder.append(")");
            if (effectNr < effect.size() - 1) {
                builder.append(" & ");
            }
            effectNr++;
        }
        if (positional != null) {
            builder.append(" (" + positional + ")");
        }

        return builder.toString();
    }

    public Expression getWeight() {
        return weight;
    }

    public Map<Expression,Expression> getEffect() {
        return Collections.unmodifiableMap(effect);
    }

    Alternative replaceFormulas(Map<Expression,Expression> formulas) {
        Expression newWeight = UtilExpressionStandard.replace(this.weight, formulas);
        Map<Expression,Expression> newEffects = new HashMap<>();
        for (Entry<Expression,Expression> entry : effect.entrySet()) {
            Expression newRhs = entry.getValue();
            newRhs = UtilExpressionStandard.replace(newRhs, formulas);
            newEffects.put(entry.getKey(), newRhs);
        }
        return new Alternative(newWeight, newEffects, positional);
    }

    public Positional getPositional() {
        return positional;
    }
}
