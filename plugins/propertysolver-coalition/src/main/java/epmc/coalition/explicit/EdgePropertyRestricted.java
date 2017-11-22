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

import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.Type;
import epmc.value.Value;

final class EdgePropertyRestricted implements EdgeProperty {
    private final GraphExplicitRestricted graph;
    private final EdgeProperty original;
    private final BitSet restriction;
    private final int[] substitute;
    private final int maxNumSuccessors;

    EdgePropertyRestricted(GraphExplicitRestricted graph, EdgeProperty original) {
        assert graph != null;
        assert original != null;
        this.graph = graph;
        this.original = original;
        this.restriction = graph.getRestriction();
        this.substitute = graph.getSubstitute();
        this.maxNumSuccessors = graph.getMaxNumSuccessors();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int queriedNode, int successor) {
        boolean valid = restriction.get(queriedNode * maxNumSuccessors + successor);
        return original.get(queriedNode, valid ? successor : substitute[queriedNode]);
    }

    @Override
    public void set(int node, int succ, Value value) {
        assert false;
    }

    @Override
    public Type getType() {
        return original.getType();
    }

}
