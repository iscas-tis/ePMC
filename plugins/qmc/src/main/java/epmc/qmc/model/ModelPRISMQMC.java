package epmc.qmc.model;

import static epmc.error.UtilError.ensure;

import java.io.InputStream;
import epmc.graph.Semantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.prism.model.ModelPRISM;
import epmc.qmc.error.ProblemsQMC;

public final class ModelPRISMQMC implements Model, ModelJANIConverter {
    public final static String IDENTIFIER = "prism-qmc";
    private ModelPRISM modelPRISM = new ModelPRISM();

    @Override
    public Semantics getSemantics() {
        return modelPRISM.getSemantics();
    }

    @Override
    public Properties getPropertyList() {
        return modelPRISM.getPropertyList();
    }

    @Override
    public void read(Object part, InputStream... inputs) {
        assert inputs != null;
        ensure(inputs.length == 1, ProblemsQMC.PRISM_QMC_ONE_MODEL_FILE, inputs.length);
        QMCParser parser = new QMCParser(inputs[0]);    
        parser.setModel(modelPRISM);
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
