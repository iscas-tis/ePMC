package epmc.rddl.model;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMCStandard;
import epmc.graph.SemanticsMDP;
import epmc.options.Options;
import epmc.rddl.expression.ExpressionRDDLQuantifiedIdentifier;
import epmc.rddl.expression.ExpressionRDDLQuantifier;
import epmc.rddl.options.OptionsRDDL;
import epmc.rddl.options.RDDLIntRange;
import epmc.rddl.value.OperatorDistributionBernoulli;
import epmc.rddl.value.OperatorDistributionDiracDelta;
import epmc.rddl.value.OperatorDistributionDiscrete;
import epmc.rddl.value.OperatorDistributionKronDelta;
import epmc.rddl.value.OperatorSwitch;
import epmc.rddl.value.TypeRDDLObject;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorSubtract;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnumerable;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueEnumerable;

final class ModelPreparator {
	final class DistributionIdentifier {
		private final String name;
		private final Expression identifier;
		final List<Expression> probabilities = new ArrayList<>();
		private final List<Expression> probabilitiesExternal = Collections.unmodifiableList(probabilities);
		
		DistributionIdentifier(String name, List<Expression> probabilities) {
			this.name = name;
			this.identifier = new ExpressionIdentifierStandard.Builder()
					.setName(name)
					.build();
			this.probabilities.addAll(probabilities);
		}
		
		String getName() {
			return this.name;
		}
		
		Expression getIdentifier() {
			return this.identifier;
		}
		
		int getNumProbabilities() {
			return this.probabilities.size();
		}
		
		List<Expression> getProbabilities() {
			return probabilitiesExternal;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			builder.append(identifier);
			builder.append(",");
			builder.append(probabilities);
			builder.append(")");
			return builder.toString();
		}

		public void replaceProbabilities(List<Expression> newProbs) {
			assert newProbs != null;
			for (Expression prob : newProbs) {
				assert prob != null;
			}
			this.probabilities.clear();
			this.probabilities.addAll(newProbs);
		}
	}

	private final static String UNDERSCORE = "_";
	private final static String NONDET_VAR = "%nondet";
	private final static String DISTRID_VAR = "%distrid";
	private Domain domain;
	private Instance instance;
	private ContextValue contextValue;
	private Options options;
	private Map<String,RDDLIntRange> integerRanges;
	private boolean isNonDet;
    private PVariable[] actionVariables;
    private final Semantics semantics;
    private final Value valueSemantics;
	private final List<StateFluent> stateFluents = new ArrayList<>();
	private Map<PVariable,Map<Expression,RDDLObject>> cpfsParameters;
	private List<Expression> stateActionConstraintsList = new ArrayList<>();
    private Map<Expression,DistributionIdentifier> distrIdentifierMap;
    private Type[] actionFluentTypes;
    private ValueEnumerable[] defaultActionValues;
    private Expression[] actionVariableIdentifiers;
    private Value[][] possibleActionValues;
    private List<DistributionIdentifier> distributionIdentifiers = new ArrayList<>();
	private int renameParamNumber;
    private Type[] stateFluentTypes;
    private Expression[] stateVariableIdentifiers;
    private Expression[] stateExpressions;
    private PVariable[] stateVariables;
    private Type[] nonFluentTypes;
    private Expression[] nonFluentVariableIdentifiers;
    private PVariable[] nonFluentVariables;
    private Value[] nonFluentValues;
    private Expression[] allVariableIdentifiers;
    private Value[] allVariableValues;
    private PVariable[] allVariables;
    private ModelOffsetComputer offsetComputer;
    private int[][] allVariablesParameters;
	private Map<StateFluent, Expression> assignments;

	ModelPreparator(Instance instance) throws EPMCException {
		assert instance != null;
        this.domain = instance.getDomain();
        this.instance = instance;
        this.contextValue = domain.getContextValue();
        this.options = contextValue.getOptions();
        this.integerRanges = options.get(OptionsRDDL.RDDL_INT_RANGE);
        assert this.integerRanges != null;
        prepareActionVariables();
        this.semantics = this.isNonDet ? SemanticsMDP.MDP : SemanticsDTMCStandard.DTMC;
        this.valueSemantics = new TypeObject.Builder()
        		.setContext(contextValue)
        		.setClazz(Semantics.class)
        		.build().newValue(semantics);
        this.valueSemantics.setImmutable();
        this.stateFluents.addAll(domain.getStateFluents().values());
        this.cpfsParameters = collectCpfsParameters();
        this.stateActionConstraintsList.addAll(domain.getStateActionConstraints());
        this.distrIdentifierMap = new HashMap<>();

        this.assignments = prepareAssignments();
        replaceStateActionConstraintParameters();
        prepareStateVariables(assignments);
        prepareNonFluentVariables();
        prepareAllVariables();
        prepareOffsets(assignments);
	}

