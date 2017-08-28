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

import java.util.Map;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;

//Notice: objects of this class are immutable by purpose.
//Do not modify the class to make them mutable.
public final class StateReward implements Reward {
    private Positional positional;
    private final Expression guard;
    private final Expression value;

    public StateReward(Expression guard, Expression value, Positional positional) {
        this.positional = positional;
        assert guard != null;
        assert value != null;
        this.guard = guard;
        this.value = value;
    }

    public Expression getGuard() {
        return guard;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return guard + " : " + value;
    }

    @Override
    public Reward replace(Map<Expression, Expression> map) {
        Expression newGuard = UtilExpressionStandard.replace(guard, map);
        Expression newValue = UtilExpressionStandard.replace(value, map);
        return new StateReward(newGuard, newValue, getPositional());
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
}
