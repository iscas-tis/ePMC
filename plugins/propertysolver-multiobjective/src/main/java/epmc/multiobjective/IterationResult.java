package epmc.multiobjective;

import epmc.graph.Scheduler;
import epmc.value.ValueArrayAlgebra;

final class IterationResult {
    private final ValueArrayAlgebra q;
    private final Scheduler scheduler;

    IterationResult(ValueArrayAlgebra q, Scheduler scheduler) {
        assert q != null;
        assert scheduler != null;
        this.q = q;
        this.scheduler = scheduler;
    }

    ValueArrayAlgebra getQ() {
        return q;
    }

    Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public String toString() {
        return q.toString();
    }
}
