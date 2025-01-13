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

package epmc.multiobjective;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.automaton.Automaton;
import epmc.automaton.AutomatonProduct;
import epmc.automaton.AutomatonProductLabel;
import epmc.automaton.AutomatonRabin;
import epmc.automaton.AutomatonRabinLabel;
import epmc.automaton.ProductGraphExplicit;
import epmc.automaton.UtilAutomaton;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.expression.standard.RewardSpecification;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.EdgePropertyApply;
import epmc.graph.explicit.EdgePropertyConstant;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.GraphExplicitWrapper;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.NodePropertyApply;
import epmc.graph.explicit.NodePropertyConstant;
import epmc.graph.explicit.StateSetExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorSet;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

final class ProductBuilder {
    private AutomatonProduct automatonProduct;
    private StateSetExplicit initialStates;
    private BitSet[][] stable;
    private BitSet[][] accepting;
    private int numAutomata;
    private ExpressionMultiObjective property;
    private GraphExplicit graph;
    private BitSet invertedRewards;
    private BitSet rewardProperties;

    ProductBuilder() {
    }

    ProductBuilder setProperty(ExpressionMultiObjective property) {
        this.property = property;
        return this;
    }

    ProductBuilder setModelChecker(ModelChecker modelChecker) {
        return this;
    }

    ProductBuilder setGraph(GraphExplicit graph) {
        this.graph = graph;
        initialStates = graph.newInitialStateSet();
        return this;
    }

    ProductBuilder setInvertedRewards(BitSet invertedRewards) {
        this.invertedRewards = invertedRewards;
        return this;
    }

    ProductBuilder setRewardProperties(BitSet rewardProperties) {
        this.rewardProperties = rewardProperties;
        return this;
    }

    Product build() {
        assert initialStates != null;
        GraphExplicit prodWrapper = computeProductGraph(initialStates);
        GraphBuilderExplicit builder = new GraphBuilderExplicit();
        builder.setInputGraph(prodWrapper);
        builder.addDerivedGraphProperties(prodWrapper.getGraphProperties());
        builder.addDerivedNodeProperty(CommonProperties.STATE);
        builder.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
        builder.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        builder.setReorder();
        builder.build();
        GraphExplicit iterGraph = builder.getOutputGraph();
        IterationRewards rewards = computeRewards(builder);
        return new Product(iterGraph, rewards, numAutomata);
    }

