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

package epmc.kretinsky.propertysolver;

import java.util.BitSet;
import java.util.Set;

import epmc.automaton.ProductGraphExplicit;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.Semantics;
import epmc.kretinsky.automaton.AutomatonGeneralisedRabin;
import epmc.kretinsky.automaton.AutomatonGeneralisedRabinLabel;
import epmc.kretinsky.automaton.AutomatonKretinskyProduct;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;

public final class PropertySolverExplicitGeneralisedRabin implements PropertySolver {
    public final static String IDENTIFIER = "generalised-rabin-explicit";
    private ModelChecker modelChecker;
    private Options options;
    private GraphExplicit graph;
    private StateSet forStates;
    private boolean negate;
    private Log log;
    private boolean nonDet;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.isEngineExplicit()) {
            this.graph = modelChecker.getGraphExplicit();
            this.contextValue = options.get();
            this.log = options.get(OptionsMessages.LOG);
        }
        this.nonDet = modelChecker.getModel().isNonDet();
    }

    @Override
    public StateMap solve(Expression property, StateSet forStates)
    {
        assert property != null;
        assert forStates != null;
        assert property.isQuantifier();
        this.forStates = forStates;
        Expression quantified = property.getQuantified();
        boolean min = modelChecker.computeQuantifierDirType(property).isMin();
        if (!modelChecker.getModel().isNonDet()) {
            min = false;
        }
        this.negate = min;

        AutomatonGeneralisedRabin observer = new AutomatonKretinskyProduct();// modelChecker.newObserverGeneralisedRabin(quantified, relExp);
        if (negate) {
            quantified = quantified.not();
        }
        Expression[] expressions = modelChecker.relevantExpressionsArray(quantified);
        for (Expression expression : expressions) {
            if (!graph.getNodeProperties().contains(expression)) {
                assert expression.isNot();
                Expression inner = expression.getOperand1();
                NodeProperty neg = modelChecker.newNodePropertyApply(graph.getNodeProperty(inner), ContextValue.get().getOperator(OperatorNot.IDENTIFIER));
                graph.registerNodeProperty(expression, neg);
            }
        }

        observer.setModelChecker(modelChecker);
        observer.setExpression(quantified, expressions);
        GraphExplicit modelGraph = modelChecker.getGraphExplicit();
        ProductGraphExplicit prodGraph = new ProductGraphExplicit(modelGraph, forStates.getStatesExplicit(), observer);
        GraphBuilderExplicit builder = modelChecker.newGraphBuilderExplicit();
        builder.setWrapper();
        builder.setInputGraph(prodGraph);
        builder.addDerivedGraphProperties(prodGraph.getGraphProperties());
        builder.addDerivedNodeProperty(CommonProperties.STATE);
        builder.addDerivedNodeProperty(CommonProperties.PLAYER);
        builder.addDerivedNodeProperty(CommonProperties.AUTOMATON_LABEL);
        builder.addDerivedNodeProperty(CommonProperties.NODE_AUTOMATON);
        builder.addDerivedNodeProperty(CommonProperties.NODE_MODEL);
        builder.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        builder.build();
        GraphExplicit graph = builder.getOutputGraph();
        log.send(MessagesEPMC.EXPLORING_STATE_SPACE);
        graph.explore();
        log.send(MessagesEPMC.EXPLORING_STATE_SPACE_DONE, graph.computeNumStates(),
                observer.getNumStates());
        log.send(MessagesEPMC.COMPUTING_END_COMPONENTS);
        BitSet acc = new BitSet();
        BitSet undecided = new BitSet();
        BitSet initNs = graph.getInitialNodes();
        BitSet init = (java.util.BitSet) initNs.clone();

        ComponentsExplicit components = modelChecker.newComponentsExplicit();
        EndComponents endComponents = components.maximalEndComponents(graph);
        int numComponents = 0;
        int numECCStates = 0;
        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
        for (BitSet leafSCC = endComponents.next(); leafSCC != null; leafSCC = endComponents.next()) {
            numComponents++;
            for (int node = leafSCC.nextSetBit(0); node >= 0; node = leafSCC.nextSetBit(node+1)) {
                graph.queryNode(node);
                if (isState.getBoolean()) {
                    numECCStates++;
                }
            }
            if (options.getBoolean(OptionsEPMC.LTL2BA_STOP_IF_INIT_DECIDED)) {
                BitSet testInit = (BitSet) init.clone();
                testInit.and(acc);
                if (testInit.equals(init)) {
                    undecided.clear();
                    break;
                }
            }

            boolean decision;
            if (nonDet) {
                decision = decideComponentGeneralisedRabinMDPLeaf(graph, leafSCC);
            } else {
                decision = decideComponentGeneralisedRabinMCLeaf(graph, leafSCC);                
            }
            if (decision) {
                if (options.getBoolean(OptionsEPMC.LTL2BA_REMOVE_DECIDED)) {
                    leafSCC = components.reachMaxOne(graph, leafSCC);
                }
                acc.or(leafSCC);
            }
        }
        log.send(MessagesEPMC.NUM_END_COMPONENTS, numComponents);
        log.send(MessagesEPMC.NUM_ECC_STATES, numECCStates);
        undecided.andNot(acc);
        if (!undecided.isEmpty()) {
            return null;
        }

        StateMap resultValues = prodToOrigResult(prepareAndIterate(graph, acc), graph);
        if (property.getCompareType() == CmpType.IS) {
            return resultValues;
        } else {
            StateMap compare = modelChecker.check(property.getCompare(), forStates);
            Operator op = property.getCompareType().asExOpType(contextValue);
            return resultValues.applyWith(op, compare);
        }
    }

    @Override
    public boolean canHandle(Expression property, StateSet states) {
        assert property != null;
        assert states != null;
        if (!modelChecker.isEngineExplicit()) {
            return false;
        }
        if (!property.isQuantifier()) {
            return false;
        }
        if (property.getQuantified().isReward()) {
            return false;
        }
        Set<Expression> inners = modelChecker.collectNonBasic(property.getQuantified());
        for (Expression inner : inners) {
            modelChecker.ensureCanHandle(inner, modelChecker.getAllStates());
        }
        return true;
    }

    static boolean decideComponentGeneralisedRabinMCLeaf(GraphExplicit graph, BitSet leafSCC) {
        AutomatonGeneralisedRabin rabin = graph.getGraphProperty(CommonProperties.AUTOMATON).getObject();
        NodeProperty automatonLabel = graph.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        int numPairs = rabin.getNumPairs();
        BitSet[] accepting = new BitSet[rabin.getNumPairs()];
        for (int pair = 0; pair < numPairs; pair++) {
            accepting[pair] = new BitSet(rabin.getNumAccepting(pair));
        }
        BitSet stable = new BitSet(rabin.getNumPairs());
        stable.set(0, rabin.getNumPairs());
        for (int node = leafSCC.nextSetBit(0); node >= 0; node = leafSCC.nextSetBit(node+1)) {
            graph.queryNode(node);
            AutomatonGeneralisedRabinLabel label = automatonLabel.getObject();
            for (int pair = 0; pair < numPairs; pair++) {
                if (!label.isStable(pair)) {
                    stable.clear(pair);
                }
                for (int acc = 0; acc < rabin.getNumAccepting(pair); acc++) {
                    if (label.isAccepting(pair, acc)) {
                        accepting[pair].set(acc);
                    }
                }
            }
        }
        for (int pair = 0; pair < numPairs; pair++) {
            for (int acc = 0; acc < rabin.getNumAccepting(pair); acc++) {
                if (!accepting[pair].get(acc)) {
                    stable.clear(pair);
                }
            }
        }
        return stable.cardinality() > 0;
    }

    static boolean decideComponentGeneralisedRabinMDPLeaf(GraphExplicit graph,
            BitSet leafSCC) {
        AutomatonGeneralisedRabin rabin = graph.getGraphProperty(CommonProperties.AUTOMATON).getObject();
        boolean accepting = false;
        NodeProperty automatonLabel = graph.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int label = 0; label < rabin.getNumPairs(); label++) {
            BitSet existing = new BitSet();
            for (int node = leafSCC.nextSetBit(0); node >= 0; node = leafSCC.nextSetBit(node+1)) {
                graph.queryNode(node);
                AutomatonGeneralisedRabinLabel rabinLabel = automatonLabel.getObject();
                if (rabinLabel.isStable(label)) {
                    existing.set(node);
                }
            }
            ComponentsExplicit components = new ComponentsExplicitImpl();
            components.removeLeavingAttr(graph, existing);
            BitSet acceptance = new BitSet();
            int labelSize = rabin.getNumAccepting(label);
            if (existing.cardinality() > 0 && labelSize == 0) {
                accepting = true;
            } else {
                for (int node = existing.nextSetBit(0); node >= 0; node = existing.nextSetBit(node+1)) {
                    graph.queryNode(node);
                    AutomatonGeneralisedRabinLabel rabinLabel = automatonLabel.getObject();
                    for (int acc = 0; acc < labelSize; acc++) {
                        if (rabinLabel.isAccepting(label, acc)) {
                            acceptance.set(acc);
                            if (acceptance.cardinality() == labelSize) {
                                accepting = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (accepting) {
                break;
            }
        }
        return accepting;
    }

    private Value prepareAndIterate(GraphExplicit graph, BitSet acc) {
        log.send(MessagesEPMC.PREPARING_MDP_FOR_ITERATION);
        BitSet test = (BitSet) graph.getInitialNodes().clone();
        test.andNot(acc);
        Type typeWeight = contextValue.getTypeWeight();
        if (test.cardinality() == 0) {
            Value values = contextValue.newValueArrayWeight(graph.getQueriedNodesLength());
            for (int node = graph.getInitialNodes().nextSetBit(0); node >= 0;
                    node = graph.getInitialNodes().nextSetBit(node+1)) {
                values.set(typeWeight.getOne(), node);
            }
            return values;
        }

        Semantics semanticsType = graph.getGraphProperty(CommonProperties.SEMANTICS).getObject();
        boolean embed = semanticsType.isContinuousTime();
        GraphBuilderExplicit builder = modelChecker.newGraphBuilderExplicit();
        builder.setInputGraph(graph);
        builder.addDerivedGraphProperties(graph.getGraphProperties());
        builder.addDerivedNodeProperty(CommonProperties.PLAYER);
        builder.addDerivedNodeProperty(CommonProperties.STATE);
        builder.addDerivedEdgeProperty(CommonProperties.WEIGHT);
        builder.addSink(acc);
        builder.setForIteration();
        builder.build();
        GraphExplicit iterGraph = builder.getOutputGraph();
        if (embed) {
            GraphExplicitModifier.embed(iterGraph);
        }

        BitSet target = new BitSet(iterGraph.computeNumStates());

        NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
        for (int node = acc.nextSetBit(0); node >= 0; node = acc.nextSetBit(node+1)) {
            graph.queryNode(node);
            if (isState.getBoolean()) {
                target.set(builder.inputToOutputNode(node));
            }
        }
        log.send(MessagesEPMC.PREPARING_MDP_FOR_ITERATION_DONE, iterGraph.computeNumStates());
        GraphSolverConfiguration iters = modelChecker.newGraphSolverConfiguration();
        iters.setGraph(iterGraph);
        iters.setMin(false);
        iters.setTargetStates(target);
        iters.setObjective(CommonGraphSolverObjective.UNBOUNDED_REACHABILITY);
        iters.solve();
        Value values = iters.getOutputValues();
        Value entry = typeWeight.newValue();
        Value result = contextValue.newValueArrayWeight(graph.getQueriedNodesLength());
        Value one = contextValue.getTypeWeight().getOne();
        for (int node = graph.getQueriedNodes().nextSetBit(0); node >= 0; node = graph.getQueriedNodes().nextSetBit(node+1)) {
            graph.queryNode(node);
            if (isState.getBoolean()) {
                int newNode = builder.inputToOutputNode(node);
                values.get(entry, newNode);
                if (negate) {
                    entry.subtract(one, entry);
                }
                result.set(entry, node);
            }
        }
        return result;
    }

    private StateMap prodToOrigResult(Value iterResult,
            GraphExplicit prodGraph) {
        assert iterResult != null;
        assert prodGraph != null;
        Type typeWeight = contextValue.getTypeWeight();
        Value entry = typeWeight.newValue();
        BitSet nodes = forStates.getStatesExplicit();
        Value resultValues = typeWeight.getTypeArray().newValue(forStates.size());
        int i = 0;
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            prodGraph.queryNode(node);
            iterResult.get(entry, i);
            resultValues.set(entry, i);
            i++;
        }
        return modelChecker.newStateMap(forStates.clone(), resultValues);
    }
}
