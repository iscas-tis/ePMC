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

package epmc.automaton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

final class HanoiHeader {
	private final Map<String,Expression> ap2expr;
	private int numStates;
	private final BitSet startStates = new BitSetUnboundedLongArray();
	private final List<Expression> aps = new ArrayList<>();
	private int numAcc;
	
	HanoiHeader(Map<String,Expression> ap2expr) {
		assert ap2expr != null;
		this.ap2expr = ap2expr;
	}
	
	void setNumStates(int numStates) {
		this.numStates = numStates;
	}
	
	int getNumStates() {
		return numStates;
	}
	
	void setStartState(int startState) {
		startStates.set(startState);
	}

	BitSet getStartStates() {
		return startStates;
	}
	
	void addAP(String name) {
		assert name != null;
		assert ap2expr.containsKey(name);
		aps.add(ap2expr.get(name));
	}
	
	Expression numberToIdentifier(int number) {
		assert number >= 0;
		assert number < aps.size();
		return aps.get(number);
	}
	
	void setNumAcc(int numAcc) {
		this.numAcc = numAcc;
	}
	
	public int getNumAcc() {
		return numAcc;
	}
}