    private GraphExplicit computeProductGraph(StateSetExplicit initialStates) {
        Set<Expression> expressionsSet = new HashSet<>();
        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (ExpressionReward.is(quantified)) {
                // TODO
            } else if (ExpressionSteadyState.is(quantified)) {
                // TODO
            } else {
                Set<Expression> inners = UtilLTL.collectLTLInner(quantified);
                expressionsSet.addAll(inners);
            }
        }
        Expression[] expressions = expressionsSet.toArray(new Expression[expressionsSet.size()]);
        List<Automaton> automata = new ArrayList<>();
        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (ExpressionReward.is(quantified)) {
                quantified = ExpressionLiteral.getFalse();
            } else if (ExpressionSteadyState.is(quantified)) {
                quantified = ExpressionLiteral.getFalse();
            }
            AutomatonRabin automaton = UtilAutomaton.newAutomatonRabin(quantified, expressions);
            automata.add(automaton);
        }
        automatonProduct = new AutomatonProduct(automata);
        numAutomata = automatonProduct.getNumComponents();
        List<Object> prodNodeProperties = new ArrayList<>();
        prodNodeProperties.add(CommonProperties.STATE);
        prodNodeProperties.add(CommonProperties.PLAYER);
        List<Object> prodEdgeProperties = new ArrayList<>();
        prodEdgeProperties.add(CommonProperties.WEIGHT);
        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (quantified instanceof ExpressionReward) {
                RewardSpecification rewardStructure = ((ExpressionReward) quantified).getReward();
                prodNodeProperties.add(rewardStructure);
                prodEdgeProperties.add(rewardStructure);
            }
        }
        ProductGraphExplicit product = new ProductGraphExplicit.Builder()
                .setModel(graph)
                .setModelInitialNodes(initialStates.getStatesExplicit())
                .setAutomaton(automatonProduct)
                .setAutomatonInitialState(automatonProduct.getInitState())
                .addGraphProperties(graph.getGraphProperties())
                .addNodeProperties(prodNodeProperties)
                .addEdgeProperties(prodEdgeProperties)
                .build();

        GraphExplicitWrapper prodWrapper = new GraphExplicitWrapper(product);
        prodWrapper.addDerivedGraphProperties(product.getGraphProperties());
        prodWrapper.addDerivedNodeProperty(CommonProperties.STATE);
        prodWrapper.addDerivedNodeProperty(CommonProperties.PLAYER);
        prodWrapper.addDerivedNodeProperty(CommonProperties.AUTOMATON_LABEL);
        prodWrapper.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
        prodWrapper.addDerivedEdgeProperty(CommonProperties.WEIGHT);

        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (quantified instanceof ExpressionReward) {
                RewardSpecification rewardStructure = ((ExpressionReward) quantified).getReward();
                prodWrapper.addDerivedNodeProperty(rewardStructure);
                prodWrapper.addDerivedEdgeProperty(rewardStructure);
            }
        }

        prodWrapper.explore();
        return prodWrapper;
    }

    private IterationRewards computeRewards(GraphBuilderExplicit builder)
    {
        GraphExplicit prodWrapper = builder.getInputGraph();
        NodeProperty[] stateRewards = new NodeProperty[property.getOperands().size()];
        EdgeProperty[] transRewards = new EdgeProperty[property.getOperands().size()];
        int propNr = 0;
        Value zero = UtilValue.newValue(TypeWeight.get(), 0);
        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (ExpressionReward.is(quantified)) {
                RewardSpecification rewardStructure = ((ExpressionReward) quantified).getReward();
                stateRewards[propNr] = prodWrapper.getNodeProperty(rewardStructure);
                transRewards[propNr] = prodWrapper.getEdgeProperty(rewardStructure);
            } else {
                stateRewards[propNr] = new NodePropertyConstant(prodWrapper, zero);
                transRewards[propNr] = new EdgePropertyConstant(prodWrapper, zero);
            }
            if (invertedRewards.get(propNr)) {
                stateRewards[propNr] = new NodePropertyApply(prodWrapper, OperatorAddInverse.ADD_INVERSE, stateRewards[propNr]);
                transRewards[propNr] = new EdgePropertyApply(prodWrapper, OperatorAddInverse.ADD_INVERSE, transRewards[propNr]);
            }
            propNr++;
        }

        Map<BitSet,BitSet> resultMap = computeCombinations(builder);
        GraphExplicitSparseAlternate iterGraph = (GraphExplicitSparseAlternate) builder.getOutputGraph();

        IterationRewards result = new IterationRewards(iterGraph, numAutomata);
        int numStates = iterGraph.computeNumStates();
        BitSet empty = UtilBitSet.newBitSetUnbounded();
        for (int iterState = 0; iterState < numStates; iterState++) {
            int state = builder.outputToInputNode(iterState);
            if (iterState < 0 || iterState >= numStates) {
                continue;
            }
            boolean found = false;
            for (Entry<BitSet, BitSet> entry : resultMap.entrySet()) {
                BitSet combination = entry.getKey();
                BitSet states = entry.getValue();
                if (states.get(state)) {
                    result.addCombination(combination);
                    found = true;
                }
            }
            if (!found) {
                result.addCombination(empty);
            }
            ValueAlgebra stateReward = newValueWeight();
            ValueAlgebra transReward = newValueWeight();
            OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
            int numSucc = iterGraph.getNumSuccessors(iterState);
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
            for (int obj = 0; obj < numAutomata; obj++) {
                set.apply(stateReward, zero);
                NodeProperty stateRewardProp = stateRewards[obj];
                set.apply(stateReward, stateRewardProp.get(state));
                EdgeProperty edgeRewardProp = transRewards[obj];
                for (int succNr = 0; succNr < numSucc; succNr++) {
                    // TODO HACK
                    set.apply(transReward, stateReward);
                    add.apply(transReward, transReward, edgeRewardProp.get(state, succNr));
                    int succ = prodWrapper.getSuccessorNode(state, succNr);
                    add.apply(transReward, transReward, edgeRewardProp.get(succ, 0));
                    result.setReward(transReward, succNr, obj);
                }
            }
            result.finishState();
        }
        return result;
    }

    private Map<BitSet,BitSet> computeCombinations(GraphBuilderExplicit builder) {
        computeStableAccepting(builder.getInputGraph());
        Map<BitSet,BitSet> todoMap = new HashMap<>();
        Map<BitSet,BitSet> resultMap = new HashMap<>();

        Deque<BitSet> todo = new LinkedList<>();
        BitSet initBitSet = UtilBitSet.newBitSetUnbounded();
        for (int prop = 0; prop < numAutomata; prop++) {
            initBitSet.set(prop);
        }
        initBitSet.andNot(rewardProperties);
        todo.add(initBitSet);
        GraphExplicit prodWrapper = builder.getInputGraph();
        BitSet todoBS = UtilBitSet.newBitSetUnbounded();
        todoBS.set(0, prodWrapper.getNumNodes());
        todoMap.put(initBitSet, todoBS);
        while (!todo.isEmpty()) {
            BitSet combination = todo.getLast();
            todo.removeLast();
            BitSet states = todoMap.get(combination);
            todoMap.remove(combination);
            BitSet accepting = computeAccepting(builder.getInputGraph(), states, combination);
            resultMap.put(combination, accepting);
            for (int prop = combination.nextSetBit(0); prop >= 0;
                    prop = combination.nextSetBit(prop+1)) {
                combination.set(prop, false);
                if (!resultMap.containsKey(combination)
                        && !todoMap.containsKey(combination)
                        && !combination.isEmpty()) {
                    todo.addFirst(combination.clone());
                    BitSet remaining = states.clone();
                    remaining.andNot(accepting);
                    assert remaining != null;
                    todoMap.put(combination.clone(), remaining);
                }
                combination.set(prop, true);
            }
        }
        return resultMap;
    }

    private void computeStableAccepting(GraphExplicit prodWrapper) {
        NodeProperty automatonLabel = prodWrapper.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        stable = new BitSet[automatonProduct.getNumComponents()][];
        accepting = new BitSet[automatonProduct.getNumComponents()][];
        int automatonNr = 0;
        for (Automaton automaton : automatonProduct.getAutomata()) {
            AutomatonRabin automatonRabin = (AutomatonRabin) automaton;
            BitSet[] automatonStable = new BitSet[automatonRabin.getNumPairs()];
            BitSet[] automatonAccepting = new BitSet[automatonRabin.getNumPairs()];
            for (int label = 0; label < automatonRabin.getNumPairs(); label++) {
                BitSet labelStableBitSet = UtilBitSet.newBitSetUnbounded();
                BitSet labelAcceptingBitSet = UtilBitSet.newBitSetUnbounded();
                assert prodWrapper != null;
                for (int node = 0; node < prodWrapper.getNumNodes(); node++) {
                    AutomatonProductLabel prodLabel = automatonLabel.getObject(node);
                    AutomatonRabinLabel rabinLabel = (AutomatonRabinLabel) automaton.numberToLabel(prodLabel.get(automatonNr));
                    labelStableBitSet.set(node, rabinLabel.getStable().get(label));
                    labelAcceptingBitSet.set(node, rabinLabel.getAccepting().get(label));
                }
                automatonStable[label] = labelStableBitSet;
                automatonAccepting[label] = labelAcceptingBitSet;
            }
            stable[automatonNr] = automatonStable;
            accepting[automatonNr] = automatonAccepting;
            automatonNr++;
        }
    }

    private BitSet computeAccepting(GraphExplicit prodWrapper, BitSet states, BitSet properties)
    {
        assert states != null;
        assert properties != null;
        int numCombinations = 1;
        for (int prop = properties.nextSetBit(0); prop >= 0;
                prop = properties.nextSetBit(prop+1)) {
            AutomatonRabin automaton = (AutomatonRabin) automatonProduct.getAutomaton(prop);
            numCombinations *= automaton.getNumPairs();
        }

        BitSet result = UtilBitSet.newBitSetUnbounded();
        BitSet stable = UtilBitSet.newBitSetUnbounded();
        for (int combNumber = 0; combNumber < numCombinations; combNumber++) {
            stable.clear();
            stable.or(states);
            int numberPart = combNumber;
            for (int prop = properties.nextSetBit(0); prop >= 0;
                    prop = properties.nextSetBit(prop+1)) {
                AutomatonRabin automaton = (AutomatonRabin) automatonProduct.getAutomaton(prop);
                int labelNr = numberPart % automaton.getNumPairs();
                numberPart /= automaton.getNumPairs();
                stable.and(this.stable[prop][labelNr]);
            }
            ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
            components.removeLeavingAttr(prodWrapper, stable);
            EndComponents endComponents = components.maximalEndComponents(prodWrapper, stable);
            for (BitSet mec = endComponents.next(); mec != null; mec = endComponents.next()) {
                numberPart = combNumber;
                boolean allThere = true;
                for (int prop = properties.nextSetBit(0); prop >= 0;
                        prop = properties.nextSetBit(prop+1)) {
                    AutomatonRabin automaton = (AutomatonRabin) automatonProduct.getAutomaton(prop);
                    int labelNr = numberPart % automaton.getNumPairs();
                    numberPart /= automaton.getNumPairs();
                    boolean mecAccepting = false;
                    BitSet accBS = this.accepting[prop][labelNr];
                    for (int mecBit = mec.nextSetBit(0); mecBit >= 0;
                            mecBit = mec.nextSetBit(mecBit+1)) {
                        if (accBS.get(mecBit)) {
                            mecAccepting = true;
                            break;
                        }
                    }
                    if (!mecAccepting) {
                        allThere = false;
                        break;
                    }
                }
                if (allThere) {
                    result.or(mec);
                }
            }
        }
        //        result = ComponentsExplicit.reachMaxOne(prodWrapper, result);
        return result;
    }

    private ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
