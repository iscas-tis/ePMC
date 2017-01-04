package epmc.rddl.model;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNode;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.options.Options;
import epmc.rddl.expression.EvaluatorRDDL;
import epmc.rddl.options.OptionsRDDL;
import epmc.rddl.options.RDDLIntRange;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeObject;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;

final class ExplorerRDDL implements Explorer {
	private class ExplorerNodePropertyNode implements ExplorerNodeProperty {
		private final Type type;
		private final Value value;
		
		ExplorerNodePropertyNode() {
			this.type = new TypeObject.Builder()
	                .setContext(contextValue)
	                .setClazz(ExplorerNode.class)
	                .build();
			this.value = type.newValue();
		}
		
		@Override
		public Explorer getExplorer() {
            return ExplorerRDDL.this;
		}

		@Override
		public Value get() throws EPMCException {
			ValueObject.asObject(this.value).set(ExplorerRDDL.this.node);
			return value;
		}

		@Override
		public Type getType() {
			return this.type;
		}		
	}
	
    private final class ExplorerNodePropertyExpression implements ExplorerNodeProperty {
        @Override
        public Explorer getExplorer() {
            return ExplorerRDDL.this;
        }

        @Override
        public Value get() throws EPMCException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Type getType() {
            // TODO Auto-generated method stub
            return null;
        }

    }
    
    private class ExplorerNodePropertyState implements ExplorerNodeProperty {
        private final ValueBoolean value = TypeBoolean.get(contextValue).newValue();
        
        @Override
        public Explorer getExplorer() {
            return ExplorerRDDL.this;
        }

        @Override
        public ValueBoolean get() throws EPMCException {
            this.value.set(queryNodeIsState);
            return value;
        }

        @Override
        public Type getType() {
            return TypeBoolean.get(contextValue);
        }
    }
    
    private class ExplorerEdgePropertyWeight implements ExplorerEdgeProperty {
		@Override
		public Explorer getExplorer() {
			return ExplorerRDDL.this;
		}

		@Override
		public Value get(int successor) throws EPMCException {
			return ExplorerRDDL.this.weight[successor];
		}

		@Override
		public Type getType() {
			return TypeWeight.get(ExplorerRDDL.this.contextValue);
		}

    }

    private final ContextValue contextValue;
    private final Options options;
    private int[] stateExpressionNumbers;
    private final List<Object> graphProperties = new ArrayList<>();
    private final List<Object> graphPropertiesExternal = Collections.unmodifiableList(graphProperties);
    private final List<Object> nodeProperties = new ArrayList<>();
    private final List<Object> nodePropertiesExternal = Collections.unmodifiableList(nodeProperties);
    private final List<Object> edgeProperties = new ArrayList<>();
    private final List<Object> edgePropertiesExternal = Collections.unmodifiableList(edgeProperties);
    private ExplorerNodePropertyState nodePropertyState;
    private boolean queryNodeIsNondet;
    private boolean queryNodeIsState;
    private EvaluatorRDDL evaluator;
    private final Instance instance;
    private int numSuccessors;
    private ExplorerNodeRDDL currentSuccessor;
    private ExplorerNodeRDDL[] successorNodes;
    private int numNodeBits;
    private ValueAlgebra currentWeight;
    private ValueAlgebra[] weight;
	private ExplorerEdgePropertyWeight weightProperty;
	private int[] relevantDistributionIdentifiers;
	private int relevantDistributionIdentifiersLength;
	private boolean subsumeSuccessors;
	private TObjectIntMap<ExplorerNodeRDDL> existingSuccessorNodes = new TObjectIntHashMap<>();
	private ExplorerNodeRDDL node;
	private int[] stateActionConstraints;
	private ModelPreparator modelPreparator;
    
    public ExplorerRDDL(Instance instance)
            throws EPMCException {
    	this.modelPreparator = new ModelPreparator(instance);
        this.instance = instance;
        Domain domain = instance.getDomain();
        this.contextValue = domain.getContextValue();
        this.options = contextValue.getOptions();
        this.subsumeSuccessors = options.getBoolean(OptionsRDDL.RDDL_SUBSUME_SUCCESSORS_POST);
        prepareEvaluator(this.modelPreparator.getAssignments());
        prepareSuccessors();
    }
        
