package epmc.rddl.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.rddl.value.UtilValue;
import epmc.value.Value;

final class Instance {
	private final static String INSTANCE = "instance";
	private final static String DOMAIN = "domain";
	private final static String NON_FLUENTS = "non-fluents";
	private final static String INIT_STATE = "init-state";
	private final static String MAX_NONDEF_ACTIONS = "max-nondef-actions";
	private final static String HORIZON = "horizon";
	private final static String DISCOUNT = "discount";
	private final static String EQ_SPACE = " = ";
    private String name;
    private Value discount;
    private int horizon;
    private Domain domain;
    private int maxNonDefActions;
    private Map<PVariableEntry,Value> initialValues = new LinkedHashMap<>();
    private Map<PVariableEntry,Value> initialValuesExternal = Collections.unmodifiableMap(initialValues);
    private NonFluents nonFluents = new NonFluents();
    private final RDDLObjects objects = new RDDLObjects();

    void setName(String name) {
        assert name != null;
        this.name = name;
    }

    void setDiscount(Value discount) {
        assert discount != null;
        this.discount = epmc.value.UtilValue.clone(discount);
    }

    void setHorizon(int horizon) {
        assert horizon >= 0;
        this.horizon = horizon;
    }

    
    void setDomain(Domain domain) {
        assert domain != null;
        this.domain = domain;
    }

    void setNonFluents(NonFluents nonFluents) {
        assert nonFluents != null;
        this.nonFluents = nonFluents;
    }
    
    NonFluents getNonFluents() {
        return nonFluents;
    }
    
    void setMaxNonDefActions(int maxNonDefActions) {
        assert maxNonDefActions >= 0;
        this.maxNonDefActions = maxNonDefActions;
    }
    
    int getMaxNonDefActions() {
        return this.maxNonDefActions;
    }

    void setInitialValue(PVariable pVariable, List<String> parameters, Value value) {
        assert pVariable != null;
        assert value != null;
        assert parameters != null;
        this.initialValues.put(new PVariableEntry(pVariable, parameters), value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(INSTANCE + " " + name + " {\n");
        builder.append(indent(1) + DOMAIN + EQ_SPACE);
        builder.append(domain.getName());
        builder.append(";\n\n");
        if (nonFluents != null) {
            builder.append(indent(1) + NON_FLUENTS + EQ_SPACE);
            builder.append(nonFluents.getName());
            builder.append(";\n\n");
        }
        if (initialValues.size() > 0) {
            builder.append(indent(1) + INIT_STATE + " {\n");
            for (Entry<PVariableEntry, Value> entry : initialValues.entrySet()) {
                builder.append(indent(2));
                builder.append(entry.getKey().getPVariable().getName());
                List<String> parameters = entry.getKey().getParameterStrings();
                if (parameters.size() > 0) {
                    builder.append("(");
                    for (int i = 0; i < parameters.size(); i++) {
                        builder.append(parameters.get(i));
                        if (i < parameters.size() - 1) {
                            builder.append(",");
                        }
                    }
                    builder.append(")");
                }
                builder.append(EQ_SPACE);
                builder.append(entry.getValue());
                builder.append(";\n");
            }
            builder.append(indent(1) + "};\n\n");
        }
        builder.append(indent(1) + MAX_NONDEF_ACTIONS + EQ_SPACE);
        builder.append(maxNonDefActions);
        builder.append(";\n");
        builder.append(indent(1) + HORIZON + EQ_SPACE);
        builder.append(horizon);
        builder.append(";\n");
        builder.append(indent(1) + DISCOUNT + EQ_SPACE);
        builder.append(UtilValue.toString(discount));
        builder.append(";\n");
        builder.append("}\n");
        
        return builder.toString();
    }

    Domain getDomain() {
        return domain;
    }
    
    Map<PVariableEntry, Value> getInitialValues() {
        return initialValuesExternal;
    }

	public void setObjectValues(String objectName, List<String> objectValues) {
		assert objectName != null;
		assert objectValues != null;
		this.objects.setObjectValues(objectName, objectValues);
	}

	public List<RDDLObjectValue> getObjectValue(String parameter) {
		assert parameter != null;
		if (this.objects.contains(parameter)) {
			return this.objects.getObjectValue(parameter);
		} else {
			return this.nonFluents.getObjectValue(parameter);
		}
	}
	
	public RDDLObject getObject(String name) {
		assert name != null;
		assert name != null;
		if (this.objects.contains(name)) {
			return this.objects.getObject(name);
		} else {
			return this.nonFluents.getObject(name);
		}
	}
	
	private String indent(int level) {
    	assert level >= 0;
    	StringBuffer buffer = new StringBuffer();
    	for (int l = 0; l < level; l++) {
    		buffer.append("    ");
    	}
    	return buffer.toString();
    }
}
