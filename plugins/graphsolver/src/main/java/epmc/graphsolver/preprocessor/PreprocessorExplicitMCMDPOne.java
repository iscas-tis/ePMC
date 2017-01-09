package epmc.graphsolver.preprocessor;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsCTMDP;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsNonDet;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.Scheduler;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ValueObject;

public final class PreprocessorExplicitMCMDPOne implements PreprocessorExplicit {
	public final static String IDENTIFIER = "mc-mdp-one";
	
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
		if (!(objective instanceof GraphSolverObjectiveExplicitUnboundedReachability)) {
			return false;
		}
		if (!(SemanticsDTMC.isDTMC(semantics)
				|| SemanticsCTMC.isCTMC(semantics)
				|| SemanticsMDP.isMDP(semantics)
				|| SemanticsCTMDP.isCTMDP(semantics))) {
			return false;
		}
        GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        // TODO fix for non-null case
		if (objectiveUnboundedReachability.getZeroSet() != null) {
			return false;
		}
		return true;
	}

	@Override
	public void process() throws EPMCException {
        GraphExplicit graphExplicit = null;
        GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        graphExplicit = objectiveUnboundedReachability.getGraph();
        Semantics semantics = ValueObject.asObject(graphExplicit.getGraphProperty(CommonProperties.SEMANTICS)).getObject();

        boolean min = objectiveUnboundedReachability.isMin();
        BitSet target = objectiveUnboundedReachability.getTarget();
        BitSet zero = objectiveUnboundedReachability.getZeroSet();
        assert zero == null;
        BitSet oldTarget = target;
        ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
        target = components.reachPre(graphExplicit, target, min, true);
        objectiveUnboundedReachability.setTarget(target);
        if (SemanticsNonDet.isNonDet(semantics) && objectiveUnboundedReachability.isComputeScheduler()) {
            objectiveUnboundedReachability.setScheduler(computeScheduler(graphExplicit, oldTarget, target));
        }
	}

    private Scheduler computeScheduler(GraphExplicit graph,
            BitSet target, BitSet extendedTarget) throws EPMCException {
        SchedulerSimple scheduler = new SchedulerSimpleArray(graph);
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
                graph.queryNode(node);
                for (int predNr = 0; predNr < graph.getNumPredecessors(); predNr++) {
                    int predecessor = graph.getPredecessorNode(predNr);
                    if (!extendedTarget.get(predecessor)) {
                        continue;
                    }
                    graph.queryNode(predecessor);
                    if (states.getBoolean() && scheduler.get(predecessor) == -1) {
                        for (int succNr = 0; succNr < graph.getNumSuccessors(); succNr++) {
                            if (graph.getSuccessorNode(succNr) == node) {
                                scheduler.set(predecessor, succNr);
                            }
                        }
                    }
                    graph.queryNode(node);
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
