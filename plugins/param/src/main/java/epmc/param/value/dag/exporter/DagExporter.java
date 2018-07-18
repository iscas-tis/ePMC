package epmc.param.value.dag.exporter;

import java.io.IOException;

import epmc.param.value.dag.Dag;

public interface DagExporter {
    interface Builder {
        Builder setDag(Dag dag);

        Builder addRelevantNode(int node);
        
        DagExporter build();
    }
    
    void export(Appendable result) throws IOException;
}
