package epmc.graphsolver.lumping;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.NodePropertyExpression;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class UtilLump {
    public static GraphSolverObjectiveExplicitLump partitionByAPsObjective(GraphExplicit graph, Collection<Expression> atomics) throws EPMCException {
        int[] partition = partitionByAPs(graph, atomics);
        GraphSolverObjectiveExplicitLump result = new GraphSolverObjectiveExplicitLump();
        result.setGraph(graph);
        result.setPartition(partition);
        return result;
    }
    
    public static int[] partitionByAPs(GraphExplicit graph, Collection<Expression> atomics) throws EPMCException {
        assert graph != null;
        for (Expression atomic : atomics) {
            assert atomic != null;
            assert isPropositional(atomic) : atomic;
        }
        NodeProperty[] atomProperties = computeAtomNodeProperties(graph, atomics);
        int[] partition = computePartition(graph, atomProperties);
        fillGaps(partition);
        
        return partition;
    }
        
    private static NodeProperty[] computeAtomNodeProperties(GraphExplicit graph,
            Collection<Expression> atomics) throws EPMCException {
        List<NodeProperty> result = new ArrayList<>();
        for (Expression atomic : atomics) {
            if (graph.getNodeProperty(atomic) != null) {
                result.add(graph.getNodeProperty(atomic));
            } else {
                result.add(new NodePropertyExpression(graph, atomic));
            }
        }
        return result.toArray(new NodeProperty[0]);
    }

    private static int[] computePartition(GraphExplicit graph,
            NodeProperty[] atomProperties) throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] partition = new int[numStates];
        for (int state = 0; state < numStates; state++) {
            graph.queryNode(state);
            int value = 0;
            int marker = 1;
            for (int atomicNr = 0; atomicNr < atomProperties.length; atomicNr++) {
                if (atomProperties[atomicNr].getBoolean()) {
                    value |= marker;
                }
                marker <<= 1;
            }
            partition[state] = value;
        }
        return partition;
    }

    public static void fillGaps(int[] partition) {
        BitSet used = new BitSetUnboundedLongArray();
        for (int state = 0; state < partition.length; state++) {
            used.set(partition[state]);
        }
        int[] oldToNew = new int[used.length()];
        Arrays.fill(oldToNew, -1);
        int newIndex = 0;
        for (int oldIndex = used.nextSetBit(0); oldIndex >= 0; oldIndex
                = used.nextSetBit(oldIndex + 1)) {
            oldToNew[oldIndex] = newIndex;
            newIndex++;
        }
        for (int state = 0; state < partition.length; state++) {
            partition[state] = oldToNew[partition[state]];
        }
    }

    private UtilLump() {
    }
}
