package epmc.imdp.model;

import static epmc.error.UtilError.ensure;

import java.io.InputStream;
import java.util.Set;

import epmc.imdp.model.IMDPParser;
import epmc.error.EPMCException;
import epmc.graph.LowLevel;
import epmc.graph.Semantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.modelchecker.Engine;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.prism.error.ProblemsPRISM;
import epmc.prism.model.ModelPRISM;
import epmc.value.ContextValue;

public final class ModelIMDP implements Model, ModelJANIConverter {
    public final static String IDENTIFIER = "imdp";
    private final ModelPRISM modelPRISM = new ModelPRISM();
    
    @Override
    public Semantics getSemantics() {
        return modelPRISM.getSemantics();
    }

	@Override
	public LowLevel newLowLevel(Engine engine, Set<Object> graphProperties, Set<Object> nodeProperties,
			Set<Object> edgeProperties) throws EPMCException {
		return modelPRISM.newLowLevel(engine, graphProperties, nodeProperties, edgeProperties);
    }
    
    @Override
    public Properties getPropertyList() {
    	return modelPRISM.getPropertyList();
    }
    
    @Override
    public ContextValue getContextValue() {
        return modelPRISM.getContextValue();
    }
    
    @Override
    public void setContext(ContextValue context) {
    	modelPRISM.setContext(context);
    }

    @Override
    public void read(InputStream... inputs) throws EPMCException {
        assert inputs != null;
        ensure(inputs.length == 1, ProblemsPRISM.PRISM_ONE_MODEL_FILE, inputs.length);
        IMDPParser parser = new IMDPParser(inputs[0]);    
        parser.setModel(modelPRISM);
        parser.parseModel(modelPRISM.getContextValue());
    }
    
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public ModelJANI toJANI() throws EPMCException {
		return modelPRISM.toJANI();
	}
}
