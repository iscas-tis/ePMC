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

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.Value;

final class NodePropertyRestricted implements NodeProperty {
    private final GraphExplicitRestricted graph;
    private final NodeProperty original;

    NodePropertyRestricted(GraphExplicitRestricted graph, NodeProperty original) {
        assert graph != null;
        assert original != null;
        this.graph = graph;
        this.original = original;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        return original.get(node);
    }

    @Override
    public void set(int node, Value value) {
        assert false;
    }

    @Override
    public Type getType() {
        return original.getType();
    }
}
