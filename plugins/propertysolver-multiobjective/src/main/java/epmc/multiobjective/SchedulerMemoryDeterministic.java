package epmc.multiobjective;

import epmc.graph.Scheduler;

public interface SchedulerMemoryDeterministic extends Scheduler {
    int getInitialMemory(int state);

    int getDecision(int state, int memory);

    int getNextMemory(int presMemory, int nextState);
}
