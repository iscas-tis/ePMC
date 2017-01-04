package epmc.graph.explicit;

import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

/**
 * Node property in which all edges are assigned the same value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class EdgePropertyConstant implements EdgeProperty {
    /** Graph to which this edge property belongs. */
    private final GraphExplicit graph;
    /** Value returned by {@link #get()} . */
    private final Value value;

    /**
     * Create new constant node property.
     * None of parameters may be {@code null}.
     * 
     * @param graph graph to which the node property belongs
     * @param value value assigned to all edges
     */
    public EdgePropertyConstant(GraphExplicit graph, Value value) {
        assert graph != null;
        assert value != null;
        this.graph = graph;
        this.value = UtilValue.clone(value);
    }
    
    /**
     * {@inheritDoc}
     * In this implementation, the value is the same value for all edges,
     * given by the constructor of this class.
     */
    @Override
    public Value get(int successor) {
        assert successor >= 0;
        assert successor < graph.getNumSuccessors() : successor;
        return value;
    }

    /**
     * {@inheritDoc}
     * In this implementation, this method will set the value for all edges
     * of the graph.
     */
    @Override
    public void set(Value value, int successor) {
        assert value != null;
        assert successor >= 0;
        this.value.set(value);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }
}
