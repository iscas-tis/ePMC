package epmc.multiobjective;

import epmc.graph.Scheduler;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;

public class SchedulerInitialRandomisedImpl implements SchedulerInitialRandomised {
    private ValueAlgebra[] probabilities;
    private Scheduler[] schedulers;

    SchedulerInitialRandomisedImpl(ValueAlgebra[] probabilities, Scheduler[] schedulers) {
        assert probabilities != null;
        for (ValueAlgebra value : probabilities) {
            assert value != null;
        }
        assert schedulers != null;
        for (Scheduler scheduler : schedulers) {
            assert scheduler != null;
        }
        assert probabilities.length == schedulers.length;
        this.probabilities = probabilities;
        this.schedulers = schedulers;
    }

    @Override
    public int size() {
        return schedulers.length;
    }

    @Override
    public ValueAlgebra getProbability(int scheduler) {
        return UtilValue.clone(probabilities[scheduler]);
    }

    @Override
    public Scheduler getScheduler(int scheduler) {
        return schedulers[scheduler];
    }
}
