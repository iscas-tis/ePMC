package epmc.imdp.model;

import static epmc.error.UtilError.ensure;

import java.io.InputStream;

import epmc.graph.Semantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.prism.error.ProblemsPRISM;
import epmc.prism.model.ModelPRISM;

public final class ModelIMDP implements Model, ModelJANIConverter {
    public final static String IDENTIFIER = "imdp";
    private final ModelPRISM modelPRISM = new ModelPRISM();

    @Override
    public Semantics getSemantics() {
        return modelPRISM.getSemantics();
    }

    @Override
    public Properties getPropertyList() {
        return modelPRISM.getPropertyList();
    }

    @Override
    public void read(Object identifier, InputStream... inputs) {
        assert inputs != null;
        ensure(inputs.length == 1, ProblemsPRISM.PRISM_ONE_MODEL_FILE, inputs.length);
        IMDPParser parser = new IMDPParser(inputs[0]);    
        parser.setModel(modelPRISM);
        parser.setPart(identifier);
        parser.parseModel();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public ModelJANI toJANI(boolean forExporting) {
        return modelPRISM.toJANI(forExporting);
    }
    
    public ModelPRISM getModelPRISM() {
        return modelPRISM;
    }
}
