package epmc.imdp.lump;

import epmc.graph.explicit.GraphExplicit;
import epmc.modelchecker.Log;

public interface Violator {
    interface Builder {
        Builder setOriginal(GraphExplicit original);

        Builder setPartition(Partition partition);

        Violator build();
    }

    boolean violate(int state, int compareState);

    void sendStatistics(Log log);
}
