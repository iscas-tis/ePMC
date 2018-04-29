package epmc.imdp.error.preprocessor;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Scheduler;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.graphsolver.preprocessor.PreprocessorExplicit;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;

public final class PreprocessorExplicitIMDPOne implements PreprocessorExplicit {
    public final static String IDENTIFIER = "imdp-one";

    private GraphSolverObjectiveExplicit objective;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setObjective(GraphSolverObjectiveExplicit objective) {
        this.objective = objective;
    }

    @Override
    public GraphSolverObjectiveExplicit getObjective() {
        return objective;
    }

    @Override
    public boolean canHandle() {
        assert objective != null;
        Semantics semantics = objective.getGraph().getGraphPropertyObject(CommonProperties.SEMANTICS);
        return objective instanceof GraphSolverObjectiveExplicitUnboundedReachability
                && SemanticsMDP.isMDP(semantics)
                && TypeInterval.is(TypeWeightTransition.get());
    }

    @Override
    public void process() {
        GraphExplicit graphExplicit = null;
        GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        graphExplicit = objectiveUnboundedReachability.getGraph();
        boolean min = objectiveUnboundedReachability.isMin();
        BitSet target = objectiveUnboundedReachability.getTarget();
        BitSet oldTarget = target;
        ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
        target = components.reachPre(graphExplicit, target, min, true);
        objectiveUnboundedReachability.setTarget(target);
        if (objectiveUnboundedReachability.isComputeScheduler()) {
            objectiveUnboundedReachability.setScheduler(computeScheduler(graphExplicit, oldTarget, target));
        }
    }

    private Scheduler computeScheduler(GraphExplicit graph,
            BitSet target, BitSet extendedTarget) {
        SchedulerSimpleSettable scheduler = new SchedulerSimpleArray(graph);
        graph.computePredecessors();
        NodeProperty states = graph.getNodeProperty(CommonProperties.STATE);
        BitSet seen = UtilBitSet.newBitSetUnbounded();
        seen.or(target);
        BitSet next = UtilBitSet.newBitSetUnbounded();
        next.or(target);
        BitSet nextNext = UtilBitSet.newBitSetUnbounded();
        boolean changed = true;
        while (changed) {
            changed = false;
            nextNext.clear();
            for (int node = next.nextSetBit(0); node >= 0; node = next.nextSetBit(node + 1)) {
                for (int predNr = 0; predNr < graph.getProperties().getNumPredecessors(node); predNr++) {
                    int predecessor = graph.getProperties().getPredecessorNode(node, predNr);
                    if (!extendedTarget.get(predecessor)) {
                        continue;
                    }
                    if (states.getBoolean(predecessor) && scheduler.getDecision(predecessor) == -1) {
                        for (int succNr = 0; succNr < graph.getNumSuccessors(predecessor); succNr++) {
                            if (graph.getSuccessorNode(predecessor, succNr) == node) {
                                scheduler.set(predecessor, succNr);
                            }
                        }
                    }
                    if (!seen.get(predecessor)) {
                        changed = true;
                        seen.set(predecessor);
                        nextNext.set(predecessor);
                    }
                }
            }
            BitSet swap;
            swap = next;
            next = nextNext;
            nextNext = swap;
        }
        return scheduler;
    }

}