	List<StateFluent> getStateFluents() {
		return stateFluents;
	}
	
	List<Expression> getStateActionConstraintsList() {
		return stateActionConstraintsList;
	}
	
	Map<String, RDDLIntRange> getIntegerRanges() {
		return integerRanges;
	}
	
	boolean isNonDet() {
		return isNonDet;
	}
	
	List<DistributionIdentifier> getDistributionIdentifiers() {
		return distributionIdentifiers;
	}
	
	Value getValueSemantics() {
		return valueSemantics;
	}
	
	Value[] getAllVariableValues() {
		return allVariableValues;
	}
	
	int[][] getAllVariablesParameters() {
		return allVariablesParameters;
	}
	
	Expression[] getAllVariableIdentifiers() {
		return allVariableIdentifiers;
	}
	
	Value[][] getPossibleActionValues() {
		return possibleActionValues;
	}
	
	Expression[] getActionVariableIdentifiers() {
		return actionVariableIdentifiers;
	}
	
	Type[] getStateFluentTypes() {
		return stateFluentTypes;
	}
	
	Expression[] getStateExpressions() {
		return stateExpressions;
	}
	
	Map<StateFluent, Expression> getAssignments() {
		return assignments;
	}
	
	private void prepareOffsets(Map<StateFluent, Expression> assignments)
			throws EPMCException {
		assert assignments != null;
        this.offsetComputer = new ModelOffsetComputer(instance);
        Set<String> alreadySeen = new LinkedHashSet<>();
		for (int variableNr = 0; variableNr < this.allVariables.length; variableNr++) {
			PVariable variable = this.allVariables[variableNr];
			if (alreadySeen.contains(variable.getName())) {
				continue;
			}
	    	this.offsetComputer.addVariable(variable);
	    	alreadySeen.add(variable.getName());
		}
		Map<Expression, RDDLObject> parameterVariables = collectParameters(assignments);
        for (Entry<Expression, RDDLObject> entry : parameterVariables.entrySet()) {
//        	entry.getKey().getContext().registerType(entry.getKey(), TypeInteger.get(contextValue));
            this.offsetComputer.addParameter((ExpressionIdentifierStandard) entry.getKey(), entry.getValue());
        }
        this.offsetComputer.build();
        this.allVariablesParameters = new int[this.allVariableIdentifiers.length][];
        int varNr = this.isNonDet ? 1 : 0;
        for (Entry<StateFluent, Expression> entry : assignments.entrySet()) {
        	PVariable pVariable = entry.getKey();
        	Map<Expression, RDDLObject> parametersM = this.cpfsParameters.get(pVariable);
        	List<Expression> parameters = new ArrayList<>();
        	parameters.addAll(parametersM.keySet());
    		int[] paramAsg = newAssignmentArray(pVariable);
    		int size = computeNumCopies(pVariable);
            for (int valueNumber = 0; valueNumber < size; valueNumber++) {
            	setAssignmentFromInt(paramAsg, valueNumber, pVariable);
            	for (int paramNr = 0; paramNr < parameters.size(); paramNr++) {
                	Expression variable = parameters.get(paramNr);
            		this.offsetComputer.setParameter((ExpressionIdentifierStandard) variable, paramAsg[paramNr]);
            	}
            	this.allVariablesParameters[varNr] = this.offsetComputer.getParameters().clone();
            	varNr++;
            }
        }
		for (int variableNr = 0; variableNr < this.allVariables.length; variableNr++) {
			if (this.allVariablesParameters[variableNr] == null) {
				this.allVariablesParameters[variableNr] = new int[0];
			}
		}
	}

	private Map<Expression, RDDLObject> collectParameters(Map<StateFluent,Expression> assignments) {
    	assert assignments != null;
        Map<Expression,RDDLObject> parameters = new LinkedHashMap<>();
        for (StateFluent stateFluent : assignments.keySet()) {
            parameters.putAll(this.cpfsParameters.get(stateFluent));
        }
        for (StateFluent stateFluent : assignments.keySet()) {
    		collectParameterVariables(parameters, assignments.get(stateFluent));
        }
        for (Expression constaint : this.stateActionConstraintsList) {
    		collectParameterVariables(parameters, constaint);
        }
        for (DistributionIdentifier identifier : this.distributionIdentifiers) {
        	for (Expression probability : identifier.getProbabilities()) {
        		collectParameterVariables(parameters, probability);
        	}
        }
        return parameters;
	}
	
