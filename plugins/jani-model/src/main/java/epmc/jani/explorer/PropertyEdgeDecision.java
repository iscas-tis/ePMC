package epmc.jani.explorer;

import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.value.Type;
import epmc.value.Value;

public final class PropertyEdgeDecision implements ExplorerEdgeProperty {
    private final ExplorerJANI explorer;
    private final ValueDecision value;

    public PropertyEdgeDecision(ExplorerJANI explorer) {
        assert explorer != null;
        this.explorer = explorer;
        TypeDecision type = new TypeDecision(explorer);
        value = type.newValue();
    }

    @Override
    public Value get(int successor) {
        NodeJANI node = explorer.getSuccessorNode(successor);
        value.set(node);
        return value;
    }

    @Override
    public Type getType() {
        return value.getType();
    }
}
