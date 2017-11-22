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

package epmc.automata.determinisation;

import epmc.util.BitSet;

final class ScheweCacheKey implements Cloneable {
    AutomatonScheweState state;
    BitSet guards;

    @Override
    public int hashCode() {
        int hash = 0;
        hash = state.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = guards.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ScheweCacheKey)) {
            return false;
        }
        ScheweCacheKey other = (ScheweCacheKey) obj;
        if (!state.equals(other.state)) {
            return false;
        }
        if (!guards.equals(other.guards)) {
            return false;
        }
        return true;
    }

    @Override
    protected ScheweCacheKey clone() {
        ScheweCacheKey result = new ScheweCacheKey();
        result.state = state;
        result.guards = guards.clone();
        return result;
    }
}
