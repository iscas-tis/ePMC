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

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.graph.explicit.GraphExporterDOT;
import epmc.graph.explicit.SchedulerSimple;
import epmc.util.BitSet;

public final class GraphExplicitInduced implements GraphExplicit {
    private final GraphExplicit original;
    private final SchedulerSimple scheduler;
    private final GraphExplicitProperties properties;

    public GraphExplicitInduced(GraphExplicit original, SchedulerSimple scheduler) {
        assert original != null;
        assert scheduler != null;
        this.original = original;
        this.scheduler = scheduler;
        this.properties = new GraphExplicitProperties(this);
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
    public int getNumSuccessors(int node) {
        int decision = scheduler.getDecision(node);
        if (decision == -1) {
            return original.getNumSuccessors(node);
        } else {
            return 1;
        }
    }

    @Override
    public int getSuccessorNode(int node, int successor) {
        int decision = scheduler.getDecision(node);
        if (decision == -1) {
            return original.getSuccessorNode(node, successor);
        } else {
            return original.getSuccessorNode(node, decision);
        }
    }

    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    int getDecision(int node) {
        return scheduler.getDecision(node);
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GraphExporterDOT.export(this, out);
        return out.toString();
    }

    @Override
    public void close() {
    }
}
