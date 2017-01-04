package epmc.graphsolver;

import epmc.error.EPMCException;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;

/**
 * Collection of static methods for graph solvers.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilGraphSolver {
    /**
     * Create new explicit-state graph solver configuration.
     * 
     * @param options options to use for creation
     * @return graph solver configuration created
     */
    public static GraphSolverConfigurationExplicit newGraphSolverConfigurationExplicit(Options options) {
        assert options != null;
        return new GraphSolverConfigurationExplicit(options);
    }

    public static void solve(GraphSolverObjectiveExplicit objective) throws EPMCException {
        assert objective != null;
        GraphSolverConfigurationExplicit configuration = newGraphSolverConfigurationExplicit(objective.getGraph().getOptions());
        configuration.setObjective(objective);
        configuration.solve();
    }
    
    /**
     * Create new DD-based sstate graph solver configuration.
     * 
     * @param options options to use for creation
     * @return graph solver configuration created
     */
    public static GraphSolverConfigurationDD newGraphSolverConfigurationDD(
            Options options) {
        assert options != null;
        return new GraphSolverConfigurationDD(options);
    }
    
    public static BitSet map(int size, StateMap map, BitSet original) {
        assert size >= 0;
        assert map != null;
        if (original == null) {
            return null;
        }
        BitSet result = new BitSetBoundedLongArray(size);
        
        // TODO HACK to make sigref based lumper work
        if (size == 0) {
        	return result;
        }
        for (int entryNr = 0; entryNr < size; entryNr++) {
            result.set(entryNr, original.get(map.map(entryNr)));
        }
        return result;
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilGraphSolver() {
    }
}
