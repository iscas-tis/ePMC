package epmc.qmc.model;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.graph.LowLevel;
import epmc.graph.LowLevel.Builder;
import epmc.modelchecker.Engine;
import epmc.modelchecker.Model;
import epmc.modelchecker.UtilModelChecker;
import epmc.prism.model.ModelPRISM;

public final class LowLevelQMCBuilder implements LowLevel.Builder {
    public final static String IDENTIFIER = "qmc";
    
    private Model model;
    private Engine engine;
    private final Set<Object> graphProperties = new LinkedHashSet<>();
    private final Set<Object> nodeProperties = new LinkedHashSet<>();
    private final Set<Object> edgeProperties = new LinkedHashSet<>();

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
        if (!(model instanceof ModelPRISMQMC)) {
            return null;
        }
        ModelPRISM modelPRISM = ((ModelPRISMQMC) model).getModelPRISM();
        return UtilModelChecker.buildLowLevel(modelPRISM, engine, graphProperties, nodeProperties, edgeProperties);
    }
}
