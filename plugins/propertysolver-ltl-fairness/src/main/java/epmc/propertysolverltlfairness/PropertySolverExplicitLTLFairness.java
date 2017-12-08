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

package epmc.propertysolverltlfairness;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.Type;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

/**
 * Input property is fairness in LTL.
 * Currently we only support the formulas in which only X, F, G and U modalities
 * occur. In addition, we do not allow F and G occur in the scope of U and X.
 * Please input LTL formula which is in negation normal form.
 * @author Yong Li
 */
public final class PropertySolverExplicitLTLFairness implements PropertySolver {
    public final static String IDENTIFIER = "ltl-fairness-explicit";
    private GraphExplicit modelGraph;
    private Options options;
    private Log log;
    private Set<Expression> stateLabels;
    private ModelChecker modelChecker;
    private Expression property;
    private ExpressionQuantifier propertyQuantifier;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
            this.modelGraph = modelChecker.getLowLevel();
        }
        this.log = options.get(OptionsMessages.LOG);
    }

    // must have P[max,min][>= k,=?,<=] [LTL-prop form]
    @Override
    public boolean canHandle() {
        assert property != null;
        // TODO Auto-generated method stub
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier propQuantifier = (ExpressionQuantifier)property;
        return UtilLTL.isValidLTL(propQuantifier.getQuantified()) 
                && UtilLTL.isFairLTL(propQuantifier.getQuantified(), false, false);
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        return Collections.unmodifiableSet(required);
    }

    public static void print(Set<Set<Expression>> sets) {
        System.out.println("{ ");
        for (Set<Expression> set : sets) {
            print1(set);
        }
        System.out.println("}");
    }

    public static void print1(Set<Expression> set) {
        System.out.print(" { ");
        for (Expression exp : set) {
            System.out.print(exp + " ");
        }
        System.out.println(" }");
    }

    /**
     * Flatten operator defined in the paper
     */
    public static Set<Set<Expression>> flatten(Expression prop) {
        if(prop instanceof ExpressionPropositional) {
            Set<Set<Expression>> setOfSets = new HashSet<>();
            Set<Expression>       set      = new HashSet<>();
            set.add(prop);
            setOfSets.add(set);
            return setOfSets;
        }

        if (prop instanceof ExpressionOperator) { // AND, OR will be flattened
            Set<Set<Expression>> set = null;
            ExpressionOperator expressionOperator = (ExpressionOperator)prop;
            Operator operator = expressionOperator.getOperator();
            if (operator.equals(OperatorOr.OR)) {
                Set<Set<Expression>> op1Set = flatten(expressionOperator.getOperand1());
                op1Set.addAll(flatten(expressionOperator.getOperand2())); 
                return op1Set;
            } else if (operator.equals(OperatorAnd.AND)) {
                set = new HashSet<>();
                Set<Set<Expression>> set1 = flatten(expressionOperator.getOperand1());
                Set<Set<Expression>> set2 = flatten(expressionOperator.getOperand2());
                // CNF => DNF
                for (Set<Expression> p1 : set1) {
                    for (Set<Expression> p2 : set2) {
                        Set<Expression> tmp = new HashSet<>(p1);
                        tmp.addAll(p2); 
                        set.add(tmp);
                    }
                } 
                return set;
            } else {
                assert false;
            }
        } else if (ExpressionTemporalFinally.is(prop)) {
            Set<Set<Expression>> result = new HashSet<>();
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(prop);
            Set<Set<Expression>> setOp1 = flatten(expressionTemporal.getOperand()); // flatten op1 
            if (ExpressionTemporalGlobally.is(expressionTemporal.getOperand())
                    || ExpressionTemporalFinally.is(expressionTemporal.getOperand())) {
                return setOp1;
            }
            // if no temporal operators in op1
            for (Set<Expression> set : setOp1) {
                Set<Expression> tmp = new HashSet<>();
                Expression conjs = null;
                for (Expression p : set) {
                    if (ExpressionTemporalGlobally.is(p) ||
                            ExpressionTemporalFinally.is(p)) { // F l, G l
                        tmp.add(p);
                    } else {
                        conjs = conjs == null ?
                                // conjunction
                                p : UtilLTL.newOperator(OperatorAnd.AND, conjs, p); 
                    }
                }
                if(conjs != null) tmp.add(UtilLTL.newFinally(conjs));
                result.add(tmp);
            }
            return result;
        } else if (ExpressionTemporalGlobally.is(prop)) {
            Set<Set<Expression>> result = new HashSet<>();
            Set<Set<Expression>> setOp1 = null;
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(prop);
            setOp1 = flatten(expressionTemporal.getOperand());
            if (ExpressionTemporalGlobally.is(expressionTemporal.getOperand())
                    || ExpressionTemporalFinally.is(expressionTemporal.getOperand()))
                return setOp1;
            Set<Set<Expression>> cnfSet = UtilLTL.permute(setOp1); // to CNF form
            for (Set<Expression> set : cnfSet) {
                Set<Expression> tmp = new HashSet<>();
                Expression disjs = null;
                for (Expression p : set) {
                    if (ExpressionTemporalGlobally.is(p)
                            || ExpressionTemporalFinally.is(p)) { // G l, F l
                        tmp.add(p);
                    } else {
                        disjs = disjs == null ?
                                // disjunction
                                p : UtilLTL.newOperator(OperatorOr.OR, disjs, p); 
                    }
                }
                if (disjs != null)
                    tmp.add(UtilLTL.newGlobally(disjs));
                result.add(tmp);
            }
            return UtilLTL.permute(result);
        }
        assert false;
        return null;
    }


    /**
     * find all BSCCs that satisfied the Expression
     */
    public BitSet getAcceptingBSCCs(GraphExplicit graph, Expression prop)
    {
        assert (!(prop instanceof ExpressionQuantifier));
        Expression normFromProp = prop;
        Set<Set<Expression>> clauses = PropertySolverExplicitLTLFairness
                .flatten(normFromProp);

        BitSet acBSCCs = UtilBitSet.newBitSetUnbounded();

        log.send(MessagesLTLFairness.LTL_FAIRNESS_COMPUTING_END_COMPONENTS);
        ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
        EndComponents endComponents = components.maximalEndComponents(graph);
        int num = 0;
        for (   BitSet scc = endComponents.next();
                scc != null; 
                scc = endComponents.next()) {
            if (isBSCC(graph, scc)) { 
                for (Set<Expression> clause : clauses) {
                    if (checkBSCC(graph, scc, clause)) { 
                        acBSCCs.or(scc);
                        num++;
                        break;
                    }
                }
            }
        }

        log.send(MessagesLTLFairness.LTL_FAIRNESS_NUM_END_COMPONENTS, num);
        return acBSCCs;
    }

    /**
     * check whether the SCC satisfied set from Expression
     * FG l /\ GF l1 /\ GF l2 /\ ...
     */
    private boolean checkBSCC(GraphExplicit graph, BitSet scc,
            Set<Expression> set) {
        Expression globalFormula = null;
        List<Expression> finalFormulas = new LinkedList<>();
        for (Expression formula : set) {
            if (ExpressionTemporalFinally.is(formula)) {
                finalFormulas.add(ExpressionTemporalFinally.as(formula).getOperand());
            } else if (ExpressionTemporalGlobally.is(formula)){
                ExpressionTemporalGlobally formulaTemporal = ExpressionTemporalGlobally.as(formula);
                globalFormula = globalFormula == null? 
                        formulaTemporal.getOperand() :
                            UtilExpressionStandard.opAnd(globalFormula, formulaTemporal.getOperand());                
            } else {
                assert false;
            }
        }

        for (int node = scc.nextSetBit(0); node >= 0; node = scc
                .nextSetBit(node + 1)) { 
            if(globalFormula != null 
                    &&  !checkNode(graph, node, stateLabels, globalFormula)) {
                return false;
            }
            Iterator<Expression> iterator = finalFormulas.iterator();
            while(iterator.hasNext()) {
                if (checkNode(graph, node, stateLabels, iterator.next())){
                    iterator.remove();
                }
            }
        }// think this way is better 
        return finalFormulas.isEmpty();
    }


    /**
     * check node whether its satisfies the given literal formula
     */
    private boolean checkNode(GraphExplicit graph, int node, Set<Expression> labels,
            Expression lit) {
        // lit may be combination of labels
        assert lit != null && !(ExpressionTemporalGlobally.is(lit)
                || ExpressionTemporalFinally.is(lit));
        if (labels.contains(lit)) {
            return graph.getNodeProperty(lit).getBoolean(node);
        } else if (lit instanceof ExpressionLiteral){
            return false;
        } else {
            ExpressionOperator expressionOperator = (ExpressionOperator) lit;
            Operator operator = expressionOperator.getOperator();
            if (operator.equals(OperatorNot.NOT)) {
                return !checkNode(graph, node, labels, expressionOperator.getOperand1());
            } else if (operator.equals(OperatorOr.OR)) {
                return checkNode(graph, node, labels, expressionOperator.getOperand1())
                        || checkNode(graph, node, labels, expressionOperator.getOperand2());
            } else if (operator.equals(OperatorAnd.AND)) {
                return checkNode(graph, node, labels, expressionOperator.getOperand1())
                        && checkNode(graph, node, labels, expressionOperator.getOperand2());
            } else {
                assert false : "ERROR literal not in labels";   
            }
        }
        return true;
    }
    // usually we do not need to check this
    private boolean isBSCC(GraphExplicit graph, BitSet ecc)
    {
        boolean isBSCC = true;
        for (int node = ecc.nextSetBit(0); node >= 0; node = ecc
                .nextSetBit(node + 1)) {
            for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                int succ = graph.getSuccessorNode(node, succNr);
                // all successors must in itself
                if (!ecc.get(succ)) { 
                    isBSCC = false;
                    break;
                }
            }
        }
        return isBSCC;
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
        Semantics type = modelGraph
                .getGraphPropertyObject(CommonProperties.SEMANTICS);
        assert !SemanticsNonDet.isNonDet(type);

        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
            UtilGraph.registerResult(modelGraph, inner, innerResult);
        }
        this.stateLabels = inners;
        allStates.close();
        StateMap result = doSolve(modelGraph, forStates, quantifiedProp);
        if (!propertyQuantifier.getCompareType().isIs()) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            assert op != null;
            result = result.applyWith(op, compare);
        }
        return result;
    }

    private StateMap doSolve(GraphExplicit origGraph, StateSet states,
            Expression quantifiedProp) {
        // it seems that no need to rebuild the graph
        GraphExplicit graph = origGraph;//builder.getOutputGraph();
        log.send(MessagesLTLFairness.LTL_FAIRNESS_EXPLORING_STATE_SPACE);
        graph.explore();
        log.send(MessagesLTLFairness.LTL_FAIRNESS_EXPLORING_STATE_SPACE_DONE,
                graph.computeNumStates());
        ValueArrayAlgebra result = prepareAndIterate(origGraph, getAcceptingBSCCs(graph, quantifiedProp));
        return prodToOrigResult(result, origGraph, states);
    }

    private StateMap prodToOrigResult(ValueArrayAlgebra iterResult,
            GraphExplicit prodGraph, StateSet forStates) {
        // TODO implement more cleanly
        assert iterResult != null;
        assert prodGraph != null;
        Type typeWeight = TypeWeight.get();
        Value entry = typeWeight.newValue();
        StateSetExplicit forStatesExplicit = (StateSetExplicit) forStates;
        BitSet nodes = forStatesExplicit.getStatesExplicit();
        ValueArray resultValues = UtilValue.newArray(typeWeight.getTypeArray(), forStates.size());
        int i = 0;
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            iterResult.get(entry, i);
            resultValues.set(entry, i);
            i++;
        }
        return UtilGraph.newStateMap(forStatesExplicit.clone(), resultValues);
    }

    /** solve linear equation system */
    private ValueArrayAlgebra prepareAndIterate(GraphExplicit graph, BitSet acc)
    {
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
        objective.setMin(false);
        objective.setGraph(graph);
        objective.setTarget(acc);
        configuration.setObjective(objective);
        configuration.solve();
        ValueArrayAlgebra values = objective.getResult();
        return values;
    }


    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }


}
