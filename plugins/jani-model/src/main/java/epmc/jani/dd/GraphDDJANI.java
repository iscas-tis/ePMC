/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

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
import epmc.expression.Expression;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.LowLevel;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.SemanticsNonDet;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.graph.explorer.Explorer;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.OptionsJANIModel;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.Variable;
import epmc.modelchecker.Engine;
import epmc.modelchecker.Model;
import epmc.options.Options;
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
    public final static String IDENTIFIER = "jani-dd";
    
    public final static class Builder implements LowLevel.Builder {
        private Model model;
        private Engine engine;
        private final Set<Object> graphProperties = new LinkedHashSet<>();
        private final Set<Object> nodeProperties = new LinkedHashSet<>();
        private final Set<Object> edgeProperties = new LinkedHashSet<>();

        @Override
        public Builder setModel(Model model) {
            this.model = model;
            return this;
        }

        @Override
        public Builder setEngine(Engine engine) {
            this.engine = engine;
            return this;
        }

        @Override
        public Builder addGraphProperties(Set<Object> graphProperties) {
            this.graphProperties.addAll(graphProperties);
            return this;
        }

        @Override
        public Builder addNodeProperties(Set<Object> nodeProperties) {
            this.nodeProperties.addAll(nodeProperties);
            return this;
        }

        @Override
        public Builder addEdgeProperties(Set<Object> edgeProperties) {
            this.edgeProperties.addAll(edgeProperties);
            return this;
        }

        @Override
        public GraphDDJANI build() {
            if (!(engine instanceof Explorer)) {
                return null;
            }
            if (!(model instanceof ModelJANI)) {
                return null;
            }
            return new GraphDDJANI(this);
        }
    }
    
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

    private GraphDDJANI(Builder builder) {
        this((ModelJANI) builder.model, builder.nodeProperties, builder.edgeProperties);
    }

    /**
     * Construct a new symbolic graph from the given JANI model.
     * None of the parameters may be {@code null} or contain {@code null}
     * entries.
     * 
     * @param model model to construct symbolic representation of
     * @param nodeProperties node properties to be derived and stored
     * @param edgeProperties edge properties to be derived and stored
     */
    public GraphDDJANI(ModelJANI model,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        assert assertConstructor(nodeProperties, edgeProperties);

        this.model = model;
        this.contextDD = ContextDD.get();
        prepareActionDDVariable();
        buildGlobalVariables();
        expressionToDD = new ExpressionToDD(globalIdentifierToDD);
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
            ensure(Options.get().getBoolean(OptionsJANIModel.JANI_FIX_DEADLOCKS), ProblemsJANIDD.JANI_DD_DEADLOCK);
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

    private void buildProperties(DD weight) {
        properties.registerGraphProperty(CommonProperties.EXPRESSION_TO_DD,
                new TypeObject.Builder()
                .setClazz(expressionToDD.getClass())
                .build());
        properties.setGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD,
                expressionToDD);
        properties.registerGraphProperty(CommonProperties.SEMANTICS,
                new TypeObject.Builder()
                .setClazz(Semantics.class)
                .build());
        properties.setGraphPropertyObject(CommonProperties.SEMANTICS,
                model.getSemantics());

        TypeEnum playerType = TypeEnum.get(Player.class);
        Value playerOneStochastic = playerType.newValue(Player.ONE_STOCHASTIC);
        DD player;
        player = getContextDD().newConstant(playerOneStochastic);
        properties.registerNodeProperty(CommonProperties.PLAYER, player);
        DD trueDD = getContextDD().newConstant(true);
        properties.registerNodeProperty(CommonProperties.STATE, trueDD);        
        trueDD.dispose();

        properties.registerEdgeProperty(CommonProperties.WEIGHT, weight);
    }

    private void checkStateSpace(List<DDTransition> transitions) {
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
     */
    private DD buildPresCube(DDComponent component) {
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
     */
    private DD buildNextCube(DDComponent component) {
        assert component != null;
        DD nextCube = component.getNextCube().clone();
        for (VariableDD variable : globalVariableDDs) {
            nextCube = nextCube.andWith(variable.newCube(NEXT_STATE));
        }
        return nextCube;
    }

    private VariableDD buildActionVariable(List<DDTransition> transitions) {
        if (SemanticsNonDet.isNonDet(model.getSemantics())) {
            int required = Integer.SIZE - Integer.numberOfLeadingZeros(transitions.size() - 1);
            ensure(this.ddActionBits.get(0).size() >= required, ProblemsJANIDD.JANI_DD_ACTION_BITS_INSUFFICIENT, this.ddActionBits.get(0).size(), required);
            Type actionType = TypeInteger.get(0, transitions.size() - 1);
            return contextDD.newVariable(ACTION, actionType, ddActionBits);
        } else {
            return null;
        }
    }

    private DD buildActionCube(VariableDD actionVariable) {
        if (SemanticsNonDet.isNonDet(model.getSemantics())) {
            assert actionVariable != null;
            return actionVariable.newCube(PRES_STATE);
        } else {
            return contextDD.newConstant(true);
        }
    }

    private DD buildWeight(List<DDTransition> transitions, VariableDD actionVariable) {
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

    private DDTransition computeDeadlockTransition(DD deadlocks, Set<VariableDD> variables) {
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

    private DD buildDeadlocks(List<DDTransition> transitions) {
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
     */
    private DD explore(List<DDTransition> transitionsList) {	
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
     */
    private List<DDTransition> buildTransitions(DDComponent component) {
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
     */
    private void prepareActionDDVariable() {
        if (!SemanticsNonDet.isNonDet(model.getSemantics())) {
            return;
        }
        ddActionBits = new ArrayList<>();
        ddActionBits.add(new ArrayList<>());
        Options options = Options.get();
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
     */
    private void buildGlobalVariables() {
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
     */
    private DD buildInitialNodes(DDComponent component) {
        assert component != null;
        Expression initialStates = model.getInitialStatesExpressionOrTrue();
        Expression bounds = UtilModelParser.restrictToVariableRange(model.getGlobalVariablesOrEmpty());
        initialStates = UtilExpressionStandard.opAnd(initialStates, bounds);
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
    public DD getInitialNodes() {
        return initialNodes;
    }

    @Override
    public DD getTransitions() {
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
    public DD getNodeSpace() {
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
}
