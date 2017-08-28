package epmc.multiobjective;

import epmc.graph.explicit.SchedulerSimple;

final class SchedulerSimpleArray implements SchedulerSimple {
    private final int[] array;

    SchedulerSimpleArray(int[] array) {
        assert array != null;
        this.array = array;
    }

    @Override
    public int getDecision(int node) {
        return array[node];
    }
}