	private void prepareSuccessors() {
        this.relevantDistributionIdentifiers = new int[modelPreparator.getDistributionIdentifiers().size()];
        this.currentSuccessor = new ExplorerNodeRDDL(this);
        this.successorNodes = new ExplorerNodeRDDL[1];
        this.successorNodes[0] = new ExplorerNodeRDDL(this);
        this.currentWeight = newValueWeight();
        this.weight = new ValueAlgebra[1];
        this.weight[0] = newValueWeight();
        this.numNodeBits = computeNumNodeBits();
        this.weightProperty = new ExplorerEdgePropertyWeight();
	}

	private void prepareEvaluator(Map<StateFluent, Expression> assignments)
			throws EPMCException {
		assert assignments != null;
        this.evaluator = new EvaluatorRDDL(this.contextValue, this.modelPreparator.getAllVariableIdentifiers(), this.modelPreparator.getOffsetComputer().getOffsetComputer());
        for (int i = 0; i < this.modelPreparator.getStateExpressions().length; i++) {
        	this.evaluator.addExpression(this.modelPreparator.getStateExpressions()[i]);
        }
        for (ModelPreparator.DistributionIdentifier identifier : this.modelPreparator.getDistributionIdentifiers()) {
        	for (Expression probability : identifier.getProbabilities()) {
        		this.evaluator.addExpression(probability);
        	}
        }
        for (Expression constaint : this.modelPreparator.getStateActionConstraintsList()) {
        	this.evaluator.addExpression(constaint);
        }
        this.evaluator.prepare();
        this.stateActionConstraints = new int[this.modelPreparator.getStateActionConstraintsList().size()];
        for (int constrNr = 0; constrNr < this.modelPreparator.getStateActionConstraintsList().size(); constrNr++) {
        	this.stateActionConstraints[constrNr] = this.evaluator.getExpressionNumber(this.modelPreparator.getStateActionConstraintsList().get(constrNr));
        }
        this.stateExpressionNumbers = new int[this.modelPreparator.getStateExpressions().length];
        for (int exprNr = 0; exprNr < this.modelPreparator.getStateExpressions().length; exprNr++) {
        	this.stateExpressionNumbers[exprNr] = this.evaluator.getExpressionNumber(this.modelPreparator.getStateExpressions()[exprNr]);
        }
	}

	private int computeNumCopies(PVariable stateFluent) {
    	assert stateFluent != null;
    	List<RDDLObject> parameters = stateFluent.getParameters(this.instance);
    	int size = 1;
    	for (RDDLObject parameter : parameters) {
    		size *= parameter.numValues();
    	}
    	return size;
	}

