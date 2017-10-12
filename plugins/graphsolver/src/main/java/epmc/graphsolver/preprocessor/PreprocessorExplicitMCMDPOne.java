/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.graphsolver.preprocessor;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Scheduler;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsCTMDP;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsNonDet;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
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
    public void process() {
        GraphExplicit graphExplicit = null;
        GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachability) objective;
        graphExplicit = objectiveUnboundedReachability.getGraph();
        Semantics semantics = ValueObject.as(graphExplicit.getGraphProperty(CommonProperties.SEMANTICS)).getObject();

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
