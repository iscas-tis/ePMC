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

package epmc.propertysolver.ltllazy;

import static epmc.error.UtilError.ensure;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import epmc.algorithms.dd.ComponentsDD;
import epmc.automaton.AutomatonDD;
import epmc.automaton.AutomatonRabin;
import epmc.automaton.AutomatonRabinLabel;
import epmc.automaton.AutomatonStateBuechiSubset;
import epmc.automaton.Buechi;
import epmc.automaton.ProductGraphDD;
import epmc.automaton.ProductGraphDDDD;
import epmc.automaton.ProductGraphDDExplicit;
import epmc.automaton.UtilAutomaton;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.graphsolver.GraphSolverConfigurationDD;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveDDUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.options.Options;
import epmc.propertysolver.ltllazy.automata.AutomatonDDBreakpoint;
import epmc.propertysolver.ltllazy.automata.AutomatonDDSubset;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.ValueBoolean;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class PropertySolverDDLTLLazy implements PropertySolver {
    private enum ComponentDecision {
        ACCEPT, REJECT, UNDECIDED
    }

    private enum DecisionMethod {
        SUBSET, BREAKPOINT, RABIN
    }

    public final static String IDENTIFIER = "ltl-dd";
    private ModelChecker modelChecker;
    private Log log;
    private GraphDD modelGraph;
    private ExpressionToDD expressionToDD;
    private boolean nonDet;
    private boolean negate;
    private Options options;
    private Semantics type;
    private boolean skipTransient;
    private Expression property;
    private ExpressionQuantifier propertyQuantifier;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        this.options = Options.get();
        this.log = options.get(OptionsMessages.LOG);
    }

    private StateMap solve(Expression property, StateSet forStates, boolean min)
    {
        this.negate = min;
        ValueBoolean negate = TypeBoolean.get()
                .newValue(this.negate);
        Buechi buechi = UtilAutomaton.newBuechi
                (property, null, SemanticsNonDet.isNonDet(type), negate);
        DD result = checkProperty(buechi, (StateSetDD) forStates, negate.getBoolean());
        StateSetDD forStatesDD = (StateSetDD) forStates;
        return new StateMapDD(forStatesDD.clone(), result);
    }

    private DD checkTemporalLTLNonIncremental(Buechi buechi, StateSetDD forStates)
    {
        DD result = null;
        if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_SUBSET)) {
            result = computeNonIncremental(DecisionMethod.SUBSET, buechi, forStates);
        }
        if (result == null && options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_BREAKPOINT)) {
            result = computeNonIncremental(DecisionMethod.BREAKPOINT, buechi, forStates);
        }
        if (result == null && options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_RABIN)) {
            result = computeNonIncremental(DecisionMethod.RABIN, buechi, forStates);
        }
        ensure(result != null, ProblemsLTLLazy.LTL_LAZY_COULDNT_DECIDE);
        return result;
    }

    private DD checkProperty(Buechi buechi, StateSetDD forStates, boolean negate)
    {
        DD innerResult;
        if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_INCREMENTAL)) {
            innerResult = checkTemporalLTLIncremental(buechi, forStates);
        } else {
            innerResult = checkTemporalLTLNonIncremental(buechi, forStates);
        }
        if (innerResult == null) {
            return null;
        }
        if (negate) {
            innerResult = getContextDD().newConstant(1).subtractWith(innerResult);
        }

        return innerResult;
    }

    private DD checkTemporalLTLIncremental(Buechi buechi, StateSetDD forStates) {
        log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL);
        DD states = modelGraph.getNodeProperty(CommonProperties.STATE);
        AutomatonDDSubset automaton = new AutomatonDDSubset(expressionToDD, buechi, states);
        ProductGraphDDDD product = new ProductGraphDDDD(modelGraph, forStates.getStatesDD(), automaton);
        log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_DONE);
        log.send(MessagesLTLLazy.LTL_LAZY_EXPLORING_STATE_SPACE);
        DD nodeSpace = product.getNodeSpace();
        DD prodStates = product.getNodeProperty(CommonProperties.STATE);
        BigInteger modelStates = nodeSpace.and(prodStates).countSatWith(product.getPresCube().clone());
        BigInteger automatonStates = nodeSpace.abstractExist(modelGraph.getPresCube()).countSatWith(automaton.getPresCube().clone());
        log.send(MessagesLTLLazy.LTL_LAZY_EXPLORING_STATE_SPACE_DONE, modelStates, automatonStates);
        log.send(MessagesLTLLazy.LTL_LAZY_COMPUTING_END_COMPONENTS_INCREMENTALLY);
        ComponentsDD sccs = new ComponentsDD(product, nodeSpace, skipTransient);
        log.send(MessagesLTLLazy.LTL_LAZY_COMPUTING_END_COMPONENTS_INCREMENTALLY_DONE);
        DD acc = decideComponents(product, sccs, nodeSpace);
        sccs.close();

        DD reachProbs = computeReachProbs(product, acc, nodeSpace);
        nodeSpace.dispose();
        acc.dispose();
        reachProbs = reachProbs.multiplyWith(product.getInitialNodes().toMT());
        reachProbs = reachProbs.abstractSumWith(automaton.getPresCube().clone());
        product.close();
        automaton.close();

        return reachProbs;
    }

    private DD decideComponents(ProductGraphDDDD product, ComponentsDD components, DD nodeSpace)
    {
        AutomatonDDSubset subset = (AutomatonDDSubset) product.getAutomaton();
        Buechi buechi = subset.getBuechi();
        DD acceptingNodes = getContextDD().newConstant(false);
        int numComponents = 0;
        DD undecided = getContextDD().newConstant(false);
        DD init = product.getInitialNodes();
        for (DD component = components.next(); component != null;
                component = components.next()) {
            if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_STOP_IF_INIT_DECIDED)
                    && init.andNot(acceptingNodes).isFalseWith()) {
                component.dispose();
                break;
            }
            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_COMPONENT, numComponents);
            if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_REMOVE_DECIDED)) {
                if (component.andNot(acceptingNodes).isFalseWith()) {
                    log.send(MessagesLTLLazy.LTL_LAZY_SKIPPING_COMPONENT, numComponents);
                    numComponents++;
                    component.dispose();
                    continue;
                }
            }
            ComponentDecision decision = ComponentDecision.UNDECIDED;
            if (decision == ComponentDecision.UNDECIDED
                    && options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_SUBSET)) {
                decision = decideComponentSubset(subset, component);
            }
            boolean bscc = isBSCC(component, product);
            if (decision == ComponentDecision.UNDECIDED
                    && options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_BREAKPOINT)) {
                decision = decideComponentBreakpoint(buechi, product, component, bscc);
            }
            if (decision == ComponentDecision.UNDECIDED
                    && options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_RABIN)) {
                decision = decideComponentRabin(buechi, product, component, bscc);
            }
            if (decision == ComponentDecision.UNDECIDED
                    && options.getBoolean(OptionsLTLLazy.LTL_LAZY_USE_BREAKPOINT_SINGLETONS)) {
                decision = decideComponentBreakpointSingletons(product, buechi, product, component, bscc);
            }
            if (decision == ComponentDecision.ACCEPT) {
                if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_REMOVE_DECIDED)) {
                    DD oldComponent = component;
                    component = ComponentsDD.reachMaxOne(product, component, nodeSpace);
                    oldComponent.dispose();
                }
                acceptingNodes = acceptingNodes.orWith(component.clone());
            }
            if (decision == ComponentDecision.UNDECIDED) {
                undecided = undecided.orWith(component.clone());
            } else {
                undecided = undecided.andNotWith(component.clone());
            }
            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_COMPONENT_DONE, numComponents);
            numComponents++;
            component.dispose();
        }
        ensure(undecided.isFalse(), ProblemsLTLLazy.LTL_LAZY_COULDNT_DECIDE);
        undecided.dispose();
        return acceptingNodes;
    }

    private boolean isBSCC(DD component, ProductGraphDDDD product)
    {
        if (!nonDet) {
            return true;
        }
        DD next = post(product, component, product.getTransitions());
        return next.andNotWith(component.clone()).isFalseWith();
    }

    private DD post(GraphDD graph, DD nodes, DD trans) {
        Permutation nextToPres = graph.getSwapPresNext();
        DD presAndActions = graph.getPresCube().and(graph.getActionCube());
        DD result = trans.abstractAndExist(nodes, presAndActions);
        presAndActions.dispose();
        result = result.permuteWith(nextToPres);
        return result;
    }

    private ComponentDecision decideComponentSubset(AutomatonDDSubset automaton, DD component)
    {
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_SUBSET);
        boolean under = true;
        DD underRestricted = component.and(automaton.getUnder());
        for (DD label : automaton.getLabelVars()) {
            DD check = underRestricted.and(label);
            if (check.isFalseWith()) {
                under = false;
                break;
            }
        }
        underRestricted.dispose();
        if (under) {
            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_SUBSET_DONE_ACCEPT);
            return ComponentDecision.ACCEPT;
        }
        boolean over = true;
        DD overRestricted = component.and(automaton.getOver());
        for (DD label : automaton.getLabelVars()) {
            DD check = overRestricted.and(label);
            if (check.isFalseWith()) {
                over = false;
                break;
            }
        }
        overRestricted.dispose();
        if (!over) {
            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_SUBSET_DONE_REJECT);
            return ComponentDecision.REJECT;
        }
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_SUBSET_DONE_UNDECIDED);
        return ComponentDecision.UNDECIDED;
    }

    private final static class StatePair {
        DD modelState;
        DD automatonState;
    }

    private ComponentDecision decideComponentBreakpoint(Buechi buechi,
            ProductGraphDDDD subsetProduct, DD component, boolean bscc)
    {
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT);
        AutomatonDDSubset subset = (AutomatonDDSubset) subsetProduct.getAutomaton();
        StatePair state = chooseState(component, subset);
        DD modelState = state.modelState;
        DD automatonState = state.automatonState;
        DD states = modelGraph.getNodeProperty(CommonProperties.STATE);
        AutomatonDDBreakpoint automaton = new AutomatonDDBreakpoint
                (expressionToDD, buechi, states, automatonState, subset.getStateVariables());
        ProductGraphDDDD product = new ProductGraphDDDD(modelGraph, modelState, automaton);
        DD nodeSpace = product.getNodeSpace();
        DD prodStates = product.getNodeProperty(CommonProperties.STATE);
        BigInteger numStates = nodeSpace.and(prodStates).countSat(product.getPresCube());
        BigInteger numAut = nodeSpace.abstractExist(modelGraph.getPresCube()).countSatWith(automaton.getPresCube().clone());
        log.send(MessagesLTLLazy.LTL_LAZY_EXPLORING_STATE_SPACE_DONE, numStates, numAut);
        ComponentsDD components = new ComponentsDD(product, nodeSpace, skipTransient);
        boolean undecidedSeen = false;
        for (DD leafSCC = components.next(); leafSCC != null; leafSCC = components.next()) {
            ComponentDecision decision;
            if (!nonDet) {
                decision = decideComponentBreakpointMCLeaf(automaton, leafSCC);
            } else {
                decision = decideComponentBreakpointMDPLeaf(automaton, product,
                        leafSCC);
            }
            if (decision == ComponentDecision.ACCEPT) {
                if (bscc) {
                    log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_DONE_ACCEPT);
                    automaton.close();
                    product.close();
                    components.close();
                    return ComponentDecision.ACCEPT;
                } else {
                    if (checkReachOne(component, subsetProduct, leafSCC, product, nodeSpace)) {
                        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_DONE_ACCEPT);
                        automaton.close();
                        product.close();
                        components.close();
                        return ComponentDecision.ACCEPT;                        
                    } else {
                        undecidedSeen = true;
                    }
                }
            } else if (decision == ComponentDecision.UNDECIDED) {
                undecidedSeen = true;
            }
        }
        components.close();

        if (undecidedSeen) {
            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_DONE_UNDECIDED);
            automaton.close();
            product.close();
            return ComponentDecision.UNDECIDED;
        } else {
            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_DONE_REJECT);
            automaton.close();
            product.close();
            return ComponentDecision.REJECT;
        }
    }

    private boolean checkReachOne(DD component, ProductGraphDDDD subsetProduct,
            DD leafSCC, GraphDD product, DD productSpace)
    {
        // states reaching this leaf component with probability 1
        DD reaching = ComponentsDD.reachMaxOne(product, leafSCC, productSpace);
        // variables only of Rabin and not of other
        DD removeVars = reaching.supportDD().abstractExistWith(leafSCC.supportDD());
        // attach subset variables if necessary
        if (product instanceof ProductGraphDDExplicit) {
            AutomatonDDSubset automatonSubset = (AutomatonDDSubset) subsetProduct.getAutomaton();
            List<VariableDD> variables = automatonSubset.getStateVariables();
            reaching = reaching.andWith(rabinSubsetToDD((ProductGraphDDExplicit) product, variables));
        }
        // abstracting to state set in subset automaton
        reaching = reaching.abstractExistWith(removeVars);
        // checking if component contains any states reach with prob 1
        return !component.clone().andWith(reaching).isFalseWith();
    }

    private ComponentDecision decideComponentBreakpointMCLeaf(
            AutomatonDDBreakpoint automaton, DD leafSCC) {
        if (!leafSCC.and(automaton.getAccepting()).isFalse()) {
            return ComponentDecision.ACCEPT;
        } else if (leafSCC.and(automaton.getRejecting()).isFalse()) {
            return ComponentDecision.UNDECIDED;
        } else {
            return ComponentDecision.REJECT;
        }
    }

    private ComponentDecision decideComponentBreakpointMDPLeaf(
            AutomatonDDBreakpoint automaton, ProductGraphDD product, DD leafSCC)
    {
        if (!leafSCC.and(automaton.getAccepting()).isFalseWith()) {
            return ComponentDecision.ACCEPT;
        }
        DD nonRejecting = leafSCC.andNot(automaton.getRejecting());
        DD notNdNodes = product.getNodeProperty(CommonProperties.PLAYER).clone()
                .eqWith(getContextDD().newConstant(Player.STOCHASTIC));
        nonRejecting = stayIn(product, nonRejecting, notNdNodes);
        notNdNodes.dispose();
        if (!nonRejecting.isFalseWith()) {
            return ComponentDecision.UNDECIDED;
        } else {
            return ComponentDecision.REJECT;
        }
    }

    private ComponentDecision decideComponentRabin(Buechi buechi,
            ProductGraphDDDD subsetProduct, DD component, boolean bscc)
    {
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_RABIN);
        AutomatonDDSubset subset = (AutomatonDDSubset) subsetProduct.getAutomaton();
        StatePair state = chooseState(component, subset);
        DD modelState = state.modelState;
        DD automatonStateDD = state.automatonState;
        BitSet automatonState = ddToSubsetState(automatonStateDD, subset);
        automatonStateDD.dispose();
        AutomatonRabin automaton = UtilAutomaton.newAutomatonRabinSafra(buechi, automatonState);

        ProductGraphDDExplicit product = new ProductGraphDDExplicit(modelGraph, modelState, automaton, expressionToDD);
        modelState.dispose();
        DD nodeSpace = product.getNodeSpace();
        DD productStates = product.getNodeProperty(CommonProperties.STATE);
        BigInteger numStates = nodeSpace.and(productStates).countSatWith(product.getPresCube().clone());
        BigInteger numAut = nodeSpace.abstractExist(modelGraph.getPresCube()).countSatWith(product.getAutomatonPresCube().clone());
        log.send(MessagesLTLLazy.LTL_LAZY_EXPLORING_STATE_SPACE_DONE, numStates, numAut);
        List<DD> stable = new ArrayList<>();
        List<DD> accepting = new ArrayList<>();
        DD nonStates = nodeSpace.clone().andWith(productStates.not());
        computeSymbStabAcc(automaton, product.getLabeling(), nonStates, automaton.getNumPairs(), stable, accepting);
        nonStates.dispose();
        ComponentsDD components = new ComponentsDD(product, nodeSpace, skipTransient);
        for (DD leafSCC = components.next(); leafSCC != null; leafSCC = components.next()) {
            ComponentDecision decision;
            if (!nonDet) {
                decision = decideComponentRabinMCLeaf(leafSCC, stable, accepting);
            } else {
                decision = decideComponentRabinMDPLeaf(product, leafSCC,
                        stable, accepting);
            }
            if (decision == ComponentDecision.ACCEPT) {
                if (bscc) {
                    log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_RABIN_DONE_ACCEPT);
                    product.close();
                    leafSCC.dispose();
                    nodeSpace.dispose();
                    components.close();
                    getContextDD().dispose(stable);
                    getContextDD().dispose(accepting);
                    return ComponentDecision.ACCEPT;
                } else {
                    if (checkReachOne(component, subsetProduct, leafSCC, product, nodeSpace)) {
                        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_RABIN_DONE_ACCEPT);
                        product.close();
                        nodeSpace.dispose();
                        leafSCC.dispose();
                        components.close();
                        getContextDD().dispose(stable);
                        getContextDD().dispose(accepting);
                        return ComponentDecision.ACCEPT;
                    }
                }
            }
            leafSCC.dispose();
        }
        product.close();
        nodeSpace.dispose();
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_RABIN_DONE_REJECT);
        components.close();
        getContextDD().dispose(stable);
        getContextDD().dispose(accepting);
        return ComponentDecision.REJECT;
    }

    private ComponentDecision decideComponentBreakpointSingletons(GraphDD graph,
            Buechi buechi,
            ProductGraphDDDD subsetProduct, DD component, boolean bscc)
    {
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS);
        DD toCheck = component.clone();
        AutomatonDDSubset automatonDDSubset = (AutomatonDDSubset) subsetProduct.getAutomaton();

        DD statesDD = graph.getNodeProperty(CommonProperties.STATE);
        while (!toCheck.and(statesDD).isFalseWith()) {
            AutomatonDDSubset subset = (AutomatonDDSubset) subsetProduct.getAutomaton();
            StatePair stateDD = chooseState(toCheck, subset);
            DD modelState = stateDD.modelState;
            DD automatonStateDD = stateDD.automatonState;
            BitSet states =  ddToSubsetState(automatonStateDD, subset);
            //          for (int state = 0; state < buechi.getNumStates(); state++) {
            for (int state = buechi.getNumStates() - 1; state >= 0; state--) {
                if (!states.get(state)) {
                    continue;
                }
                BitSet singletonBS = UtilBitSet.newBitSetUnbounded();
                singletonBS.set(state);
                DD singletonDD = bitSetToDD(singletonBS, automatonDDSubset.getStateVariables());
                DD modelStates = modelGraph.getNodeProperty(CommonProperties.STATE);
                AutomatonDDBreakpoint automaton = new AutomatonDDBreakpoint
                        (expressionToDD, buechi, modelStates, singletonDD, subset.getStateVariables());
                ProductGraphDDDD product = new ProductGraphDDDD(modelGraph, modelState, automaton);
                DD nodeSpace = product.getNodeSpace();
                DD productStates = product.getNodeProperty(CommonProperties.STATE);
                BigInteger numStates = nodeSpace.and(productStates).countSatWith(product.getPresCube().clone());
                BigInteger numAut = nodeSpace.abstractExist(modelGraph.getPresCube()).countSatWith(automaton.getPresCube().clone());
                log.send(MessagesLTLLazy.LTL_LAZY_EXPLORING_STATE_SPACE_DONE, numStates, numAut);
                ComponentsDD components = new ComponentsDD(product, nodeSpace, skipTransient);
                for (DD leafSCC = components.next(); leafSCC != null; leafSCC = components.next()) {
                    ComponentDecision decision;
                    if (!nonDet) {
                        decision = decideComponentBreakpointMCLeaf(automaton, leafSCC);
                    } else {
                        decision = decideComponentBreakpointMDPLeaf(automaton, product,
                                leafSCC);
                    }
                    if (decision == ComponentDecision.ACCEPT) {
                        if (bscc) {
                            log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS_DONE_ACCEPT);
                            leafSCC.dispose();
                            product.close();
                            components.close();
                            automaton.close();
                            nodeSpace.dispose();
                            modelState.dispose();
                            automatonStateDD.dispose();
                            toCheck.dispose();
                            singletonDD.dispose();
                            return ComponentDecision.ACCEPT;
                        } else {
                            DD checkNode = singletonDD.and(modelState);
                            if (checkReachOne(checkNode, subsetProduct, leafSCC, product, nodeSpace)) {
                                log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS_DONE_ACCEPT);
                                product.close();
                                components.close();
                                leafSCC.dispose();
                                automaton.close();
                                nodeSpace.dispose();
                                modelState.dispose();
                                automatonStateDD.dispose();
                                toCheck.dispose();
                                singletonDD.dispose();
                                checkNode.dispose();
                                return ComponentDecision.ACCEPT;                        
                            }
                            checkNode.dispose();
                        }
                    }
                    leafSCC.dispose();
                }
                singletonDD.dispose();
                nodeSpace.dispose();
                automaton.close();
                components.close();
                product.close();
            }
            DD reaching = ComponentsDD.reachPre(graph, automatonStateDD, component, true, false);
            modelState.dispose();
            automatonStateDD.dispose();
            toCheck = toCheck.andNotWith(reaching);
        }
        toCheck.dispose();
        log.send(MessagesLTLLazy.LTL_LAZY_DECIDING_BREAKPOINT_SINGLETONS_DONE_REJECT);
        return ComponentDecision.REJECT;
    }

    private BitSet ddToSubsetState(DD automatonStateDD, AutomatonDDSubset subset)
    {
        BitSet states = UtilBitSet.newBitSetUnbounded();
        int number = 0;
        for (DD dd : subset.getPresVars()) {
            states.set(number, !dd.and(automatonStateDD).isFalseWith());
            number++;
        }
        return states;
    }

    private DD rabinSubsetToDD(ProductGraphDDExplicit product, List<VariableDD> variables)
    {
        AutomatonRabin automaton = (AutomatonRabin) product.getAutomaton();
        int numBuechiStates = automaton.getBuechi().getGraph().getNumNodes();
        HashMap<BitSet,DD> map = new HashMap<>();


        for (DD which : product.getAutomatonStates().keySet()) {
            int value = product.getAutomatonStates().getInt(which);
            BitSet state = ((AutomatonStateBuechiSubset) product.getAutomaton().numberToState(value)).getStates();
            if (!map.containsKey(state)) {
                map.put(state, getContextDD().newConstant(false));
            }
            map.put(state, which.or(map.get(state)));
        }

        /*        
        for (Entry<DD, AutomatonState> entry : product.getAutomatonStates().entrySet()) {
            DD which = entry.getKey();
            BitSet state = ((AutomatonStateBuechi) entry.getValue()).getStates();
            if (!map.containsKey(state)) {
                map.put(state, contextDD.newConstant(false));
            }
            map.put(state, which.or(map.get(state)));
        }
         */
        DD result = getContextDD().newConstant(true);
        for (Entry<BitSet,DD> entry : map.entrySet()) {
            DD symState = getContextDD().newConstant(true);
            for (int bsNumber = 0; bsNumber < numBuechiStates; bsNumber++) {
                DD autLit = variables.get(bsNumber).newCube(0);
                if (!entry.getKey().get(bsNumber)) {
                    autLit = autLit.not();
                }
                symState = symState.and(autLit);
            }
            result = result.or(symState.and(entry.getValue()));
        }
        return result;
    }

    private DD bitSetToDD(BitSet bitSet, List<VariableDD> variables)
    {
        int bitNr = 0;
        DD result = getContextDD().newConstant(true);
        for (VariableDD variable : variables) {
            DD literal = variable.getDDVariables(0).get(0);
            if (!bitSet.get(bitNr)) {
                literal = literal.not();
            } else {
                literal = literal.clone();
            }
            result = result.andWith(literal);
            bitNr++;
        }
        return result;
    }

    private ComponentDecision decideComponentRabinMCLeaf(
            DD component,
            List<DD> stable, List<DD> accepting)
    {
        for (int labelNr = 0; labelNr < stable.size(); labelNr++) {
            DD stableDD = stable.get(labelNr);
            DD acceptingDD = accepting.get(labelNr);
            if (component.equals(component.and(stableDD))
                    && !component.and(acceptingDD).isFalse()) {
                return ComponentDecision.ACCEPT;
            }
        }

        return ComponentDecision.REJECT;
    }

    private ComponentDecision decideComponentRabinMDPLeaf(
            ProductGraphDDExplicit product, DD leafSCC,
            List<DD> stable, List<DD> accepting) {
        ComponentDecision decision = ComponentDecision.REJECT;
        for (int labelNr = 0; labelNr < stable.size(); labelNr++) {
            DD leafSCCAndStable = leafSCC.and(stable.get(labelNr));
            DD prodNodesNot = product.getNodeProperty(CommonProperties.PLAYER)
                    .eq(getContextDD().newConstant(Player.STOCHASTIC));
            DD stableDD = stayIn(product, leafSCCAndStable, prodNodesNot);
            prodNodesNot.dispose();
            //            leafSCCAndStable.dispose();
            if (!stableDD.andWith(accepting.get(labelNr).clone()).isFalseWith()) {
                decision = ComponentDecision.ACCEPT;
                break;
            }
        }
        return decision;
    }

    private StatePair chooseState(DD set, AutomatonDD automaton)
    {
        DD stateSupport = automaton.getPresCube().and(modelGraph.getPresCube());
        DD modelStates = modelGraph.getNodeProperty(CommonProperties.STATE);
        DD stateDD = set.and(modelStates).findSatWith(stateSupport);

        StatePair result = new StatePair();
        result.modelState = stateDD.abstractExist(automaton.getPresCube());
        result.automatonState = stateDD.abstractExist(modelGraph.getPresCube());
        stateDD.dispose();

        return result;
    }


    private DD computeNonIncremental(DecisionMethod method, Buechi buechi,
            StateSetDD forStates) {
        ProductGraphDD product = null;
        AutomatonRabin automatonRabin = null;
        AutomatonDD automatonDD = null;
        DD modelStates = modelGraph.getNodeProperty(CommonProperties.STATE);
        switch (method) {
        case SUBSET:
            log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_SUBSET);
            automatonDD = new AutomatonDDSubset(expressionToDD, buechi, modelStates);
            product = new ProductGraphDDDD(modelGraph, forStates.getStatesDD(), automatonDD);
            log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_SUBSET_DONE);
            break;
        case BREAKPOINT:
            log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_BREAKPOINT);
            automatonDD = new AutomatonDDBreakpoint(expressionToDD, buechi, modelStates);
            product = new ProductGraphDDDD(modelGraph, forStates.getStatesDD(), automatonDD);
            log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_BREAKPOINT_DONE);
            break;
        case RABIN:
            log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_RABIN);
            automatonRabin = UtilAutomaton.newAutomatonRabinSafra(buechi, null);
            product = new ProductGraphDDExplicit(modelGraph, forStates.getStatesDD(), automatonRabin, expressionToDD);
            log.send(MessagesLTLLazy.LTL_LAZY_INITIALISING_AUTOMATON_AND_PRODUCT_MODEL_RABIN_DONE);
            break;
        }

        DD nodeSpace = product.getNodeSpace();
        DD productStates = product.getNodeProperty(CommonProperties.STATE);
        DD nodeSpaceAndSpace = nodeSpace.and(productStates);
        BigInteger numModelStates = nodeSpaceAndSpace.countSat(product.getPresCube());
        nodeSpaceAndSpace.dispose();
        int numAutomatonStates;
        if (method == DecisionMethod.RABIN) {
            numAutomatonStates = automatonRabin.getNumStates();
        } else {
            DD nodeSpaceEx = nodeSpace.abstractExist(modelGraph.getPresCube());
            numAutomatonStates = nodeSpaceEx.countSat(automatonDD.getPresCube()).intValue();
            nodeSpaceEx.dispose();
        }
        log.send(MessagesLTLLazy.LTL_LAZY_EXPLORING_STATE_SPACE_DONE, numModelStates, numAutomatonStates);
        log.send(MessagesLTLLazy.LTL_LAZY_COMPUTING_END_COMPONENTS);

        ArrayList<DD> stable = new ArrayList<>();
        ArrayList<DD> accepting = new ArrayList<>();
        if (method == DecisionMethod.RABIN) {
            DD nonStates = nodeSpace.clone().andWith(productStates.not());
            computeSymbStabAcc((AutomatonRabin) ((ProductGraphDDExplicit) product).getAutomaton(),
                    ((ProductGraphDDExplicit) product).getLabeling(),
                    nonStates, automatonRabin.getNumPairs(), stable, accepting);
            nonStates.dispose();
        }

        DD oneNodes = getContextDD().newConstant(false);
        DD todoNodes = nodeSpace.clone();
        ComponentsDD sccs = new ComponentsDD(product, nodeSpace, skipTransient);
        int numSCCs = 0;
        DD init = product.getInitialNodes();
        DD undecided = getContextDD().newConstant(false);
        for (DD component = sccs.next(); component != null; component = sccs.next()) {
            numSCCs++;
            ComponentDecision result = null;
            switch (method) {
            case SUBSET:
                result = decideComponentSubset((AutomatonDDSubset) automatonDD, component);
                break;
            case BREAKPOINT:
                if (nonDet) {
                    result = decideComponentBreakpointMDPLeaf((AutomatonDDBreakpoint) automatonDD, (ProductGraphDDDD) product, component);
                } else {
                    result = decideComponentBreakpointMCLeaf((AutomatonDDBreakpoint) automatonDD, component);
                }
                break;
            case RABIN:
                if (nonDet) {
                    result = decideComponentRabinMDPLeaf((ProductGraphDDExplicit) product, component, stable, accepting);
                } else {
                    result = decideComponentRabinMCLeaf(component, stable, accepting);
                }
                break;            
            }

            if (result == ComponentDecision.ACCEPT) {
                if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_REMOVE_DECIDED)) {
                    DD componentOld = component;
                    component = ComponentsDD.reachMaxOne(product, component, todoNodes);
                    componentOld.dispose();
                    todoNodes = todoNodes.andNotWith(component.clone());
                }
                oneNodes = oneNodes.orWith(component.clone());
            } else if (result == ComponentDecision.UNDECIDED) {
                undecided = undecided.orWith(component.clone());
            }
            component.dispose();
            if (options.getBoolean(OptionsLTLLazy.LTL_LAZY_STOP_IF_INIT_DECIDED)
                    && init.andNot(oneNodes).isFalseWith()) {
                undecided.dispose();
                undecided = getContextDD().newConstant(false);
                break;
            }
        }
        getContextDD().dispose(stable);
        getContextDD().dispose(accepting);
        todoNodes.dispose();
        sccs.close();
        log.send(MessagesLTLLazy.LTL_LAZY_COMPUTING_END_COMPONENTS_DONE, numSCCs);
        undecided = undecided.andNotWith(oneNodes.clone());
        if (!undecided.isFalseWith()) {
            return null;
        }
        DD reachProbs = computeReachProbs(product, oneNodes, nodeSpace);
        nodeSpace.dispose();
        oneNodes.dispose();
        reachProbs = reachProbs.multiplyWith(product.getInitialNodes().toMT());
        switch (method) {
        case SUBSET: case BREAKPOINT:
            reachProbs = reachProbs.abstractSumWith(automatonDD.getPresCube().clone());
            break;
        case RABIN:
            reachProbs = reachProbs.abstractSumWith(((ProductGraphDDExplicit) product).getAutomatonPresCube().clone());
            break;
        default:
            break;

        }
        product.close();
        if (automatonDD != null) {
            automatonDD.close();
        }
        return reachProbs;
    }

    private void computeSymbStabAcc(AutomatonRabin automaton, Object2IntOpenHashMap<DD> tObjectIntMap,
            DD nonStates, int numLabels, List<DD> stable, List<DD> accepting)
    {
        assert tObjectIntMap != null;
        assert nonStates != null;
        assert stable != null;
        assert accepting != null;
        assert numLabels >= 0;
        assert stable.size() == 0;
        assert accepting.size() == 0;

        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            stable.add(nonStates.clone());
            accepting.add(getContextDD().newConstant(false));
        }

        for (DD key : tObjectIntMap.keySet()) {
            int value = tObjectIntMap.getInt(key);
            AutomatonRabinLabel label = (AutomatonRabinLabel) automaton.numberToLabel(value);
            BitSet labelStable = label.getStable();
            BitSet labelAccepting = label.getAccepting();
            for (int labelNr = 0; labelNr < numLabels; labelNr++) {
                if (labelStable.get(labelNr)) {
                    stable.set(labelNr, stable.get(labelNr).orWith(key.clone()));
                }
                if (labelAccepting.get(labelNr)) {
                    accepting.set(labelNr, accepting.get(labelNr).orWith(key.clone()));
                }
            }
        }        
    }

    private DD stayIn(GraphDD graph, DD target, DD forall)
    {
        assert graph != null;
        assert target != null;
        assert forall != null;
        DD prevTarget = getContextDD().newConstant(false);
        DD transitions = graph.getTransitions();
        DD nextAndActions = graph.getNextCube().and(graph.getActionCube());
        while (!prevTarget.equals(target)) {
            prevTarget.dispose();
            prevTarget = target;
            DD targetNodesNext = target.permute(graph.getSwapPresNext());
            DD targetExist = target.clone().andWith(forall.not());
            DD targetForall = target.and(forall);

            DD targetExistOne = targetNodesNext.abstractAndExist(transitions, nextAndActions);
            targetExistOne = targetExistOne.andWith(targetExist);

            DD targetForallOne = targetNodesNext.not().abstractAndExistWith
                    (transitions.clone(), nextAndActions.clone());
            targetNodesNext.dispose();
            targetForallOne = targetForallOne.notWith().andWith(targetForall);

            target = target.clone().andWith(targetForallOne.orWith(targetExistOne));
        }
        prevTarget.dispose();
        nextAndActions.dispose();

        return target;
    }

    private DD computeReachProbs(GraphDD graphDD, DD target, DD nodeSpace)
    {
        GraphSolverConfigurationDD configuration = UtilGraphSolver.newGraphSolverConfigurationDD();
        List<DD> sinks = new ArrayList<>();
        DD someNodes = ComponentsDD.reachMaxSome(graphDD, target, nodeSpace).andNotWith(target.clone());
        DD zeroNodes = nodeSpace.clone().andNotWith(someNodes).andNotWith(target.clone());
        DD init = graphDD.getInitialNodes();
        if (init.andNot(target).isFalseWith()
                || init.andNot(zeroNodes).isFalseWith()) {
            zeroNodes.dispose();
            return target.toMT();
        }
        sinks.add(zeroNodes);
        sinks.add(target);
        configuration.setGraph(graphDD);
        configuration.setTargetStates(target);
        GraphSolverObjectiveDDUnboundedReachability objective = new GraphSolverObjectiveDDUnboundedReachability();
        objective.setMin(false);
        configuration.setObjective(objective);
        configuration.setSinkStatesDD(sinks);
        configuration.solve();
        zeroNodes.dispose();
        return configuration.getOutputValuesDD();
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
        if (property instanceof ExpressionQuantifier) {
            this.propertyQuantifier = (ExpressionQuantifier) property;
        }
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public StateMap solve() {
        if (modelChecker.getEngine() instanceof EngineDD) {
            this.modelGraph = modelChecker.getLowLevel();
            this.expressionToDD = modelGraph.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        }
        this.type = modelChecker.getModel().getSemantics();
        this.nonDet = SemanticsNonDet.isNonDet(this.type);
        this.skipTransient = options.getBoolean(OptionsLTLLazy.LTL_LAZY_SCC_SKIP_TRANSIENT);
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilLTL.collectLTLInner(quantifiedProp);
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);
            ExpressionToDD expressionToDD = modelGraph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        allStates.close();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType.isMin();
        StateMap result = solve(quantifiedProp, forStates, min);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            result = result.applyWith(op, compare);
        }
        return result;
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        if (propertyQuantifier.getQuantified() instanceof ExpressionReward) {
            return false;
        }
        if (!TypeReal.is(TypeWeight.get())) {
            return false;
        }
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            modelChecker.ensureCanHandle(inner, allStates);
        }
        if (allStates != null) {
            allStates.close();
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        required.add(CommonProperties.EXPRESSION_TO_DD);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }    

    public ContextDD getContextDD() {
        return ContextDD.get();
    }
}