	private int computeNumNodeBits() {
        int result = 0;
        for (Type type : modelPreparator.getStateFluentTypes()) {
            int numBits = TypeNumBitsKnown.getNumBits(type);
            if (numBits == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            result += TypeNumBitsKnown.getNumBits(type);
        }
        return result;
    }

    @Override
    public Collection<ExplorerNode> getInitialNodes()
            throws EPMCException {
    	int numStateFluents = this.modelPreparator.isNonDet() ? 1 : 0;
    	for (StateFluent stateFluent : this.modelPreparator.getStateFluents()) {
    		numStateFluents += computeNumCopies(stateFluent);
    	}
        Value[] initialValues = new Value[numStateFluents];
        int number = 0;
        if (this.modelPreparator.isNonDet()){
            TypeAlgebra type = TypeAlgebra.asAlgebra(this.modelPreparator.getStateFluentTypes()[number]);
            initialValues[number] = UtilValue.newValue(type, -1);
        	number++;
        }
        for (StateFluent stateFluent : this.modelPreparator.getStateFluents()) {
        	int size = computeNumCopies(stateFluent);
            Type type = computeType(stateFluent);
        	int[] paramValues = newAssignmentArray(stateFluent);
        	for (int assignment = 0; assignment < size; assignment++) {
        		setAssignmentFromInt(paramValues, assignment, stateFluent);
        		initialValues[number] = type.newValue();
        		initialValues[number].set(stateFluent.getDefault());
        		number++;
        	}
        }
        
        number = this.modelPreparator.isNonDet() ? 1 : 0;
        TObjectIntMap<StateFluent> stateFluentBeginToNumber = new TObjectIntHashMap<>(100, 0.5f, -1);
        for (StateFluent stateFluent : this.modelPreparator.getStateFluents()) {
            stateFluentBeginToNumber.put(stateFluent, number);
            number += computeNumCopies(stateFluent);
        }
        for (Entry<PVariableEntry, Value> entry : instance.getInitialValues().entrySet()) {
        	PVariableEntry pVariableEntry = entry.getKey();
            number = stateFluentBeginToNumber.get(pVariableEntry.getPVariable());
            int offset = computePVariableEntryOffset(pVariableEntry);
            initialValues[number + offset].set(entry.getValue());
        }
        ExplorerNodeRDDL initialNode = new ExplorerNodeRDDL(this, initialValues);
        return Collections.singleton(initialNode);
    }

    private Type computeType(PVariable variable) {
    	assert variable != null;
    	Type type = variable.getType();
    	assert type != null;
        if (TypeInteger.isInteger(type)) {
        	RDDLIntRange range = this.modelPreparator.getIntegerRanges().get(variable.getName());
        	if (range == null) {
        		return type;
        	}
        	assert range != null || (variable instanceof IntermediateFluent) : variable;
        	return TypeInteger.get(contextValue, range.getLower(), range.getUpper());
        } else {
        	return type;
        }
	}

	private int[] newAssignmentArray(PVariable pVariable) {
    	assert pVariable != null;
    	return new int[pVariable.getParameterStrings().size()];
	}

	private int computePVariableEntryOffset(PVariableEntry pVariableEntry) {
    	assert pVariableEntry != null;
    	List<RDDLObjectValue> parameters = pVariableEntry.getParameters(this.instance);
    	int start = 1;
    	int result = 0;
    	for (RDDLObjectValue value : parameters) {
    		result += value.getNumber() * start;
    		start *= value.getObject().numValues();
    	}
		return result;
	}

	private void setAssignmentFromInt(int[] assignment, int number, PVariable variable) {
    	int remaining = number;
    	int parameterNr = 0;
    	for (RDDLObject parameter : variable.getParameters(this.instance)) {
    		int paramValue = remaining % parameter.numValues();
    		remaining /= parameter.numValues();
    		assignment[parameterNr] = paramValue;
    		parameterNr++;
    	}
    }

	@Override
    public void queryNode(ExplorerNode node) throws EPMCException {
        assert node != null;
        assert node instanceof ExplorerNodeRDDL;
        assert node.getExplorer() == this;
        ExplorerNodeRDDL explorerNode = (ExplorerNodeRDDL) node;
        this.node = explorerNode;
        int actionNumber;
        if (this.modelPreparator.isNonDet()) {
        	actionNumber = ValueInteger.asInteger(explorerNode.getValues()[0]).getInt();
        } else {
        	actionNumber = 0;
        }
        this.queryNodeIsNondet = actionNumber == -1;
        this.queryNodeIsState = !this.modelPreparator.isNonDet() || queryNodeIsNondet;
        if (queryNodeIsNondet) {
        	queryNodeNonDet(explorerNode);
        } else {
        	queryNodeStochastic(actionNumber, explorerNode);
        }
    }

    private void queryNodeStochastic(int actionNumber, ExplorerNodeRDDL explorerNode)
    		throws EPMCException {
    	initEvaluatorStateValues(explorerNode);
    	initEvaluatorActionValues(actionNumber, explorerNode);
    	initialiseDistributionIdentifierValues();
    	
		boolean finished = false;
		int valueNr = 0;
		this.existingSuccessorNodes.clear();
		while (!finished) {
    		this.currentWeight.set(1);
    		for (int i = 0; i < this.modelPreparator.getDistributionIdentifiers().size(); i++) {
    			int v = ValueInteger.asInteger(this.modelPreparator.getAllVariableValues()[this.modelPreparator.getStateVariableIdentifiers().length + this.modelPreparator.getActionVariableIdentifiers().length + i]).getInt();
    			Expression probExpr = this.modelPreparator.getDistributionIdentifiers().get(i).probabilities.get(v);
    			this.evaluator.evaluate(probExpr);
    			Value resultValue = this.evaluator.getResultValue(probExpr);
    			this.currentWeight.multiply(currentWeight, resultValue);
    		}
    		if (this.currentWeight.isZero()) {
        		finished = nextDistributionValues();
        		continue;
    		}
    		this.evaluator.setVariableValues(this.modelPreparator.getAllVariableValues());
    		Value[] currentSuccessorValues = this.currentSuccessor.getValues();
    		for (int varNr = 0; varNr < currentSuccessorValues.length; varNr++) {
    			this.modelPreparator.getOffsetComputer().setParameters(this.modelPreparator.getAllVariablesParameters()[varNr]);
    			this.evaluator.evaluate(this.stateExpressionNumbers[varNr]);
    			currentSuccessorValues[varNr].set(this.evaluator.getResultValue(stateExpressionNumbers[varNr]));
    		}
    		if (this.modelPreparator.isNonDet()) {
    			ValueAlgebra.asAlgebra(this.currentSuccessor.getValues()[0]).set(-1);
    		}
    		if (storeSuccessor(valueNr)) {
    			valueNr++;
    		}
    		finished = nextDistributionValues();
		}
	}

	private boolean storeSuccessor(int valueNr) throws EPMCException {
		if (!this.subsumeSuccessors) {
			this.weight[valueNr].set(this.currentWeight);
			this.numSuccessors = valueNr + 1;
			ensureSuccessorsSize();
			this.successorNodes[valueNr].set(currentSuccessor);
			return true;
		} else {
			if (this.existingSuccessorNodes.containsKey(currentSuccessor)) {
				int prevSuccNr = this.existingSuccessorNodes.get(currentSuccessor);
				this.weight[prevSuccNr].add(this.weight[prevSuccNr], this.currentWeight);
				return false;
			} else {
				this.numSuccessors = valueNr + 1;
				ensureSuccessorsSize();
				this.weight[valueNr].set(this.currentWeight);
				this.successorNodes[valueNr].set(currentSuccessor);
				this.existingSuccessorNodes.put(this.successorNodes[valueNr], valueNr);
				return true;
			}
		}
	}

	private void initEvaluatorStateValues(ExplorerNodeRDDL explorerNode)
			throws EPMCException {
		int number = 0;
    	for (int i = 0; i < explorerNode.getValues().length; i++) {
    		this.modelPreparator.getAllVariableValues()[number].set(explorerNode.getValues()[i]);
    		number++;
    	}
	}
	
	private void initEvaluatorActionValues(int actionNumber, ExplorerNodeRDDL explorerNode)
			throws EPMCException {
		int number = explorerNode.getValues().length;
    	for (int i = 0; i < this.modelPreparator.getPossibleActionValues()[actionNumber].length; i++) {
    		this.modelPreparator.getAllVariableValues()[number].set(this.modelPreparator.getPossibleActionValues()[actionNumber][i]);
    		number++;
    	}
		this.evaluator.setVariableValues(this.modelPreparator.getAllVariableValues());
	}

	private void queryNodeNonDet(ExplorerNodeRDDL node) throws EPMCException {
		assert node != null;
        int succNr = 0;
        this.numSuccessors = 0;
        ensureSuccessorsSize();
    	initEvaluatorStateValues(node);
    	TIntIntMap count = new TIntIntHashMap();
        for (int actionNr = 0; actionNr < this.modelPreparator.getPossibleActionValues().length; actionNr++) {
        	initEvaluatorActionValues(actionNr, node);
        	boolean allOK = true;
        	int cnr = 0;
        	for (int constraint : this.stateActionConstraints) {
        		if (!this.evaluator.evaluateBoolean(constraint)) {
        			count.put(cnr, count.get(cnr) + 1);
        			allOK = false;
        			break;
        		}
        		cnr++;
        	}
        	if (!allOK) {
        		continue;
        	}
            this.numSuccessors = succNr + 1;
            ensureSuccessorsSize();
        	this.weight[succNr].set(-1);
            this.successorNodes[succNr].set(node);
            ValueAlgebra.asAlgebra(this.successorNodes[succNr].getValues()[0]).set(actionNr);
            succNr++;
        }
        this.numSuccessors = succNr;
	}

	private void initialiseDistributionIdentifierValues() {
		for (int i = 0; i < this.modelPreparator.getDistributionIdentifiers().size(); i++) {
			ValueAlgebra.asAlgebra(this.modelPreparator.getAllVariableValues()[this.modelPreparator.getStateVariableIdentifiers().length + this.modelPreparator.getActionVariableIdentifiers().length + i]).set(0);
		}
	}

	private boolean nextDistributionValues() {
		computeRelevantDistributionIdentifiers();
		boolean finished = true;
		for (int i = 0; i < this.relevantDistributionIdentifiersLength; i++) {
			int identifierNumber = this.relevantDistributionIdentifiers[i];
			int numValues = this.modelPreparator.getDistributionIdentifiers().get(identifierNumber).getNumProbabilities();
			ValueAlgebra value = ValueAlgebra.asAlgebra(this.modelPreparator.getAllVariableValues()[this.modelPreparator.getStateVariableIdentifiers().length + this.modelPreparator.getActionVariableIdentifiers().length + identifierNumber]);
			int currentValue = ValueInteger.asInteger(value).getInt();
			currentValue++;
			value.set(currentValue);
			if (currentValue == numValues) {
				currentValue = 0;
				value.set(currentValue);
			} else {
				finished = false;
				break;
			}
		}
		return finished;
	}

	private void computeRelevantDistributionIdentifiers() {
		this.relevantDistributionIdentifiersLength = this.modelPreparator.getDistributionIdentifiers().size();
		for (int i = 0; i < relevantDistributionIdentifiersLength; i++) {
			this.relevantDistributionIdentifiers[i] = i;
		}
	}

	private void ensureSuccessorsSize() {
    	int oldSize = successorNodes.length;
    	int newSize = successorNodes.length;
    	assert this.numSuccessors >= 0 : this.numSuccessors;
    	if (newSize >= this.numSuccessors) {
    		return;
    	}
    	while (newSize < this.numSuccessors) {
    		newSize *= 2;
    	}
    	this.successorNodes = Arrays.copyOf(this.successorNodes, newSize);
    	this.weight = Arrays.copyOf(this.weight, newSize);
    	for (int i = oldSize; i < newSize; i++) {
    		this.successorNodes[i] = new ExplorerNodeRDDL(this);
    		this.weight[i] = newValueWeight();
    	}
	}

	@Override
    public int getNumSuccessors() {
        return numSuccessors;
    }

    @Override
    public ExplorerNode getSuccessorNode(int number) {
        assert number >= 0 : number;
        assert number < numSuccessors : number + " " + numSuccessors;
        return this.successorNodes[number];
    }

    @Override
    public Value getGraphProperty(Object property) {
        if (property == CommonProperties.SEMANTICS) {
            return this.modelPreparator.getValueSemantics();
        } else {
            assert false;
            return null;
        }
    }

    @Override
    public ExplorerNodeProperty getNodeProperty(Object property) {
        if (property == CommonProperties.STATE) {
            return this.nodePropertyState;
        } else {
            assert false;
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
    	if (property == CommonProperties.WEIGHT) {
    		return weightProperty;
    	}
    	assert false;
    	return null;
    }

    @Override
    public ExplorerNode newNode() {
        return new ExplorerNodeRDDL(this);
    }

    @Override
    public int getNumNodeBits() {
        return this.numNodeBits;
    }

    Type[] getStateFluentTypes() {
        return this.modelPreparator.getStateFluentTypes();
    }

	ValueAlgebra newValueWeight() {
		return TypeWeight.get(getContextValue()).newValue();
	}
	
	@Override
	public void close() {
	}

	@Override
	public Type getType(Expression expression) throws EPMCException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}
}
