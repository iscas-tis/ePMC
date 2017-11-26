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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.UtilExpressionStandard;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
/**
 * Guarded command of a PRISM model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Command {
    /** label of command. use empty string if not synchronised, not null */
    private final Expression label;
    /** Guard of the guarded command. */
    private final Expression guard;
    /** The different stochastically chosen alternatives of the command. */
    private final ArrayList<Alternative> alternatives = new ArrayList<>();
    /** Source file position information about the guarded command. */
    private final Positional positional;
    /** Player controlling the guarded command. */
    private int player = -1;

    public Command(Expression label, Expression guard, List<Alternative> alternatives, Positional positional) {
        assert label != null;
        assert guard != null;
        assert alternatives != null;
        for (Alternative alternative : alternatives) {
            assert alternative != null;
        }
        this.label = label;
        this.guard = guard;
        this.alternatives.addAll(alternatives);
        this.positional = positional;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[" + label + "] ");
        builder.append(guard + " -> ");
        if (alternatives.isEmpty()) {
            // this corresponds to a transition with no updates; self-loop
            // represented in prism as "true" update
            builder.append("true");
        }

        int altNr = 0;
        for (Alternative alternative : alternatives) {
            builder.append(alternative);
            if (altNr < alternatives.size() - 1) {
                builder.append(" + ");
            }
            altNr++;
        }
        return builder.toString();
    }

    Expression getLabel() {
        return label;
    }

    public Expression getAction() {
        return label;
    }

    public Expression getGuard() {
        return guard;
    }

    public List<Alternative> getAlternatives() {
        return Collections.unmodifiableList(alternatives);
    }

    Command replaceFormulas(Map<Expression,Expression> formulas) {
        assert formulas != null;
        for (Entry<Expression, Expression> entry : formulas.entrySet()) {
            assert entry.getKey() != null;
            assert entry.getValue() != null;
        }
        for (Entry<Expression, Expression> entry : formulas.entrySet()) {
            if (!(entry.getKey() instanceof ExpressionIdentifier)) {
                throw new IllegalArgumentException();
            }
        }
        Expression newGuard = guard;
        newGuard = UtilExpressionStandard.replace(newGuard, formulas);
        ArrayList<Alternative> newAlternatives = new ArrayList<>();
        for (Alternative alternative : alternatives) {
            newAlternatives.add(alternative.replaceFormulas(formulas));
        }
        return new Command(this.label, newGuard, newAlternatives, positional);
    }

    void setPlayer(int player) {
        assert player >= -1;
        assert this.player == -1;
        this.player = player;
    }

    public int getPlayer() {
        return player;
    }
}
