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

package epmc.graph.explorer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.graph.StateSet;

/**
 * State set of an explorer.
 * 
 * @author Ernst Moritz Hahn
 */
final class StateSetExplorer <T extends ExplorerNode> implements StateSet {
    /** Explorer of which states of this set belong to. */
    private final Explorer explorer;
    /** States of this state set. */
    private final Set<T> nodes = new LinkedHashSet<>();

    /**
     * Construct new explorer state set.
     * None of the parameters may be {@code null} or contain {@code null}
     * entries. The states of the nodes parameter must all belong the the
     * explorer given by the explorer parameter.
     * 
     * @param explorer explorer to which nodes belong to
     * @param collection collection of which to construct nodes
     */
    @SuppressWarnings("unchecked")
    StateSetExplorer(Explorer explorer, Collection<T> collection) {
        assert explorer != null;
        assert collection != null;
        for (ExplorerNode node : collection) {
            assert node != null;
        }
        this.explorer = explorer;
        for (T node : collection) {
            this.nodes.add((T) node.clone());
        }
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void close() {
    }

    public boolean isSubsetOf(StateSet states) {
        assert states != null;
        assert states instanceof StateSetExplorer;
        StateSetExplorer other = (StateSetExplorer) states;
        assert explorer == other.explorer;
        return other.nodes.containsAll(nodes);
    }

    @Override
    public StateSet clone() {
        return new StateSetExplorer(explorer, nodes);
    }
}
