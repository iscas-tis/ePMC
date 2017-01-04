package epmc.graph.explorer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.graph.StateSet;
import epmc.value.ContextValue;

/**
 * State set of an explorer.
 * 
 * @author Ernst Moritz Hahn
 */
final class StateSetExplorer <T extends ExplorerNode> implements StateSet {
    /** Explorer of which states of this set belong to. */
    private final Explorer explorer;
    /** States of this state set. */
    private final Set<T> nodes = new LinkedHashSet<>();

    /**
     * Construct new explorer state set.
     * None of the parameters may be {@code null} or contain {@code null}
     * entries. The states of the nodes parameter must all belong the the
     * explorer given by the explorer parameter.
     * 
     * @param explorer explorer to which nodes belong to
     * @param collection collection of which to construct nodes
     */
    StateSetExplorer(Explorer explorer, Collection<T> collection) {
        assert explorer != null;
        assert collection != null;
        for (ExplorerNode node : collection) {
            assert node != null;
            assert node.getExplorer() == explorer;
        }
        this.explorer = explorer;
        for (T node : collection) {
            this.nodes.add((T) node.clone());
        }
    }
    
    @Override
    public ContextValue getContextValue() {
        return explorer.getContextValue();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isSubsetOf(StateSet states) throws EPMCException {
        assert states != null;
        assert states instanceof StateSetExplorer;
        StateSetExplorer other = (StateSetExplorer) states;
        assert explorer == other.explorer;
        return other.nodes.containsAll(nodes);
    }

    @Override
    public StateSet clone() {
        return new StateSetExplorer(explorer, nodes);
    }
}
