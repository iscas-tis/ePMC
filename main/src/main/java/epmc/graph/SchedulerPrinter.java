package epmc.graph;

import java.io.OutputStream;

public interface SchedulerPrinter {
    void setLowLevel(LowLevel lowLevel);

    void setScheduler(Scheduler scheduler);

    void setOutput(OutputStream out);

    boolean canHandle();

    void print();
}
