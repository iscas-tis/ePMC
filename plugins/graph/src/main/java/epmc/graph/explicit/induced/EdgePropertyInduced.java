package epmc.graph.explicit.induced;

import epmc.error.EPMCException;
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
    public Value get(int successor) throws EPMCException {
        assert successor >= 0;
        int decision = graph.getDecision();
        assert successor < (decision == -1 ? graph.getNumSuccessors() : 1);
        return inner.get(decision == -1 ? successor : decision);
    }

    @Override
    public void set(Value value, int successor) {
        assert value != null;
        assert successor >= 0;
        int decision = graph.getDecision();
        assert successor < (decision == -1 ? graph.getNumSuccessors() : 1);
        inner.set(value, decision == -1 ? successor : decision);
    }

    @Override
    public Type getType() {
        return inner.getType();
    }

}
