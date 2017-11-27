package epmc.multiobjective.graphsolver;

import java.util.Arrays;

import epmc.graph.Scheduler;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.SchedulerSimple;

public final class SchedulerSimpleMultiobjectiveJava implements SchedulerSimple {
    private final int[] stateBounds;
    private final int numStates;
    private final int[] decisions;

    public SchedulerSimpleMultiobjectiveJava(GraphExplicitSparseAlternate graph) {
        assert graph != null;
        stateBounds = graph.getStateBoundsJava();
        numStates = graph.computeNumStates();
        decisions = new int[numStates];
        Arrays.fill(decisions, Scheduler.UNSET);
    }

    @Override
    public int getDecision(int node) {
        if (decisions[node] >= 0) {
            return decisions[node] - stateBounds[node];
        } else {
            return decisions[node];
        }
    }

    public int[] getDecisions() {
        return decisions;
    }	
}
