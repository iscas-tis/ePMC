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

package epmc.coalition.explicit;

import epmc.automaton.AutomatonParityLabel;

/**
 * Parity automaton label implementation used for coalition solvers.
 * 
 * @author Ernst Moritz Hahn
 */
final class SettableParityLabel implements AutomatonParityLabel {
    /** Priority of the label. */
    private final int priority;

    /**
     * Construct new settable parity label.
     * The priority parameter must be nonnegative.
     * 
     * @param priority color to set for label
     */
    SettableParityLabel(int priority) {
        assert priority >= 0;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
