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

import java.io.ByteArrayOutputStream;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.graph.explicit.GraphExporterDOT;
import epmc.graph.explicit.SchedulerSimple;
import epmc.util.BitSet;
import epmc.value.ContextValue;

public final class GraphExplicitInduced implements GraphExplicit {
    private final GraphExplicit original;
    private final SchedulerSimple scheduler;
    private final GraphExplicitProperties properties;
    private int decision;

    public GraphExplicitInduced(GraphExplicit original, SchedulerSimple scheduler) {
        assert original != null;
        assert scheduler != null;
        this.original = original;
        this.scheduler = scheduler;
        this.properties = new GraphExplicitProperties(this, original.getContextValue());
        for (Object property : original.getGraphProperties()) {
            properties.registerGraphProperty(property, original.getGraphProperty(property));
        }
        for (Object property : original.getNodeProperties()) {
            properties.registerNodeProperty(property,
                    new NodePropertyInduced(this, original.getNodeProperty(property)));
        }
        for (Object property : original.getEdgeProperties()) {
            properties.registerEdgeProperty(property,
                    new EdgePropertyInduced(this, original.getEdgeProperty(property)));
        }
    }

    @Override
    public int getNumNodes() {
        return original.getNumNodes();
    }

    @Override
    public BitSet getInitialNodes() {
        return original.getInitialNodes();
    }

    @Override
    public void queryNode(int node) throws EPMCException {
        original.queryNode(node);
        decision = scheduler.getDecision(node);
    }

    @Override
    public int getQueriedNode() {
        return original.getQueriedNode();
    }

    @Override
    public int getNumSuccessors() {
        if (decision == -1) {
            return original.getNumSuccessors();
        } else {
            return 1;
        }
    }

    @Override
    public int getSuccessorNode(int successor) {
        assert successor >= 0 : successor;
        assert decision == -1 ? successor < original.getNumSuccessors() : successor < 1
                : successor + " " + decision + " " + original.getNumSuccessors();
        if (decision == -1) {
            return original.getSuccessorNode(successor);
        } else {
            return original.getSuccessorNode(decision);
        }
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    int getDecision() {
        return decision;
    }
    
    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GraphExporterDOT.export(this, out);
        } catch (EPMCException e) {
            e.printStackTrace();
            assert false;
            return null;
        }
        return out.toString();
    }
    
	@Override
	public void close() {
	}

	@Override
	public ContextValue getContextValue() {
		return original.getContextValue();
	}
}
