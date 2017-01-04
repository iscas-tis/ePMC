package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.rddl.expression.ContextExpressionRDDL;
import epmc.rddl.expression.UtilExpression;
import epmc.rddl.value.TypeRDDLEnum;
import epmc.rddl.value.TypeRDDLObject;
import epmc.value.ContextValue;
import epmc.value.Type;

final class Domain {
	private final static String DOMAIN = "domain";
	private final static String REQUIREMENTS = "requirements";
	private final static String REWARD_DETERMINISTIC = "reward-deterministic";
	private final static String INTEGER_VALUED = "integer-valued";
	private final static String CONTINUOUS = "continuous";
	private final static String MULTIVALUED = "multivalued";
	private final static String INTERMEDIATE_NODES = "intermediate-nodes";
	private final static String PARTIALLY_OBSERVED = "partially-observed";
	private final static String CONSTRAINED_STATE = "constrained-state";
	private final static String CONCURRENT = "concurrent";
	private final static String CPF_DETERMINISTIC = "cpf-deterministic";
	private final static String TYPES = "types";
	private final static String OBJECT = "object";
	private final static String PVARIABLES = "pvariables";
	private final static String REWARD = "reward";
	private final static String STATE_ACTION_CONSTRAINTS = "state-action-constraints";
	private final static String EQ_SPACE = " = ";
	private ContextExpressionRDDL contextExpressionRDDL;
    private ContextValue contextValue;
    private String name;
    private boolean rewardDeterministic;
    private boolean multiAction;
    private boolean continuousVariables;
    private boolean probabilistic;
    private boolean usesIntegers;
    private boolean hasIntermediateNodes;
    private boolean partiallyObservable;
    private boolean hasStateActionConstraints;
    private boolean usesEnums;

    private final Map<String,Type> types = new LinkedHashMap<>();
    private final Map<String,PVariable> pVariables = new LinkedHashMap<>();
    private final Map<String,StateFluent> stateFluents = new LinkedHashMap<>();
    private final Map<String,StateFluent> stateFluentsExternal = Collections.unmodifiableMap(stateFluents);
    private final Map<String,ActionFluent> actionFluents = new LinkedHashMap<>();
    private final Map<String,ActionFluent> actionFluentsExternal = Collections.unmodifiableMap(actionFluents);
    private final Map<String,IntermediateFluent> intermediateFluents = new LinkedHashMap<>();
    private final Map<String,IntermediateFluent> intermediateFluentsExternal = Collections.unmodifiableMap(intermediateFluents);
    private final Map<String,ObserverFluent> observerFluents = new LinkedHashMap<>();
    private final Map<String,NonFluent> nonFluents = new LinkedHashMap<>();
    private final Map<String,NonFluent> nonFluentsExternal = Collections.unmodifiableMap(nonFluents);
    private final List<Expression> stateActionConstraint = new ArrayList<>();
    private final List<Expression> stateActionConstraintExternal = Collections.unmodifiableList(stateActionConstraint);

    private final Map<String,Expression> cpfs = new LinkedHashMap<>();
    private final Map<String, List<Expression>> parameters = new LinkedHashMap<>();
    private final Map<String, List<Expression>> parametersExternal = Collections.unmodifiableMap(parameters);
    private Expression reward;
    private String cpfHeader;
    
    public void setContextExpressionRDDL(ContextExpressionRDDL contextExpressionRDDL) {
        assert contextExpressionRDDL != null;
        this.contextExpressionRDDL = contextExpressionRDDL;
        this.contextValue = contextExpressionRDDL.getContextValue();
    }

    void setName(String name) {
        assert name != null;
        this.name = name;
    }
    
    public void setRewardDeterministic(boolean rewardDeterministic) {
        this.rewardDeterministic = rewardDeterministic;
    }
    
    void setMultiAction(boolean multiAction) {
        this.multiAction = multiAction;
    }
    
    void setContinuousVariables(boolean continuousVariables) {
        this.continuousVariables = continuousVariables;
    }
    
    void setProbabilistic(boolean probabilistic) {
        this.probabilistic = probabilistic;
    }
    
    void setUsesIntegers(boolean usesIntegers) {
        this.usesIntegers = usesIntegers;
    }
    
    void setHasIntermediateNodes(boolean hasIntermediateNodes) {
        this.hasIntermediateNodes = hasIntermediateNodes;
    }
    
    void setPartiallyObservable(boolean partiallyObservable) {
        this.partiallyObservable = partiallyObservable;
    }
    
