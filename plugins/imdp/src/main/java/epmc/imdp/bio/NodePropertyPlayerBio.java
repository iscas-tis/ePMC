package epmc.imdp.bio;

import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.TypeEnum;
import epmc.value.Value;
import epmc.value.ValueEnum;

final class NodePropertyPlayerBio implements NodeProperty {
    private final GraphExplicitSparseAlternate graph;
    private final int numStates;
    private final TypeEnum type;
    private final ValueEnum value;

    public NodePropertyPlayerBio(GraphExplicitSparseAlternate graph) {
        assert graph != null;
        this.graph = graph;
        this.numStates = graph.computeNumStates();
        this.type = TypeEnum.get(Player.class);
        this.value = type.newValue();
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    @Override
    public Value get(int node) {
        value.set(node < numStates ? Player.ONE : Player.STOCHASTIC);
        return value;
    }

    @Override
    public void set(int node, Value value) {
        assert false;
    }

    @Override
    public Type getType() {
        return type;
    }

}
