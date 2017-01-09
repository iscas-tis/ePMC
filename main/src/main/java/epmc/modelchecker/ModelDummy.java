package epmc.modelchecker;

import java.io.InputStream;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.graph.LowLevel;
import epmc.graph.Semantics;
import epmc.value.ContextValue;

/**
 * Dummy model or cases where a model is not really needed.
 * This class can be used e.g. for translation from formulas to automata, where
 * a model is not needed but needs to be present for technical reasons.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelDummy implements Model {
    /** Identifier of this model class. */
    public final static String IDENTIFIER = "dummy";
    /** Value context used. */
    private ContextValue contextValue;
    /** Properties of the model. */
    private PropertiesDummy properties;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setContext(ContextValue context) {
        assert this.contextValue == null;
        assert context != null;
        this.contextValue = context;
        this.properties = new PropertiesDummy(context);
    }

    @Override
    public void read(InputStream... inputs) throws EPMCException {
        assert inputs != null;
        assert inputs.length == 0;
    }

    @Override
    public Semantics getSemantics() {
        return null;
    }

    @Override
    public ContextValue getContextValue() {
        return contextValue;
    }

    @Override
    public LowLevel newLowLevel(Engine engine, Set<Object> graphProperties,
            Set<Object> nodeProperties, Set<Object> edgeProperties)
                    throws EPMCException {
        return null;
    }

    @Override
    public Properties getPropertyList() {
        return properties;
    }
}