    public void setUsesEnums(boolean usesEnums) {
        this.usesEnums = usesEnums;
    }

    void setHasStateConstraints(boolean hasStateConstraints) {
        this.hasStateActionConstraints = hasStateConstraints;
    }
    
    void addStateFluent(StateFluent fluent) {
        assert fluent != null;
        this.stateFluents.put(fluent.getName(), fluent);
        this.pVariables.put(fluent.getName(), fluent);
    }

    void addActionFluent(ActionFluent fluent) {
        assert fluent != null;
        this.actionFluents.put(fluent.getName(), fluent);
        this.pVariables.put(fluent.getName(), fluent);
    }
    
    void addIntermediateFluent(IntermediateFluent fluent) {
        assert fluent != null;
        this.intermediateFluents.put(fluent.getName(), fluent);
        this.pVariables.put(fluent.getName(), fluent);
    }

    public void addObserverFluent(ObserverFluent fluent) {
        assert fluent != null;
        this.observerFluents.put(fluent.getName(), fluent);
        this.pVariables.put(fluent.getName(), fluent);
    }

    void addNonFluent(NonFluent nonFluent) {
        assert nonFluent != null;
        this.nonFluents.put(nonFluent.getName(), nonFluent);
        this.pVariables.put(nonFluent.getName(), nonFluent);
    }
    
    void addType(String name, Type type) {
        assert name != null;
        assert type != null;
        this.types.put(name, type);
    }
    
    Type getType(String name) {
        assert name != null;
        return this.types.get(name);
    }
    
    void addCpfs(String name, List<Expression> parameters, Expression expression) {
        assert name != null;
        assert parameters != null;
        for (Expression parameter : parameters) {
            assert parameter != null;
            assert parameter instanceof ExpressionIdentifierStandard;
        }
        assert expression != null;
        this.cpfs.put(name, expression);
        this.parameters.put(name, Collections.unmodifiableList(new ArrayList<>(parameters)));
    }
    
    Expression getCpfsAssignment(String name) {
        assert name != null;
        assert this.cpfs.containsKey(name);
        return this.cpfs.get(name);
    }
    
    List<Expression> getCpfsParameters(PVariable pVariable) {
    	assert pVariable != null;
    	assert this.parameters.containsKey(pVariable.getName()) : pVariable.getName() + " " + this.parameters;
    	return this.parameters.get(pVariable.getName());
    }

    Map<Expression,RDDLObject> getCpfsParameters(Instance instance, PVariable pVariable) {
    	assert instance != null;
    	assert pVariable != null;
    	assert parameters.containsKey(pVariable.getName());
    	List<Expression> pNames = parameters.get(pVariable.getName());
    	List<RDDLObject> pList = pVariable.getParameters(instance);
    	assert pList.size() == pNames.size();
    	Map<Expression,RDDLObject> result = new LinkedHashMap<>();
    	for (int varNr = 0; varNr < pList.size(); varNr++) {
    		result.put(pNames.get(varNr), pList.get(varNr));
    	}
    	return result;
    }

    public Map<String, List<Expression>> getCpfsParameters() {
		return parametersExternal;
	}

    List<Expression> getCpfsParameters(String name) {
    	assert name != null;
    	assert parameters.containsKey(name);
    	return parameters.get(name);
    }
    
    void setReward(Expression reward) {
        assert reward != null;
        assert this.reward == null;
        this.reward = reward;
    }

    public StateFluent getStateFluent(String name) {
        assert name != null;
        return stateFluents.get(name);
    }
    
    public NonFluent getNonFluent(String name) {
    	assert name != null;
    	assert nonFluents.containsKey(name);
    	return nonFluents.get(name);
    }
    
    public PVariable getPVariable(String name) {
        assert name != null;
        return pVariables.get(name);
    }
    
    public String getName() {
        return name;
    }
    
    public Map<String, StateFluent> getStateFluents() {
        return stateFluentsExternal;
    }

    public Map<String, ActionFluent> getActionFluents() {
        return actionFluentsExternal;
    }
    
    public Map<String, NonFluent> getNonFluents() {
		return nonFluentsExternal;
	}
    
    public Map<String,IntermediateFluent> getIntermediateFluents() {
        return intermediateFluentsExternal;
    }
    
    public ContextExpressionRDDL getContextExpressionRDDL() {
		return contextExpressionRDDL;
	}
    
