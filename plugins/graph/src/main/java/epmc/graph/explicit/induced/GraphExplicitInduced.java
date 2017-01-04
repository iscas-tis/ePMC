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
        decision = scheduler.get(node);
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
