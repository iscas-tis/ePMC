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

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.Value;

final class NodePropertyRestricted implements NodeProperty {
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
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
	public Value get() throws EPMCException {
		return original.get();
	}

	@Override
	public void set(Value value) throws EPMCException {
		assert false;
	}

	@Override
	public Type getType() {
		return original.getType();
	}
}
