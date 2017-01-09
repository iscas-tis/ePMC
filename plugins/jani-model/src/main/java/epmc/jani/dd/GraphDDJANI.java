package epmc.jani.dd;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.SemanticsNonDet;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.OptionsJANIModel;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.Variable;
import epmc.jani.value.ContextValueJANI;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeEnum;
import epmc.value.TypeInteger;
import epmc.value.TypeObject;
import epmc.value.Value;

// TODO check memory usage, in particular in case of errors
// TODO correct handling of graph/node/edge properties
//AT: there are no transient/observable assignment in the JANI specification
// TODO observables

/**
 * DD-based symbolic graph representation of a JANI model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class GraphDDJANI implements GraphDD {
	/** Encoding marker of present state variables. */
	private final static int PRES_STATE = 0;
	/** Encoding marker of next state variables. */
	private final static int NEXT_STATE = 1;
	/** String "action". */
	private final static String ACTION = "action";
	/** String containing an underscore. */
	private static final String UNDERSCORE = "_";

	/** Whether graph was already closed and cannot be used further. */
	private boolean closed;
	/** JANI model which this object represents. */
	private final ModelJANI model;
	/** DD context of this graph, derived from model. */
	private final ContextDD contextDD;
	/** DD variables used to build the variable for the action chosen. */
	private List<List<DD>> ddActionBits;
	/** List of global DD variables. */
	private final List<VariableDD> globalVariableDDs = new ArrayList<>();
	/** Map from global variables to DD variables. */
	private final Map<Variable,VariableDD> globalVariableToDD = new LinkedHashMap<>();
	/** Unmodifiable map from global variables to DD variables. */
	private final Map<Variable,VariableDD> globalVariablesToDDExternal = Collections.unmodifiableMap(globalVariableToDD);
	/** Map from global variable identifiers to DD variables. */
	private final Map<Expression,VariableDD> globalIdentifierToDD = new LinkedHashMap<>();
	/** Unmodifiable map from global variable identifiers to DD variables. */
	private final Map<Expression,VariableDD> globalIdentifierToDDExternal = Collections.unmodifiableMap(globalIdentifierToDD);
	/** Map from global variables to variable identifiers. */
	private final Map<Variable,Expression> globalVariableToIdentifier = new LinkedHashMap<>();
	/** Unmodifiable map from global variables to variable identifiers. */
	private final Map<Variable,Expression> globalVariableToIdentifierExternal = Collections.unmodifiableMap(globalVariableToIdentifier);
	/** Initial nodes of the graph. */
	private final DD initialNodes;
	/** Cube of present state variables. */
	private DD presCube;
	/** Cube of next state variables. */
	private DD nextCube;
	/** Cube of action variables (encoding choice of edges). */
	private final DD actionCube;
	/** Permutation to swap present and next state variables. */
	private final Permutation swapPresNext;
	/** Final transition relation - DD, not */
	private final DD transitions;
	/** Node space of this graph. */
	private final DD nodeSpace;
	/** Properties of this graph. */
	private final GraphDDProperties properties = new GraphDDProperties(this);
	/** Converter from expressions to their symbolic representations. */
	private ExpressionToDD expressionToDD;
	
	/**
	 * Construct a new symbolic graph from the given JANI model.
	 * None of the parameters may be {@code null} or contain {@code null}
	 * entries.
	 * 
	 * @param model model to construct symbolic representation of
	 * @param nodeProperties node properties to be derived and stored
	 * @param edgeProperties edge properties to be derived and stored
	 * @throws EPMCException thrown in case of problems during construction
	 */
	public GraphDDJANI(ModelJANI model,
			Set<Object> nodeProperties,
			Set<Object> edgeProperties) throws EPMCException {
		assert assertConstructor(nodeProperties, edgeProperties);
		
		this.model = model;
		this.contextDD = ContextDD.get(model.getContextValue());
		prepareActionDDVariable();
		buildGlobalVariables();
		expressionToDD = new ExpressionToDD(getContextValue(), globalIdentifierToDD);
		PreparatorDDComponent preparator = new PreparatorDDComponent();
		DDComponent component = preparator.prepare(this, model.getSystem());

		initialNodes = buildInitialNodes(component);
		presCube = buildPresCube(component);
		nextCube = buildNextCube(component);

		List<DDTransition> transitions = buildTransitions(component);
		swapPresNext = contextDD.newPermutationCube(presCube, nextCube);
		nodeSpace = explore(transitions);
        DD deadlocks = buildDeadlocks(transitions);
        DDTransition deadlockTransition = null;
        if (!deadlocks.isFalse()) {
        	ensure(model.getContextValue().getOptions().getBoolean(OptionsJANIModel.JANI_FIX_DEADLOCKS), ProblemsJANIDD.JANI_DD_DEADLOCK);
        	deadlockTransition = computeDeadlockTransition(deadlocks, component.getVariables());
        }
		deadlocks.dispose();
		checkStateSpace(transitions);
		if (deadlockTransition != null) {
			transitions.add(deadlockTransition);
		}
		VariableDD actionVariable = buildActionVariable(transitions);
		DD weight = buildWeight(transitions, actionVariable);
        
		actionCube = buildActionCube(actionVariable);
		this.transitions = weight.clone().gtWith(contextDD.newConstant(0));
		buildProperties(weight);
		weight.dispose();
//		component.close();
		// TODO continue here (observables, etc.)
	}

	private void buildProperties(DD weight) throws EPMCException {
        properties.registerGraphProperty(CommonProperties.EXPRESSION_TO_DD,
        		new TypeObject.Builder()
                .setContext(model.getContextValue())
                .setClazz(expressionToDD.getClass())
                .build());
        properties.setGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD,
                expressionToDD);
        properties.registerGraphProperty(CommonProperties.SEMANTICS,
        		new TypeObject.Builder()
                .setContext(model.getContextValue())
                .setClazz(Semantics.class)
                .build());
        properties.setGraphPropertyObject(CommonProperties.SEMANTICS,
                model.getSemantics());
        
        TypeEnum playerType = TypeEnum.get(model.getContextValue(), Player.class);
        Value playerOneStochastic = playerType.newValue(Player.ONE_STOCHASTIC);
        DD player;
        player = getContextDD().newConstant(playerOneStochastic);
        properties.registerNodeProperty(CommonProperties.PLAYER, player);
		DD trueDD = getContextDD().newConstant(true);
		properties.registerNodeProperty(CommonProperties.STATE, trueDD);        
		trueDD.dispose();
		
		properties.registerEdgeProperty(CommonProperties.WEIGHT, weight);
	}

	private void checkStateSpace(List<DDTransition> transitions) throws EPMCException {
		assert transitions != null;
		for (DDTransition transition : transitions) {
			assert transition != null;
		}
		for (DDTransition transition : transitions) {			
			if (transition.isInvalid()) {
				ensure(transition.getGuard().and(nodeSpace).isFalseWith(), ProblemsJANIDD.JANI_DD_GLOBAL_MULTIPLE);
			}
			for (VariableValid valid : transition.getValidFor()) {
				ensure(nodeSpace.andNot(valid.getValid()).isFalseWith(), ProblemsJANIDD.JANI_DD_TRANSITION_INVALID_ASSIGNMENT);
			}
		}
	}

	/**
	 * Build cube of present state variables.
	 * The cube build is derived from taking the present state cube from the
	 * top-level system component and adding the cubes from global variables.
	 * The component parameter must not be {@code null}.
	 * 
	 * @param component top-level system component
	 * @return cube of present state variables
	 * @throws EPMCException thrown in case of problems
	 */
	private DD buildPresCube(DDComponent component) throws EPMCException {
		assert component != null;
		DD presCube = component.getPresCube().clone();
		for (VariableDD variable : globalVariableDDs) {
			presCube = presCube.andWith(variable.newCube(PRES_STATE));
		}
		return presCube;
	}

	/**
	 * Build cube of next state variables.
	 * The cube build is derived from taking the next state cube from the
	 * top-level system component and adding the cubes from global variables.
	 * The component parameter must not be {@code null}.
	 * 
	 * @param component top-level system component
	 * @return cube of next state variables
	 * @throws EPMCException thrown in case of problems
	 */
	private DD buildNextCube(DDComponent component) throws EPMCException {
		assert component != null;
		DD nextCube = component.getNextCube().clone();
		for (VariableDD variable : globalVariableDDs) {
			nextCube = nextCube.andWith(variable.newCube(NEXT_STATE));
		}
		return nextCube;
	}

	private VariableDD buildActionVariable(List<DDTransition> transitions) throws EPMCException {
		if (SemanticsNonDet.isNonDet(model.getSemantics())) {
			int required = Integer.SIZE - Integer.numberOfLeadingZeros(transitions.size() - 1);
			ensure(this.ddActionBits.get(0).size() >= required, ProblemsJANIDD.JANI_DD_ACTION_BITS_INSUFFICIENT, this.ddActionBits.get(0).size(), required);
			Type actionType = TypeInteger.get(getContextValue(), 0, transitions.size() - 1);
			return contextDD.newVariable(ACTION, actionType, ddActionBits);
		} else {
			return null;
		}
	}

	private DD buildActionCube(VariableDD actionVariable) throws EPMCException {
		if (SemanticsNonDet.isNonDet(model.getSemantics())) {
			assert actionVariable != null;
			return actionVariable.newCube(PRES_STATE);
		} else {
			return contextDD.newConstant(true);
		}
	}

	private DD buildWeight(List<DDTransition> transitions, VariableDD actionVariable) throws EPMCException {
		assert transitions != null;
		for (DDTransition transition : transitions) {
			assert transition != null;
		}
		Semantics semantics = model.getSemantics();
		int transitionNr = 0;
		DD weight = contextDD.newConstant(0);
		for (DDTransition transition : transitions) {
			DD transitionEnc = transition.getTransitions().clone();
			if (SemanticsNonDet.isNonDet(semantics)) {
				DD actEnc = actionVariable.newIntValue(PRES_STATE, transitionNr);
				actEnc = actEnc.toMTWith();
				transitionEnc = transitionEnc.multiplyWith(actEnc);
			}
			weight = weight.addWith(transitionEnc);
			transitionNr++;
		}
		if (!SemanticsNonDet.isNonDet(semantics) && SemanticsDiscreteTime.isDiscreteTime(semantics)) {
			DD sum = weight.abstractSum(nextCube);
			weight = weight.divideIgnoreZeroWith(sum);
		}
		return weight;
	}

	private DDTransition computeDeadlockTransition(DD deadlocks, Set<VariableDD> variables) throws EPMCException {
		DDTransition result = new DDTransition();
		result.setAction(model.getSilentAction());
		result.setGuard(deadlocks.clone());
		result.setInvalid(false);
		Set<VariableDD> allVariables = new LinkedHashSet<>();
		allVariables.addAll(globalVariableDDs);
		allVariables.addAll(variables);
		result.setWrites(allVariables);
		DD transitionDD = getContextDD().newConstant(true);
		for (VariableDD variable : allVariables) {
			DD assignmentDD = variable.newEqCopies(PRES_STATE, NEXT_STATE);
			transitionDD = transitionDD.andWith(assignmentDD);
		}
		transitionDD = transitionDD.andWith(deadlocks.clone()).toMTWith();
		result.setTransitions(transitionDD);
		return result;
	}

	private DD buildDeadlocks(List<DDTransition> transitions) throws EPMCException {
		DD guards = contextDD.newConstant(false);
		for (DDTransition transition : transitions) {
			guards = guards.orWith(transition.getGuard().clone());
		}
		DD deadlocks = nodeSpace.andNot(guards);
		guards.dispose();
        return deadlocks;
	}

	private boolean assertConstructor(Set<Object> nodeProperties,
			Set<Object> edgeProperties) {
		assert nodeProperties != null;
		for (Object property : nodeProperties) {
			assert property != null;
		}
		assert edgeProperties != null;
		for (Object property : edgeProperties) {
			assert property != null;
		}
		return true;
	}

	/**
	 * Explore node space of the model.
	 * This function is intended to be used internally to compute the reachable
	 * set of nodes, that is, without construction of the graph.
	 * The transition relation DD used for the exploration is not the same as
	 * the one obtained by {@link #getTransitions()}. The reason is that
	 * <ul>
	 * <li>for this transitions, we do not need the distinction between
	 * different edges etc. of automata, and thus we do not need to use the
	 * action variable,</li>
	 * <li>we do not need to fix deadlocks, and it is anyway not yet known on
	 * which states we will have deadlocks.</li>
	 * </ul>
	 * The list of transitions may not be {@code null} and may not contain
	 * {@code null} entries.
	 * 
	 * @param transitionsList list of transitions of the top-level component
	 * @return node space of the model
	 * @throws EPMCException thrown in case of problems
	 */
	private DD explore(List<DDTransition> transitionsList) throws EPMCException {	
		assert transitionsList != null;
		for (DDTransition transition : transitionsList) {
			assert transition != null;
		}
		DD transitions = contextDD.newConstant(false);
		DD zeroDD = contextDD.newConstant(0);
		for (DDTransition transition : transitionsList) {
			transitions = transitions.orWith(transition.getTransitions().gt(zeroDD));
		}
		zeroDD.dispose();
		
        DD states = initialNodes.clone();
        DD predecessors = contextDD.newConstant(false);
        while (!states.equals(predecessors)) {
            predecessors.dispose();
            predecessors = states;
            DD next = transitions.abstractAndExist(states, presCube);
            next = next.permuteWith(swapPresNext);
            states = states.clone().orWith(next);
        }
        predecessors.dispose();
        return states;
	}

	/**
	 * Build transitions from DD component.
	 * The function works by transforming the {@link DDTransition}s of the
	 * top-level DD component.
	 * The {@link DDTransition}s of {@link DDComponent#getTransitions()} only
	 * assign the variable values of {@link DDComponent#getVariables()}, in
	 * order to allow synchronisation with other DD components. After the
	 * composition of the whole system is done however, we must ensure for each
	 * resulting {@link DDTransition} that variables which are not written by
	 * the transition are fixed to their present value. Otherwise, they may
	 * take values on such a transition, such that the transition relation is
	 * not useable in the end.
	 * The component parameter must not be {@code null}.
	 * 
	 * @param component top-level component of the model
	 * @return transitions modified to build the transition relation
	 * @throws EPMCException thrown in case of problems
	 */
	private List<DDTransition> buildTransitions(DDComponent component) throws EPMCException {
		assert component != null;
		List<DDTransition> transitions = component.getTransitions();
		for (DDTransition transition : transitions) {
			assert transition != null;
		}
		List<DDTransition> result = new ArrayList<>();
		Set<VariableDD> allVariables = new LinkedHashSet<>();
		allVariables.addAll(component.getVariables());
		allVariables.addAll(globalVariableDDs);
		for (DDTransition componentTransition : transitions) {
			DD transitionDD = componentTransition.getTransitions().clone();
			for (VariableDD variable : allVariables) {
				if (componentTransition.getWrites().contains(variable)) {
					continue;
				}
				DD assignmentDD = variable.newEqCopies(PRES_STATE, NEXT_STATE);
				transitionDD = transitionDD.multiplyWith(assignmentDD.toMTWith());
			}
			DDTransition transition = new DDTransition();
			transition.setAction(componentTransition.getAction());
			transition.setGuard(componentTransition.getGuard().clone());
			transition.setInvalid(componentTransition.isInvalid());
			transition.setWrites(component.getVariables());
			transition.setTransitions(transitionDD);
			Set<VariableValid> valid = new LinkedHashSet<>();
			for (VariableValid varValid : componentTransition.getValidFor()) {
				valid.add(varValid.clone());
			}
			transition.setVariableValid(valid);
			result.add(transition);
		}
		return result;
	}

	/**
	 * Prepares the DD variables to encode the transitions chosen.
	 * For efficiency, this function should be called before preparing the model
	 * components. This ensures that in the DD context the DD variables of the
	 * action variable are placed before the global and model variables. Doing
	 * so is crucial, as otherwise the DD representation will be quite large.
	 * Note that it is not possible to generate a {@link VariableDD} for the
	 * transition choice before having build the model components, because the
	 * number of choices is not known at this point. Thus, we only reserve a
	 * hopefully sufficiently large number of DD variables and generate the
	 * action variable after the system components have been prepared.
	 * Because for models without nondeterminism action variables are not
	 * needed, they will not be constructed for such models.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	private void prepareActionDDVariable() throws EPMCException {
		if (!SemanticsNonDet.isNonDet(model.getSemantics())) {
			return;
		}
		ddActionBits = new ArrayList<>();
		ddActionBits.add(new ArrayList<>());
		Options options = model.getContextValue().getOptions();
		int numBits = options.getInteger(OptionsJANIModel.JANI_ACTION_BITS);
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
        	int varNr = contextDD.numVariables();
        	DD dd = contextDD.newVariable();
        	String ddName = ACTION + UNDERSCORE + bitNr;
        	contextDD.setVariableName(varNr, ddName);
        	ddActionBits.get(0).add(dd);
        }
	}

	/**
	 * Build the set of global variables.
	 * 
	 * @throws EPMCException thrown in case of problems
	 */
	private void buildGlobalVariables() throws EPMCException {
		for (Variable variable : model.getGlobalVariablesOrEmpty()) {
			VariableDD variableDD = contextDD.newVariable(variable.getName(), variable.toType(), 2);
			globalVariableDDs.add(variableDD);
			globalVariableToDD.put(variable, variableDD);
			globalIdentifierToDD.put(variable.getIdentifier(), variableDD);
			globalVariableToIdentifier.put(variable, variable.getIdentifier());
		}
	}

	/**
	 * Computes the initial nodes of this symbolic graph.
	 * For this, we use the assignments of local variables of the topmost system
	 * component, and combine it with the initial values of global variables.
	 * The component parameter must not be {@code null}.
	 * 
	 * @param component topmost DD system component
	 * @return initial nodes of this symbolic graph
	 * @throws EPMCException thrown in case of problems
	 */
	private DD buildInitialNodes(DDComponent component) throws EPMCException {
		assert component != null;
		Expression initialStates = model.getInitialStatesExpressionOrTrue();
		Expression bounds = UtilModelParser.restrictToVariableRange(getContextValue(), model.getGlobalVariablesOrEmpty());
		initialStates = UtilExpressionStandard.opAnd(getContextValue(), initialStates, bounds);
		initialStates = model.replaceConstants(initialStates);
		DD initialNodes = getContextDD().newConstant(true);
		initialNodes = initialNodes.andWith(component.getInitialNodes().clone());
		initialNodes = initialNodes.andWith(expressionToDD.translate(initialStates));

		return initialNodes;
	}


	@Override
	public ContextDD getContextDD() {
		return contextDD;
	}

	@Override
	public DD getInitialNodes() throws EPMCException {
		return initialNodes;
	}

	@Override
	public DD getTransitions() throws EPMCException {
		return transitions;
	}

	@Override
	public DD getPresCube() {
		return presCube;
	}

	@Override
	public DD getNextCube() {
		return nextCube;
	}

	@Override
	public DD getActionCube() {
		return actionCube;
	}

	@Override
	public Permutation getSwapPresNext() {
		return swapPresNext;
	}

	@Override
	public DD getNodeSpace() throws EPMCException {
		return nodeSpace;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		for (VariableDD variableDD : globalVariableDDs) {
			variableDD.close();
		}
		initialNodes.dispose();
		if (SemanticsNonDet.isNonDet(model.getSemantics())) {
			for (int copy = 0; copy < 1; copy++) {
				for (DD dd : ddActionBits.get(copy)) {
					dd.dispose();
				}
			}
		}
	}

	@Override
	public GraphDDProperties getProperties() {
		return properties;
	}

	/**
	 * Obtain map from global variables to DD variables.
	 * The result of this function is an unmodifiable map.
	 * This function is intended to be used by {@link DDComponent} objects to
	 * obtain the necessary information about global variables to build their
	 * symbolic transition relations, etc.
	 * 
	 * @return unmodifiable map from global variables to DD variables
	 */
	Map<Variable,VariableDD> getGlobalVariablesToDD() {
		return globalVariablesToDDExternal;
	}

	/**
	 * Obtain map from global variables identifiers to DD variables.
	 * The result of this function is an unmodifiable map.
	 * This function is intended to be used by {@link DDComponent} objects to
	 * obtain the necessary information about global variables to build their
	 * symbolic transition relations, etc.
	 * 
	 * @return unmodifiable map from global variable identifiers to DD variables
	 */
	Map<Expression, VariableDD> getGlobalIdentifiersToDD() {
		return globalIdentifierToDDExternal;
	}
	
	/**
	 * Obtain map from global variables to variable identifiers.
	 * The result of this function is an unmodifiable map.
	 * This function is intended to be used by {@link DDComponent} objects to
	 * obtain the necessary information about global variables to build their
	 * symbolic transition relations, etc.
	 * 
	 * @return unmodifiable map from global variables to variable identifiers
	 */
	Map<Variable, Expression> getGlobalVariableToIdentifier() {
		return globalVariableToIdentifierExternal;
	}
	
	@Override
	public ContextValue getContextValue() {
		return model.getContextValue();
	}
	
	ContextValueJANI getContextValueJANI() {
		return model.getContextValueJANI();
	}
}
