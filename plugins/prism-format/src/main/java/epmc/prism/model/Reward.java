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

//Notice: objects of this interface are immutable by purpose.
//Do not modify the interface to make them mutable.
public interface Reward {
    Positional getPositional();

    default boolean isStateReward() {
        return this instanceof StateReward;
    }

    default StateReward asStateReward() {
        assert isStateReward();
        return (StateReward) this;
    }

    default boolean isTransitionReward() {
        return this instanceof TransitionReward;
    }

    default TransitionReward asTransitionReward() {
        assert isTransitionReward();
        return (TransitionReward) this;
    }

    Reward replace(Map<Expression,Expression> map);
}
