package epmc.multiobjective;

import epmc.graph.Scheduler;
import epmc.value.ValueAlgebra;

public interface SchedulerInitialRandomised extends Scheduler {
    int size();

    ValueAlgebra getProbability(int scheduler);

    Scheduler getScheduler(int scheduler);
}