    public ContextValue getContextValue() {
        return contextValue;
    }

    public void setCPFHeader(String cpfHeader) {
        assert cpfHeader != null;
        this.cpfHeader = cpfHeader;
    }

    public void addStateActionConstraint(Expression constraint) {
        assert constraint != null;
        this.stateActionConstraint.add(constraint);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DOMAIN + " " + name + " {\n\n");
        appendRequirements(builder);
        appendTypes(builder);
        appendPVariables(builder);
        appendReward(builder);
        appendStateConstraint(builder);
        builder.append("}\n");
        return builder.toString();
    }
    
    private void appendStateConstraint(StringBuilder builder) {
    	assert builder != null;
        if (stateActionConstraint.size() == 0) {
        	return;
        }
        builder.append(indent(1) + STATE_ACTION_CONSTRAINTS + " {\n");
        for (Expression entry : stateActionConstraint) {
        	builder.append(indent(2));
        	builder.append(UtilExpression.toString(entry));
        	builder.append(";\n");
        }
        builder.append(indent(1) + "};\n\n");
	}

	private void appendReward(StringBuilder builder) {
    	assert builder != null;
        builder.append(indent(1) + REWARD + EQ_SPACE);
        builder.append(UtilExpression.toString(reward));
        builder.append(";\n\n");
	}

	private void appendPVariables(StringBuilder builder) {
    	assert builder != null;
        builder.append(indent(1) + PVARIABLES + " {\n");
        for (PVariable stateFluent : pVariables.values()) {
            builder.append(indent(2));
            builder.append(stateFluent);
            builder.append("\n");
        }
        builder.append(indent(1) + "};\n\n");
        builder.append(indent(1) + cpfHeader + " {\n");
        for (Entry<String, Expression> entry : cpfs.entrySet()) {
            builder.append(indent(2));
            builder.append(entry.getKey());
            builder.append("'");
            List<Expression> parameters = this.parameters.get(entry.getKey());
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
            builder.append(UtilExpression.toString(entry.getValue()));
            builder.append(";\n");
        }
        builder.append(indent(1) + "};\n\n");
	}

	private void appendTypes(StringBuilder builder) {
    	assert builder != null;
        if (types.size() <= 4) {
        	return;
        }
        builder.append(indent(1) + TYPES + " {\n");
        for (Entry<String, Type> entry : types.entrySet()) {
        	String name = entry.getKey();
        	Type type = entry.getValue();
        	if ((name.length() > 0 && name.charAt(0) == '%')
        			|| name.equals(ModelBuilder.BOOL)
        			|| name.equals(ModelBuilder.INT)
        			|| name.equals(ModelBuilder.REAL)) {
        		continue;
        	}
        	builder.append(indent(2));
        	builder.append(name + " : ");
        	if (type instanceof TypeRDDLEnum) {
        		TypeRDDLEnum typeEnum = (TypeRDDLEnum) type;
        		builder.append(typeEnum.definitionToString());
        	} else if (type instanceof TypeRDDLObject) {
        		builder.append(OBJECT);
        	}
        	builder.append(";\n");
        }
        builder.append(indent(1) + "};\n\n");
	}

	private void appendRequirements(StringBuilder builder) {
    	assert builder != null;
        builder.append(indent(1) + REQUIREMENTS + " = {\n");
        appendRequirement(builder, rewardDeterministic, REWARD_DETERMINISTIC);
        appendRequirement(builder, usesIntegers, INTEGER_VALUED);
        appendRequirement(builder, continuousVariables, CONTINUOUS);
        appendRequirement(builder, usesEnums, MULTIVALUED);
        appendRequirement(builder, hasIntermediateNodes, INTERMEDIATE_NODES);
        appendRequirement(builder, partiallyObservable, PARTIALLY_OBSERVED);
        appendRequirement(builder, hasStateActionConstraints, CONSTRAINED_STATE);
        appendRequirement(builder, multiAction, CONCURRENT);
        appendRequirement(builder, !probabilistic, CPF_DETERMINISTIC);
        builder.replace(builder.length() - 2, builder.length(), "\n");
        builder.append("    };\n\n");
	}

	private void appendRequirement(StringBuilder builder,
			boolean condition, String string) {
    	assert builder != null;
    	assert string != null;
        if (condition) {
            builder.append(indent(2) + string + ",\n");
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
	
	public List<Expression> getStateActionConstraints() {
		return stateActionConstraintExternal;
	}
}
