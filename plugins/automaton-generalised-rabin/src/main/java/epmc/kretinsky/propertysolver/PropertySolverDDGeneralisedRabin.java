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

import gnu.trove.map.TObjectIntMap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import epmc.automaton.ProductGraphDD;
import epmc.automaton.ProductGraphDDExplicit;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.Semantics;
import epmc.kretinsky.automaton.AutomatonGeneralisedRabin;
import epmc.kretinsky.automaton.AutomatonGeneralisedRabinLabel;
import epmc.kretinsky.automaton.AutomatonKretinskyProduct;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public class PropertySolverDDGeneralisedRabin implements PropertySolver {
    public final static String IDENTIFIER = "generalised-rabin-dd";

    private ModelChecker modelChecker;

    private StateSet forStates;

    private boolean negate;

    private boolean nonDet;

    private ContextDD contextDD;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        this.nonDet = modelChecker.getModel().isNonDet();
    }

    @Override
    public boolean canHandle(Expression property, StateSet forStates)
    {
        assert property != null;
        assert forStates != null;
        if (!modelChecker.isEngineDD()) {
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

    @Override
    public StateMap solve(Expression property, StateSet forStates)
    {
        assert property != null;
        assert forStates != null;
        assert property.isQuantifier();
        GraphDD modelGraph = modelChecker.getGraphDD();
        this.forStates = forStates;
        Expression quantified = property.getQuantified();
        boolean min = modelChecker.computeQuantifierDirType(property).isMin();
        if (!modelChecker.getModel().isNonDet()) {
            min = false;
        }
        this.negate = min;

        AutomatonGeneralisedRabin automatonRabin = new AutomatonKretinskyProduct();
        if (negate) {
            quantified = quantified.not();
        }
        Expression[] expressions = modelChecker.relevantExpressionsArray(quantified);

        automatonRabin.setModelChecker(modelChecker);
        automatonRabin.setExpression(quantified, expressions);

        ProductGraphDD product = null;
        DD modelStates = modelGraph.getNodeProperty(CommonProperties.STATE);
        ExpressionToDD expressionToDD = modelChecker.getExpressionToDD();
        Options options = Options.get();

        product = new ProductGraphDDExplicit(modelGraph, forStates.getStatesDD(), automatonRabin, expressionToDD);

        DD nodeSpace = product.getNodeSpace();
        DD productStates = product.getNodeProperty(CommonProperties.STATE);
        DD nodeSpaceAndSpace = nodeSpace.and(productStates);
        BigInteger numModelStates = nodeSpaceAndSpace.countSat(product.getPresCube());
        nodeSpaceAndSpace.dispose();
        int numAutomatonStates;
        numAutomatonStates = automatonRabin.getNumStates();

        List<DD> stable = new ArrayList<>();
        List<List<DD>> accepting = new ArrayList<>();
        DD nonStates = nodeSpace.clone().andWith(productStates.not());
        computeSymbStabAcc((AutomatonGeneralisedRabin) ((ProductGraphDDExplicit) product).getAutomaton(),
                ((ProductGraphDDExplicit) product).getLabeling(),
                nonStates, stable, accepting);
        nonStates.dispose();

        DD oneNodes = contextDD.newConstant(false);
        DD todoNodes = nodeSpace.clone();
        ComponentsDD sccs = new ComponentsDD(product, nodeSpace);
        int numSCCs = 0;
        DD init = product.getInitial();
        for (DD component = sccs.next(); component != null; component = sccs.next()) {
            numSCCs++;
            boolean result = false;
            if (nonDet) {
                result = decideComponentGeneralisedRabinMDPLeaf((ProductGraphDDExplicit) product, component, stable, accepting);
            } else {
                result = decideComponentGeneralisedRabinMCLeaf(component, stable, accepting);
            }

            if (result) {
                if (options.getBoolean(OptionsEPMC.LTL2BA_REMOVE_DECIDED)) {
                    DD componentOld = component;
                    component = ComponentsDD.reachMaxOne(product, component, todoNodes);
                    componentOld.dispose();
                    todoNodes = todoNodes.andNotWith(component.clone());
                }
                oneNodes = oneNodes.orWith(component.clone());
            }

            component.dispose();
        }
        contextDD.dispose(stable);
        for (List<DD> acc : accepting) {
            contextDD.dispose(acc);
        }
        todoNodes.dispose();
        sccs.close();
        DD reachProbs = computeReachProbs(product, oneNodes, nodeSpace);
        nodeSpace.dispose();
        oneNodes.dispose();
        reachProbs = reachProbs.multiplyWith(product.getInitial().toMT());
        reachProbs = reachProbs.abstractSumWith(((ProductGraphDDExplicit) product).getAutomatonPresCube().clone());
        product.close();
        if (negate) {
            reachProbs = contextDD.newConstant(1).subtractWith(reachProbs);
        }

        StateMap result = modelChecker.newStateMap(forStates.clone(), reachProbs);
        if (property.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(property.getCompare(), forStates);
            Operator op = property.getCompareType().asExOpType(ContextValue.get());
            result = result.applyWith(op, compare);
        }

        return result;
    }

    private void computeSymbStabAcc(AutomatonGeneralisedRabin automaton, TObjectIntMap<DD> tObjectIntMap,
            DD nonStates, List<DD> stable, List<List<DD>> accepting)
    {
        assert tObjectIntMap != null;
        assert nonStates != null;
        assert stable != null;
        assert accepting != null;
        assert stable.size() == 0;
        assert accepting.size() == 0;

        int numPairs = automaton.getNumPairs();
        for (int labelNr = 0; labelNr < numPairs; labelNr++) {
            stable.add(nonStates.clone());
            List<DD> acc = new ArrayList<>();
            accepting.add(acc);
            for (int accNr = 0; accNr < automaton.getNumAccepting(labelNr); accNr++) {
                acc.add(contextDD.newConstant(false));
            }
        }

        for (DD key : tObjectIntMap.keySet()) {
            int value = tObjectIntMap.get();
            AutomatonGeneralisedRabinLabel label = (AutomatonGeneralisedRabinLabel) automaton.numberToLabel(value);
            for (int pairNr = 0; pairNr < numPairs; pairNr++) {
                if (label.isStable(pairNr)) {
                    stable.set(pairNr, stable.get().orWith(key.clone()));
                }
                for (int accNr = 0; accNr < automaton.getNumAccepting(pairNr); accNr++) {
                    if (label.isAccepting(pairNr, accNr)) {
                        DD accDD = accepting.get().get();
                        accDD = accDD.orWith(key.clone());
                        accepting.get().set(accNr, accDD);
                    }
                }
            }
        }
    }

    private DD computeReachProbs(GraphDD graphDD, DD target, DD nodeSpace)
    {
        //        target = ComponentsDD.reachMaxOne(graphDD, target, nodeSpace);
        DD someNodes = ComponentsDD.reachMaxSome(graphDD, target, nodeSpace).andNotWith(target.clone());
        DD zeroNodes = nodeSpace.clone().andNotWith(someNodes).andNotWith(target.clone());

        DD init = graphDD.getInitial();
        if (init.andNot(target).isFalseWith()
                || init.andNot(zeroNodes).isFalseWith()) {
            zeroNodes.dispose();
            return target.toMT();
        }

        List<DD> sinks = new ArrayList<>();
        sinks.add(zeroNodes);
        sinks.add(target);
        Semantics semantics = graphDD.getGraphPropertyObject(CommonProperties.SEMANTICS);
        GraphBuilderDD converter = new GraphBuilderDD(graphDD, sinks, semantics.isNonDet());
        zeroNodes.dispose();

        GraphExplicit graph = converter.buildGraph();
        BitSet targets = converter.ddToBitSet(target);
        Type typeWeight = contextValue.getTypeWeight();
        GraphSolverConfiguration iters = modelChecker.newGraphSolverConfiguration();
        iters.setGraph(graph);
        iters.setMin(false);
        iters.setTargetStates(targets);
        iters.setObjective(CommonGraphSolverObjective.UNBOUNDED_REACHABILITY);
        iters.solve();
        Value values = iters.getOutputValues();
        DD result = converter.valuesToDD(values);
        converter.close();
        result = result.multiplyWith(graphDD.getNodeSpace().toMT());
        result = result.addWith(target.andNot(graphDD.getNodeSpace()).toMTWith());
        return result;
    }

    private boolean decideComponentGeneralisedRabinMCLeaf(DD component, List<DD> stable,
            List<List<DD>> accepting) {
        for (int labelNr = 0; labelNr < stable.size(); labelNr++) {
            DD stableDD = stable.get();
            if (!component.and(stableDD).eqWith(component.clone()).isTrueWith()) {
                continue;
            }
            boolean accept = true;
            for (DD acc : accepting.get()) {
                if (component.and(acc).isFalseWith()) {
                    accept = false;
                    break;
                }
            }
            if (accept) {
                return true;
            }
        }

        return false;
    }

    private boolean decideComponentGeneralisedRabinMDPLeaf(
            ProductGraphDDExplicit product, DD leafSCC,
            List<DD> stable, List<List<DD>> accepting) {
        boolean decision = false;
        for (int labelNr = 0; labelNr < stable.size(); labelNr++) {
            DD leafSCCAndStable = leafSCC.and(stable.get());
            DD prodNodesNot = product.getNodeProperty(CommonProperties.PLAYER)
                    .eq(contextDD.newConstant(Player.STOCHASTIC));
            DD stableDD = stayIn(product, leafSCCAndStable, prodNodesNot);
            prodNodesNot.dispose();
            //            leafSCCAndStable.dispose();
            for (DD acc : accepting.get()) {
                if (!stableDD.and(acc).isFalseWith()) {
                    //                    stableDD.dispose();
                    decision = true;
                    break;
                }
            }
        }
        return decision;
    }

    private DD stayIn(GraphDD graph, DD target, DD forall)
    {
        assert graph != null;
        assert target != null;
        assert forall != null;
        DD prevTarget = contextDD.newConstant(false);
        DD transitions = graph.getTransitionsBoolean();
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
}
