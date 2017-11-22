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

import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

final class NodePropertyPlayerToState implements NodeProperty {
    private final GraphExplicit graph;
    private final NodeProperty playerProperty;
    private final ValueBoolean value;

    NodePropertyPlayerToState(GraphExplicit graph, NodeProperty player) {
        assert graph != null;
        assert player != null;
        this.graph = graph;
        this.playerProperty = player;
        this.value = TypeBoolean.get().newValue();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        Player player = playerProperty.getEnum(node);
        value.set(player == Player.ONE || player == Player.TWO);
        return value;
    }

    @Override
    public void set(int node, Value value) {
        assert value != null;
        assert false;
    }

    @Override
    public Type getType() {
        return value.getType();
    }
}
