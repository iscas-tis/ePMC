package epmc.jani.explorer;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.graph.LowLevel;
import epmc.graph.LowLevel.Builder;
import epmc.graph.explicit.GraphBuilderExplorer;
import epmc.graph.explorer.Explorer;
import epmc.jani.model.ModelJANI;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.EngineExplorer;
import epmc.modelchecker.Model;
import epmc.modelchecker.UtilModelChecker;

public final class LowLevelExplicitBuilder implements LowLevel.Builder {
    public final static String IDENTIFIER = "jani-explicit";
    
    private Model model;
    private Engine engine;
    private Set<Object> graphProperties = new LinkedHashSet<>();
    private Set<Object> nodeProperties = new LinkedHashSet<>();
    private Set<Object> edgeProperties = new LinkedHashSet<>();
    
    @Override
    public Builder setModel(Model model) {
        this.model = model;
        return this;
    }

    @Override
    public Builder setEngine(Engine engine) {
        this.engine = engine;
        return this;
    }

    @Override
    public Builder addGraphProperties(Set<Object> graphProperties) {
        this.graphProperties.addAll(graphProperties);
        return this;
    }

    @Override
    public Builder addNodeProperties(Set<Object> nodeProperties) {
        this.nodeProperties.addAll(nodeProperties);
        return this;
    }

    @Override
    public Builder addEdgeProperties(Set<Object> edgeProperties) {
        this.edgeProperties.addAll(edgeProperties);
        return this;
    }

    @Override
    public LowLevel build() {
        if (!(model instanceof ModelJANI)) {
            return null;
        }
        if (engine instanceof EngineExplicit) {
            Explorer explorer = (Explorer) UtilModelChecker.buildLowLevel
                    (model, EngineExplorer.ENGINE_EXPLORER,
                            graphProperties, nodeProperties, edgeProperties);
            GraphBuilderExplorer builder = new GraphBuilderExplorer();
            builder.setExplorer(explorer);
            builder.addDerivedGraphProperties(graphProperties);
            builder.addDerivedNodeProperties(nodeProperties);
            builder.addDerivedEdgeProperties(edgeProperties);
            builder.build();
            return builder.getGraph();
        } else {
            return null;
        }
    }

}
