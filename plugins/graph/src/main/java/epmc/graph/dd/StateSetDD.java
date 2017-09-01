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

package epmc.graph.dd;

import java.io.Closeable;

import epmc.dd.DD;
import epmc.graph.LowLevel;
import epmc.graph.StateSet;

public final class StateSetDD implements Closeable, Cloneable, StateSet {
    private final DD statesDD;
    private int refs = 1;
    private LowLevel lowLevel;

    // note: consumes arguments statesExplicit and statesDD
    public StateSetDD(LowLevel lowLevel, DD statesDD) {
        this.lowLevel = lowLevel;
        this.statesDD = statesDD;
    }

    @Override
    public StateSetDD clone() {
        refs++;
        return this;
    }

    @Override
    public void close() {
        if (closed()) {
            return;
        }
        refs--;
        if (refs > 0) {
            return;
        }
        if (statesDD != null) {
            statesDD.dispose();
        }
    }

    @Override
    public int size() {
        assert !closed();
        GraphDD graphDD = (GraphDD) lowLevel;
        return statesDD.countSat(graphDD.getPresCube()).intValue();
    }

    @Override
    public boolean equals(Object obj) {
        assert !closed();
        assert obj != null;
        if (!(obj instanceof StateSetDD)) {
            return false;
        }
        StateSetDD other = (StateSetDD) obj;
        if ((this.statesDD == null) != (other.statesDD == null)) {
            return false;
        }
        if (statesDD != null && !this.statesDD.equals(other.statesDD)) {
            return false;
        }        
        return true;
    }

    public boolean isSubsetOf(StateSet states) {
        assert !closed();
        assert states != null;
        assert states instanceof StateSetDD;
        StateSetDD other = (StateSetDD) states;
        return statesDD.andNot(other.statesDD).isFalseWith();
    }

    public DD getStatesDD() {
        return statesDD;
    }

    private boolean closed() {
        return refs == 0;
    }

}