	private void collectParameterVariables(Map<Expression,RDDLObject> parameterVariables,
			Expression expression) {
    	if (expression instanceof ExpressionOperator) {
    		List<Expression> operands = expression.getChildren();
    		for (Expression operand : operands) {
    			collectParameterVariables(parameterVariables, operand);
    		}
    	} else if (expression instanceof ExpressionIdentifierStandard) {
    	} else if (expression instanceof ExpressionLiteral) {
    	} else if (expression instanceof ExpressionRDDLQuantifier) {
    		ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
    		for (Entry<Expression, TypeRDDLObject> entry : quantifier.getParameterMap().entrySet()) {
    			Expression name = entry.getKey();
    			RDDLObject object = instance.getObject(entry.getValue().getName());
    			parameterVariables.put(name, object);
    		}
    		collectParameterVariables(parameterVariables, quantifier.getOver());
    	} else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
    	} else {
    		assert false : expression;
    	}
	}

	Expression[] getStateVariableIdentifiers() {
		return stateVariableIdentifiers;
	}
	
	private void prepareAllVariables() throws EPMCException {
    	int totalSize = 0;
    	totalSize += this.stateVariableIdentifiers.length;
    	totalSize += this.actionVariableIdentifiers.length;
    	totalSize += this.distributionIdentifiers.size();
    	totalSize += this.nonFluentVariableIdentifiers.length;
        this.allVariableIdentifiers = new Expression[totalSize];
        this.allVariableValues = new Value[totalSize];
        this.allVariables = new PVariable[totalSize];
        int number = 0;
        for (int variableNr = 0; variableNr < stateVariableIdentifiers.length; variableNr++) {
        	assert this.stateVariableIdentifiers[variableNr] != null : variableNr;
        	this.allVariableIdentifiers[number] = this.stateVariableIdentifiers[variableNr];
        	this.allVariables[number] = this.stateVariables[variableNr];
        	// TODO
        	this.allVariableValues[number] = this.allVariableIdentifiers[number].getType(null).newValue();
            number++;
        }
        for (int variableNr = 0; variableNr < actionVariableIdentifiers.length; variableNr++) {
        	this.allVariableIdentifiers[number] = this.actionVariableIdentifiers[variableNr];
        	this.allVariables[number] = this.actionVariables[variableNr];
        	// TODO
        	this.allVariableValues[number] = this.allVariableIdentifiers[number].getType(null).newValue();
            number++;
        }
        for (DistributionIdentifier distr : this.distributionIdentifiers) {
        	this.allVariableIdentifiers[number] = distr.getIdentifier();
        	IntermediateFluent distrVar = new IntermediateFluent();
        	distrVar.setName(distr.getName());
        	distrVar.setParameters(new ArrayList<>());
        	distrVar.setType(TypeInteger.get(contextValue, -1, distr.getNumProbabilities() - 1));
        	this.allVariables[number] = distrVar;
        	// TODO
        	this.allVariableValues[number] = this.allVariableIdentifiers[number].getType(null).newValue();
            number++;
        }
        for (int variableNr = 0; variableNr < this.nonFluentVariableIdentifiers.length; variableNr++) {
        	this.allVariableIdentifiers[number] = this.nonFluentVariableIdentifiers[variableNr];
        	this.allVariables[number] = this.nonFluentVariables[variableNr];
        	this.allVariableValues[number] = this.nonFluentValues[variableNr];
            number++;
        }
        for (int variableNr = 0; variableNr < this.allVariableIdentifiers.length; variableNr++) {
        	assert this.allVariableIdentifiers[variableNr] != null : variableNr;
        	// TODO
        	assert this.allVariableIdentifiers[variableNr].getType(null) != null : variableNr;
        }
	}
	
	private void prepareNonFluentVariables() {
		int numNonFluents = 0;
        for (NonFluent nonFluent : domain.getNonFluents().values()) {
        	numNonFluents += computeNumCopies(nonFluent);
        }
        this.nonFluentTypes = new Type[numNonFluents];
        this.nonFluentVariableIdentifiers = new Expression[numNonFluents];
        this.nonFluentValues = new Value[numNonFluents];
        this.nonFluentVariables = new PVariable[numNonFluents];
        int number = 0;
        for (NonFluent nonFluent : domain.getNonFluents().values()) {
    		int[] paramAsg = newAssignmentArray(nonFluent);
    		int nonFluentSize = computeNumCopies(nonFluent);
    		List<RDDLObject> parameters = nonFluent.getParameters(this.instance);
    		Type type = computeType(nonFluent);
    		assert nonFluentSize > 0;
            for (int valueNumber = 0; valueNumber < nonFluentSize; valueNumber++) {
            	setAssignmentFromInt(paramAsg, valueNumber, nonFluent);
        		String parametrisedName = nonFluent.getName();
        		for (int varNr = 0; varNr < parameters.size(); varNr++) {
        			int paramValue = paramAsg[varNr];
        			parametrisedName += UNDERSCORE + paramValue;
        		}
        		this.nonFluentTypes[number] = type;
        		Expression identifier = new ExpressionIdentifierStandard.Builder()
        				.setName(parametrisedName)
        				.build();
        		this.nonFluentVariableIdentifiers[number] = identifier;
        		this.nonFluentVariables[number] = nonFluent;
//        		identifier.getContext().registerType(identifier, type);
        		this.nonFluentValues[number] = nonFluent.getDefault();
        		number++;
            }
        }
        
		NonFluents nonFluents = instance.getNonFluents();
        TObjectIntMap<NonFluent> nonFluentBeginToNumber = new TObjectIntHashMap<>(100, 0.5f, -1);
        number = 0;
        for (NonFluent stateFluent : domain.getNonFluents().values()) {
        	nonFluentBeginToNumber.put(stateFluent, number);
            number += computeNumCopies(stateFluent);
        }
		
        for (Entry<PVariableEntry, Value> entry : nonFluents.getNonFluentValues().entrySet()) {
        	PVariableEntry pVariableEntry = entry.getKey();
        	number = nonFluentBeginToNumber.get(pVariableEntry.getPVariable());
        	assert number >= 0;
        	int index = computePVariableEntryOffset(pVariableEntry);
        	this.nonFluentValues[number + index] = entry.getValue();
        }		
		
        for (NonFluent nonFluent : domain.getNonFluents().values()) {
        	Expression identifier = new ExpressionIdentifierStandard.Builder()
        			.setName(nonFluent.getName())
        			.build();
//        	identifier.getContext().registerType(identifier, computeType(nonFluent));
        }
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
	
	private void prepareStateVariables(
			Map<StateFluent, Expression> assignments) {
    	assert assignments != null;
        int numStateFluents = computeNumStateFluents();
        this.stateFluentTypes = new Type[numStateFluents];
        this.stateVariableIdentifiers = new Expression[numStateFluents];
        this.stateExpressions = new Expression[numStateFluents];
        this.stateVariables = new PVariable[numStateFluents];
        
        int number = 0;
        if (this.isNonDet) {
            Type type = TypeInteger.get(null, -1, this.possibleActionValues.length - 1);
            this.stateFluentTypes[number] = type;
            Expression nondetIdentifier = new ExpressionIdentifierStandard.Builder()
            		.setName(NONDET_VAR)
            		.build();
    		this.stateVariableIdentifiers[number] = nondetIdentifier;
//    		this.stateVariableIdentifiers[number].getContext().registerType(this.stateVariableIdentifiers[number], type);
    		this.stateExpressions[number] = nondetIdentifier;
    		StateFluent nondetStateFluent = new StateFluent();
    		nondetStateFluent.setName(NONDET_VAR);
    		nondetStateFluent.setType(TypeInteger.get(this.contextValue));
        	this.stateVariables[number] = nondetStateFluent;
        	number++;
        }
        for (Entry<StateFluent, Expression> entry : assignments.entrySet()) {
        	StateFluent stateFluent = entry.getKey();
        	Expression assignment = entry.getValue();
    		int[] paramAsg = newAssignmentArray(stateFluent);
    		int stateSize = computeNumCopies(stateFluent);
    		List<RDDLObject> parameters = stateFluent.getParameters(this.instance);
    		Type type = computeType(stateFluent);
    		assert stateSize > 0;
            for (int valueNumber = 0; valueNumber < stateSize; valueNumber++) {
            	setAssignmentFromInt(paramAsg, valueNumber, stateFluent);
        		String parametrisedName = stateFluent.getName();
        		for (int varNr = 0; varNr < parameters.size(); varNr++) {
        			int paramValue = paramAsg[varNr];
        			parametrisedName += UNDERSCORE + paramValue;
        		}
        		this.stateFluentTypes[number] = type;
        		Expression identifier = new ExpressionIdentifierStandard.Builder()
        				.setName(parametrisedName)
        				.build();
        		this.stateVariableIdentifiers[number] = identifier;
        		this.stateExpressions[number] = assignment;
        		this.stateVariables[number] = stateFluent;
//        		identifier.getContext().registerType(identifier, type);
        		number++;
            }
        }
	}

    private void replaceStateActionConstraintParameters() {
    	List<Expression> newStateActionConstraints = new ArrayList<>();
    	for (Expression constraint : this.stateActionConstraintsList) {
    		newStateActionConstraints.add(replaceParameters(constraint));
    	}
    	this.stateActionConstraintsList.clear();
    	this.stateActionConstraintsList.addAll(newStateActionConstraints);    	
    }

	private void prepareActionVariables() {
        int number;
        Collection<ActionFluent> actionFluents = domain.getActionFluents().values();
        int numActionFluents = 0;
        TIntList actionSizesList = new TIntArrayList();
        for (ActionFluent actionFluent : actionFluents) {
        	int size = computeNumCopies(actionFluent);
        	actionSizesList.add(size);
        	numActionFluents += size;
        }
        this.actionVariables = new PVariable[numActionFluents];
        this.actionFluentTypes = new Type[numActionFluents];
        this.defaultActionValues = new ValueEnumerable[numActionFluents];
        this.actionVariableIdentifiers = new Expression[numActionFluents];
        number = 0;
        int actionFluentNumber = 0;
        for (ActionFluent actionFluent : actionFluents) {
        	List<RDDLObject> parameters = actionFluent.getParameters(this.instance);
            Type type = computeType(actionFluent);
            String name = actionFluent.getName();
            Expression origId = new ExpressionIdentifierStandard.Builder()
            		.setName(name)
            		.build();
//            origId.getContext().registerType(origId, type);
            int actionSize = actionSizesList.get(actionFluentNumber);
            for (int valueNumber = 0; valueNumber < actionSize; valueNumber++) {
        		String parametrisedName = name;
        		int[] paramAsg = newAssignmentArray(actionFluent);
            	setAssignmentFromInt(paramAsg, valueNumber, actionFluent);
        		for (int varNr = 0; varNr < parameters.size(); varNr++) {
        			int paramValue = paramAsg[varNr];
        			parametrisedName += UNDERSCORE + paramValue;
        		}
        		this.actionFluentTypes[number] = type;
        		this.defaultActionValues[number] = TypeEnumerable.asEnumerable(type).newValue();
        		this.defaultActionValues[number].set(actionFluent.getDefault());
        		this.actionVariables[number] = actionFluent;
        		Expression identifier = new ExpressionIdentifierStandard.Builder()
        				.setName(parametrisedName)
        				.build();
        		this.actionVariableIdentifiers[number] = identifier;
//        		identifier.getContext().registerType(identifier, type);
        		number++;
            }
        	actionFluentNumber++;
        }
        this.possibleActionValues = computePossibleActionValues();
        this.isNonDet = this.possibleActionValues.length > 1
        		|| !options.getBoolean(OptionsRDDL.RDDL_ALLOW_DTMC_SEMANTICS);
	}
	
    private Map<PVariable, Map<Expression, RDDLObject>> collectCpfsParameters() {
    	Map<PVariable, Map<Expression, RDDLObject>> result = new LinkedHashMap<>();
    	for (PVariable variable : domain.getStateFluents().values()) {
    		result.put(variable, domain.getCpfsParameters(instance, variable));
    	}
    	for (PVariable variable : domain.getIntermediateFluents().values()) {
    		result.put(variable, domain.getCpfsParameters(instance, variable));
    	}
    	return result;
	}
    
	private Map<StateFluent, Expression> prepareAssignments() {
        List<IntermediateFluent> byLevel = orderByLevel(domain.getIntermediateFluents().values());
        Map<Expression,Expression> intermIdToExpression = new HashMap<>();
        Map<Expression,List<Expression>> intermIdParams =  new HashMap<>();
        buildIntermediateMaps(intermIdToExpression, intermIdParams, byLevel);
    	Map<StateFluent,Expression> result = new LinkedHashMap<>();
        for (StateFluent fluent : this.stateFluents) {
            Type type = computeType(fluent);
            String name = fluent.getName();
            Expression origId = new ExpressionIdentifierStandard.Builder()
            		.setName(name)
            		.build();
//            origId.getContext().registerType(origId, type);
    		Expression assignment = domain.getCpfsAssignment(name);
			assignment = replaceParameters(fluent, assignment);
    		assignment = replaceIntermediateExpressions(assignment, intermIdToExpression, intermIdParams);
    		assignment = replaceDistributions(assignment);
    		assert assignment != null;
    		result.put(fluent, assignment);
        }
		for (StateFluent fluent : this.stateFluents) {
			Expression assignment = result.get(fluent);
		}

		return result;
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
	
    private Type computeType(PVariable variable) {
    	assert variable != null;
    	Type type = variable.getType();
    	assert type != null;
        if (TypeInteger.isInteger(type)) {
        	RDDLIntRange range = this.integerRanges.get(variable.getName());
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
	
    private Value[][] computePossibleActionValues() {
        int maxNondefActions = instance.getMaxNonDefActions();
        List<boolean[]> nonDefault = new ArrayList<>();
        int actionsFluentsSize = actionFluentTypes.length;
        boolean[] currentVector = new boolean[actionsFluentsSize];
        for (int numNondefActions = 0; numNondefActions <= maxNondefActions; numNondefActions++) {
        	computeNonDefaultVector(nonDefault, numNondefActions, 0, actionsFluentsSize, currentVector);
        }
        
        List<Value[]> result = new ArrayList<>();
        for (boolean[] mask : nonDefault) {
            ValueEnumerable[] entry = new ValueEnumerable[defaultActionValues.length];
            for (int i = 0; i < defaultActionValues.length; i++) {
                entry[i] = UtilValue.clone(defaultActionValues[i]);
            }
            int numValues = 1;
            for (int i = 0; i < actionFluentTypes.length; i++) {
                if (mask[i]) {
                    int typeNumValues = TypeEnumerable.asEnumerable(actionFluentTypes[i]).getNumValues() - 1;
                    numValues *= typeNumValues;
                }
            }
            for (int valueNr = 0; valueNr < numValues; valueNr++) {
                int remaining = valueNr;
                for (int i = 0; i < actionFluentTypes.length; i++) {
                    if (mask[i]) {
                        TypeEnumerable type = TypeEnumerable.asEnumerable(actionFluentTypes[i]);
                        int typeNumValues = type.getNumValues() - 1;
                        int typeValueNr = remaining % typeNumValues;
                        if (typeValueNr >= ValueEnumerable.asEnumerable(defaultActionValues[i]).getValueNumber()) {
                            typeValueNr++;
                        }
                        remaining /= typeNumValues;
                        entry[i].setValueNumber(typeValueNr);
                    }
                }
                result.add(entry);
            }
        }

        return result.toArray(new Value[result.size()][]);
    }
    
    private void computeNonDefaultVector(List<boolean[]> result, int numNondefActions, int startPosition, int actionFluentsSize, boolean[] currentVector) {
        if (numNondefActions == 0) {
            result.add(currentVector.clone());
            return;
        }
//        for (int tryPosition = startPosition; tryPosition < actionFluentsSize - numNondefActions + 1; tryPosition++) {
        for (int tryPosition = startPosition; tryPosition < actionFluentsSize; tryPosition++) {
        	currentVector[tryPosition] = true;
            computeNonDefaultVector(result, numNondefActions - 1, tryPosition + 1, actionFluentsSize, currentVector);
            currentVector[tryPosition] = false;
        }
    }
    
    private List<IntermediateFluent> orderByLevel(
            Collection<IntermediateFluent> fluents) {
        TIntSet allLevels = new TIntHashSet();
        TIntObjectMap<List<IntermediateFluent>> levelToFluents = new TIntObjectHashMap<>();
        for (IntermediateFluent fluent : fluents) {
            int level = fluent.getLevel();
            allLevels.add(level);
            List<IntermediateFluent> levelFluents = levelToFluents.get(level);
            if (levelFluents == null) {
                levelFluents = new ArrayList<>();
                levelToFluents.put(level, levelFluents);
            }
            levelFluents.add(fluent);
        }
        int[] allLevelsArray = allLevels.toArray();
        Arrays.sort(allLevelsArray);
        List<IntermediateFluent> result = new ArrayList<>();
        for (int level : allLevelsArray) {
            result.addAll(levelToFluents.get(level));
        }
        return result;
    }
    
    private Expression replaceParameters(Expression expression) {
    	Map<Expression,Expression> replacement = new HashMap<>();
    	return replaceParameters(expression, replacement);
    }
    
    private Expression replaceParameters(PVariable variable, Expression expression) {
    	Map<Expression,Expression> replacement = new HashMap<>();
    	Map<Expression, RDDLObject> oldParams = this.cpfsParameters.get(variable);
    	Map<Expression, RDDLObject> newParams = new HashMap<>();
		for (Entry<Expression, RDDLObject> entry : oldParams.entrySet()) {
			ExpressionIdentifierStandard oldParameter = (ExpressionIdentifierStandard) entry.getKey();
			String newName = renameParameter(oldParameter.getName());
			Expression newParameter = new ExpressionIdentifierStandard.Builder()
					.setName(newName)
					.build();
			replacement.put(oldParameter, newParameter);
			newParams.put(newParameter, entry.getValue());
		}
		Expression newAssignment = replaceParameters(expression, replacement);
		this.cpfsParameters.put(variable, newParams);
		return newAssignment;
    }
    
    private Expression replaceParameters(Expression expression, Map<Expression, Expression> replacement) {
		assert expression != null;
		assert replacement != null;
		if (expression instanceof ExpressionIdentifierStandard
				&& replacement.containsKey(expression)) {
			return replacement.get(expression);
		} else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
			ExpressionRDDLQuantifiedIdentifier quantIdentifier = (ExpressionRDDLQuantifiedIdentifier) expression;
			return quantIdentifier.replaceParameters(replacement);
		} else if (expression instanceof ExpressionRDDLQuantifier) {
			ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
			Map<Expression,Expression> newReplacement = new HashMap<>();
			List<Expression> quantParameters = quantifier.getParameters();
			for (Entry<Expression, Expression> entry : replacement.entrySet()) {
				Expression origParam = entry.getKey();
				if (quantParameters.contains(origParam)) {
					newReplacement.put(origParam, renameParameter((ExpressionIdentifierStandard) origParam));
				} else {
					newReplacement.put(origParam, entry.getValue());
				}
			}
			for (Expression param : quantParameters) {
				if (newReplacement.containsKey(param)) {
					continue;
				}
				newReplacement.put(param, renameParameter((ExpressionIdentifierStandard) param));
			}
			Expression replacedOver = replaceParameters(quantifier.getOver(), newReplacement);
			Expression result = quantifier.replaceParameters(replacedOver, newReplacement);
			return result;
		} else {
			List<Expression> newChildren = new ArrayList<>();
			for (Expression child : expression.getChildren()) {
				newChildren.add(replaceParameters(child, replacement));
			}
			return expression.replaceChildren(newChildren);
		}
	}

	private String renameParameter(String oldName) {
    	assert oldName != null;
    	String result = oldName + UNDERSCORE + "$" + this.renameParamNumber;
    	this.renameParamNumber++;
    	return result;
    }
	
    private Expression renameParameter(ExpressionIdentifierStandard oldParameter) {
    	return new ExpressionIdentifierStandard.Builder()
    			.setName(renameParameter(oldParameter.getName()))
    			.build();
    }
	
	private void buildIntermediateMaps(Map<Expression, Expression> map,
			Map<Expression, List<Expression>> params,
			List<IntermediateFluent> byLevel) {
		Map<String,Expression> renAssg = new LinkedHashMap<>();
		for (IntermediateFluent fluent : byLevel) {
			ExpressionIdentifierStandard name = new ExpressionIdentifierStandard.Builder()
					.setName(fluent.getName())
					.build();
			Expression assignment = domain.getCpfsAssignment(name.getName());
//			assignment = replaceParameters(fluent, assignment);
			renAssg.put(fluent.getName(), assignment);
		}
		for (IntermediateFluent fluent : byLevel) {
			ExpressionIdentifierStandard name = new ExpressionIdentifierStandard.Builder()
					.setName(fluent.getName())
					.build();
			Expression assignment = renAssg.get(name.getName());
    		assignment = replaceDistributions(assignment);
			assignment = replaceIntermediateExpressions(assignment, map, params);
			map.put(name, assignment);
			List<Expression> parameters = new ArrayList<>(this.cpfsParameters.get(fluent).keySet());
			params.put(name, parameters);
		}
	}
	
	// TODO fix parameter replacement
	private Expression replaceIntermediateExpressions(Expression expression,
			Map<Expression, Expression> map,
			Map<Expression, List<Expression>> intermIdParams) {
    	if ((expression instanceof ExpressionRDDLQuantifiedIdentifier)) {
    		ExpressionRDDLQuantifiedIdentifier quantified = (ExpressionRDDLQuantifiedIdentifier) expression;
    		if (map.containsKey(quantified.getIdentifier())) {
    			Expression identifier = quantified.getIdentifier();
    			Expression assignment = map.get(identifier);
    			List<Expression> origParameters = intermIdParams.get(identifier);
    			List<Expression> instanceParameters = quantified.getParameters();
    			Map<Expression,Expression> paramMap = new HashMap<>();
    			for (int i = 0; i < origParameters.size(); i++) {
    				paramMap.put(origParameters.get(i), instanceParameters.get(i));
    			}
    			assignment = UtilExpressionStandard.replace(assignment, paramMap);
    			return assignment;
    		} else {
        		List<Expression> newChildren = new ArrayList<>();
        		for (Expression child : expression.getChildren()) {
        			newChildren.add(replaceIntermediateExpressions(child, map, intermIdParams));
        		}
        		return expression.replaceChildren(newChildren);
    		}
    	} else {
    		if (expression instanceof ExpressionIdentifierStandard
    				&& ((ExpressionIdentifierStandard) expression).getName()
    				.startsWith(DISTRID_VAR)) {
    			DistributionIdentifier dId = this.distrIdentifierMap.get(expression);
    			List<Expression> newProbs = new ArrayList<>();
    			for (Expression prob : dId.getProbabilities()) {
    				newProbs.add(replaceIntermediateExpressions(prob, map, intermIdParams));
    			}
    			dId.replaceProbabilities(newProbs);
    		}
    		List<Expression> newChildren = new ArrayList<>();
    		for (Expression child : expression.getChildren()) {
    			newChildren.add(replaceIntermediateExpressions(child, map, intermIdParams));
    		}
    		return expression.replaceChildren(newChildren);
    	}
	}

	private Expression replaceDistributions(Expression expression) {
    	assert expression != null;
    	if (expression instanceof ExpressionOperator) {
    		ExpressionOperator expressionOperator = (ExpressionOperator) expression;
    		Operator operator = expressionOperator.getOperator();
    		if (operator instanceof OperatorDistributionBernoulli) {
    			List<Expression> probabilities = new ArrayList<>();
    			TypeInteger typeInteger = TypeInteger.get(contextValue);
    			TypeBoolean typeBoolean = TypeBoolean.get(contextValue);
    			probabilities.add(expressionOperator.getOperand1());
    			probabilities.add(subtract(new ExpressionLiteral.Builder()
    					.setValue(UtilValue.newValue(typeInteger, 1))
    					.build(), expressionOperator.getOperand1()));
    			Expression identifier = newDistributionIdentifier(probabilities);
    			List<Expression> parameters = new ArrayList<>();
    			parameters.add(identifier);
    			parameters.add(new ExpressionLiteral.Builder()
    					.setValue(UtilValue.newValue(typeInteger, 0))
    					.build());
    			parameters.add(new ExpressionLiteral.Builder()
    					.setValue(typeBoolean.getTrue())
    					.build());
    			parameters.add(new ExpressionLiteral.Builder()
    					.setValue(UtilValue.newValue(typeInteger, 1))
    					.build());
    			parameters.add(new ExpressionLiteral.Builder()
    					.setValue(typeBoolean.getFalse())
    					.build());
    			return new ExpressionOperator.Builder()
    					.setOperator(contextValue.getOperator(OperatorSwitch.IDENTIFIER))
    					.setOperands(parameters)
    					.build();
    		} else if (operator instanceof OperatorDistributionDiscrete) {
    			List<Expression> probabilities = new ArrayList<>();
    			for (int i = 1; i < expressionOperator.getOperands().size(); i += 2) {
    				probabilities.add(expressionOperator.getOperands().get(i));
    			}
    			Expression identifier = newDistributionIdentifier(probabilities);
    			List<Expression> parameters = new ArrayList<>();
    			parameters.add(identifier);
    			TypeInteger typeInteger = TypeInteger.get(contextValue);
    			for (int i = 0; i < expressionOperator.getOperands().size() / 2; i++) {
    				parameters.add(new ExpressionLiteral.Builder()
    						.setValue(UtilValue.newValue(typeInteger, i * 2))
    						.build());
    				parameters.add(expressionOperator.getOperands().get(1 + i * 2));
    			}
    			return new ExpressionOperator.Builder()
    					.setOperator(contextValue.getOperator(OperatorSwitch.IDENTIFIER))
    					.setOperands(parameters)
    					.build();
    		} else if (operator instanceof OperatorDistributionDiracDelta) {
    			return expressionOperator.getOperand1();
    		} else if (operator instanceof OperatorDistributionKronDelta) {
    			return expressionOperator.getOperand1();
    		} else {
    			List<Expression> operands = expressionOperator.getChildren();
    			List<Expression> newOperands = new ArrayList<>();
    			for (Expression operand : operands) {
    				newOperands.add(replaceDistributions(operand));
    			}
    			return expressionOperator.replaceChildren(newOperands);
    		}
    	} else if (expression instanceof ExpressionIdentifierStandard) {
    		return expression;
    	} else if (expression instanceof ExpressionLiteral) {
    		return expression;
    	} else if (expression instanceof ExpressionRDDLQuantifiedIdentifier) {
    		return expression;
    	} else if (expression instanceof ExpressionRDDLQuantifier) {
    		ExpressionRDDLQuantifier quantifier = (ExpressionRDDLQuantifier) expression;
    		List<Expression> children = quantifier.getChildren();
			List<Expression> newOperands = new ArrayList<>();
			for (Expression operand : children) {
				newOperands.add(replaceDistributions(operand));
			}
			return expression.replaceChildren(newOperands);
    	} else {
    		assert false : expression;
    	}
    	
		return null;
	}
	
	private Expression newDistributionIdentifier(List<Expression> probabilities) {
		assert probabilities != null;
		String name = DISTRID_VAR + this.distributionIdentifiers.size();
		DistributionIdentifier distributionIdentifier = new DistributionIdentifier(name, probabilities);
		this.distributionIdentifiers.add(distributionIdentifier);
		Expression identifier = distributionIdentifier.getIdentifier();
		this.distrIdentifierMap.put(identifier, distributionIdentifier);
//		identifier.getContext().registerType(identifier, type);
		return identifier;
	}
	
	private int computeNumStateFluents() {
    	int totalNumStateFluents = 0;
        for (StateFluent stateFluent : this.stateFluents) {
        	int size = computeNumCopies(stateFluent);
        	totalNumStateFluents += size;
        }
        if (this.isNonDet) {
        	totalNumStateFluents++;
        }

        return totalNumStateFluents;
	}
	
	ModelOffsetComputer getOffsetComputer() {
		return offsetComputer;
	}
	
    private Expression subtract(Expression a, Expression b) {
    	return new ExpressionOperator.Builder()
    			.setOperator(contextValue.getOperator(OperatorSubtract.IDENTIFIER))
    			.setOperands(a, b)
    			.build();
    }
}
