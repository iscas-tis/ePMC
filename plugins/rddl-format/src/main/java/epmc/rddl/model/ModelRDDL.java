package epmc.rddl.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rddl.RDDL;
import rddl.parser.parser;
import epmc.error.EPMCException;
import epmc.error.UtilError;
import epmc.graph.LowLevel;
import epmc.graph.Semantics;
import epmc.graph.UtilGraph;
import epmc.graph.explorer.Explorer;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.EngineExplorer;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.modelchecker.PropertiesImpl;
import epmc.options.Options;
import epmc.rddl.error.ProblemsRDDL;
import epmc.rddl.expression.ContextExpressionRDDL;
import epmc.rddl.options.OptionsRDDL;
import epmc.rddl.value.ContextValueRDDL;
import epmc.value.ContextValue;

public final class ModelRDDL implements Model {
    public final static String IDENTIFIER = "rddl";
    private ContextValue contextValue;
    private ContextValueRDDL contextValueRDDL;
    private Semantics semantics;
    private final List<RDDL> rddl = new ArrayList<>();
    private boolean built;
    private Properties properties;
    private Map<String,Domain> domains;
    private Map<String,Instance> instances;
    private ContextExpressionRDDL contextExpressionRDDL;
    private Map<String, NonFluents> nonFluents;

    @Override
    public void setContext(ContextValue context) {
        assert context != null;
        this.contextValue = context;
        this.contextValueRDDL = new ContextValueRDDL(contextValue);
        this.contextExpressionRDDL = new ContextExpressionRDDL(contextValueRDDL);
    }

    @Override
    public void read(InputStream... inputs) throws EPMCException {
        try {
            for (InputStream input : inputs) {
                this.rddl.add(parser.parse(input));
            }
        } catch (Exception e) {
            UtilError.fail(ProblemsRDDL.RDDL_PARSER_FAILED, e);
            return;
        }
        built = false;
    }

    @Override
    public Semantics getSemantics() {
        return this.semantics;
    }

	@Override
	public LowLevel newLowLevel(Engine engine, Set<Object> graphProperties, Set<Object> nodeProperties,
			Set<Object> edgeProperties) throws EPMCException {
		if (engine instanceof EngineExplorer) {
	        if (!built) {
	            build();
	        }
	        Options options = contextValue.getOptions();
	        String instanceChosen = options.get(OptionsRDDL.RDDL_INSTANCE_NAME);
	        Instance instance = null;
	        if (instanceChosen == null) {
	            instance = instances.values().iterator().next();        	
	        } else {
	        	instance = instances.get(instanceChosen);
	        }
	        assert instance != null : instanceChosen;
	        return new ExplorerRDDL(instance);
		} else if (engine instanceof EngineExplicit) {
			Explorer explorer = (Explorer) newLowLevel(EngineExplorer.getInstance(),
	                graphProperties, nodeProperties, edgeProperties);
			return UtilGraph.buildModelGraphExplicit(explorer, graphProperties, nodeProperties, edgeProperties);
		} else if (engine instanceof EngineDD) {
	        if (!built) {
	            build();
	        }
	        // TODO Auto-generated method stub
	        return null;
		} else {
			assert false; // TODO user exception
			return null;
		}
    }

    @Override
    public Properties getPropertyList() {
        if (this.properties == null) {
            try {
				this.properties = new PropertiesImpl(contextValue);
			} catch (EPMCException e) {
				throw new RuntimeException(e);
			}
        }
        return this.properties;
    }
    
    private void build() throws EPMCException {
        assert rddl != null;
        ModelBuilder builder = new ModelBuilder();
        builder.setRDDL(rddl);
        builder.setContextExpressionRDDL(contextExpressionRDDL);
        builder.build();
        this.domains = builder.getDomains();
        this.instances = builder.getInstances();
        this.nonFluents = builder.getNonFluents();
    }

    @Override
    public String toString() {
        if (!built) {
            try {
				build();
			} catch (EPMCException e) {
				e.printStackTrace();
				assert false;
			}
        }
        StringBuilder builder = new StringBuilder();
        assert domains != null;
        for (Domain domain : domains.values()) {
            builder.append(domain);
        }
        builder.append("\n");
        for (NonFluents nonFluents : nonFluents.values()) {
            builder.append(nonFluents);
        }
        builder.append("\n");
        for (Instance instance : instances.values()) {
            builder.append(instance);
        }
        
        return builder.toString();
    }

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}
}
