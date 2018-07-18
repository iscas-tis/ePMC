package epmc.param.graphsolver.eliminationorder;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;

public interface EliminationOrder {
    interface Builder {
        Builder setGraph(MutableGraph graph);
        
        Builder setTarget(BitSet target);
        
        EliminationOrder build();
    }

    boolean hasNodes();
    
    int nextNode();
}
