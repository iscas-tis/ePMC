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

package epmc.graph.explicit.induced;

import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.value.Type;
import epmc.value.Value;

final class EdgePropertyInduced implements EdgeProperty {
    private final GraphExplicitInduced graph;
    private final EdgeProperty inner;

    EdgePropertyInduced(GraphExplicitInduced graph, EdgeProperty inner) {
        assert graph != null;
        assert inner != null;
        this.graph = graph;
        this.inner = inner;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int currentNode, int successor) {
        assert successor >= 0;
        int decision = graph.getDecision(currentNode);
        assert successor < (decision == -1 ? graph.getNumSuccessors(currentNode) : 1);
        return inner.get(currentNode, decision == -1 ? successor : decision);
    }

    @Override
    public void set(int node, int successor, Value value) {
        assert value != null;
        assert successor >= 0;
        int decision = graph.getDecision(node);
        assert successor < (decision == -1 ? graph.getNumSuccessors(node) : 1);
        inner.set(node, decision == -1 ? successor : decision, value);
    }

    @Override
    public Type getType() {
        return inner.getType();
    }

}
