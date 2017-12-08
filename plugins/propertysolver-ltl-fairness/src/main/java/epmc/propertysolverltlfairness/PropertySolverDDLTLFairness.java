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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.algorithms.dd.ComponentsDD;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderDD;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.Operator;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ValueArray;

/**
 * input property should be Limit Linear Time properties
 * 
 * @author Yong Li
 */
public final class PropertySolverDDLTLFairness implements PropertySolver {
    private enum Mod {
        F,
        G,
        UNDEF
    }

    public final static String IDENTIFIER = "ltl-fairness-dd";
    private ModelChecker modelChecker;
    private Log log;
    private GraphDD modelGraph;
    private ExpressionToDD expressionToDD;
    private boolean nonDet;
    private boolean negate;
    private Expression path;
    private ContextDD contextDD;
    private Set<Expression> stateLabels;
    private DD nodeSpace;
    private boolean skipTransient;
    private Expression property;
    private ExpressionQuantifier propertyQuantifier;
    private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    private class Expr {
        private Mod op;
        private DD expr;

        public Expr(Mod op, DD expr) {
            this.op = op;
            this.expr = expr;
        }

        @Override
        public boolean equals(Object obj) {
            if( ! (obj instanceof Expr)) return false;
            Expr exp = (Expr)obj;
            return   exp.op == op && this.expr.equals(exp.expr);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            switch(op) {
            case F:
                builder.append("F ");
                break;
            case G:
                builder.append("G ");
                break;
            default:
                break;
            }
            builder.append("( " + expr.toString() + " )");
            return builder.toString();
        }
    }
    /*
     * flatten operation 
     * */
    public Set<Set<Expr>> flatten(Expression prop, Set<Expression> labels)  {
        if (ExpressionIdentifier.is(prop) || ExpressionLiteral.is(prop)) { 
            //this should not happen
            Expr expr = new Expr(Mod.UNDEF, expressionToDD.translate(prop));
            Set<Expr> inSet = Collections.singleton(expr);
            return Collections.singleton(inSet);
        }

        if (labels.contains(prop)) {
            Expr expr = new Expr(Mod.UNDEF, expressionToDD.translate(prop));
            Set<Expr> inSet = Collections.singleton(expr);
            return Collections.singleton(inSet);
        }

        if (ExpressionOperator.is(prop)) { //AND, OR will be flattened
            ExpressionOperator expressionOperator = ExpressionOperator.as(prop);
            List<? extends Expression> ops = expressionOperator.getOperands();
            Set<Set<Expr>> set = null;
            Operator operator = expressionOperator.getOperator();
            if (operator.equals(OperatorNot.NOT)
                    || operator.equals(OperatorLt.LT)
                    || operator.equals(OperatorGt.GT)
                    || operator.equals(OperatorGe.GE)
                    || operator.equals(OperatorLe.LE)
                    || operator.equals(OperatorEq.EQ)
                    || operator.equals(OperatorNe.NE)) {
                set = new HashSet<>(); 
                Set<Expr> inSet = new HashSet<>();
                inSet.add(new Expr(Mod.UNDEF, expressionToDD.translate(prop)));
                set.add(inSet);
                return set;
            } else if (operator.equals(OperatorOr.OR)) {
                Set<Set<Expr>> op1 = flatten(ops.get(0),labels);
                op1.addAll(flatten(ops.get(1),labels));  //should be disjunction
                return op1;
            } else if (operator.equals(OperatorAnd.AND)) {
                set = new HashSet<>();
                Set<Set<Expr>> op11 = flatten(ops.get(0),labels);
                Set<Set<Expr>> op12 = flatten(ops.get(1),labels);
                for(Set<Expr> p1 : op11) {
                    for(Set<Expr> p2 : op12) {
                        Set<Expr> tmp = new HashSet<>(p1);
                        tmp.addAll(p2);  //do not change original p1
                        set.add(tmp);
                    }
                } //cartesian
                return set;
            } else {
                assert(false);
            }
        } else if (ExpressionTemporalFinally.is(prop)) {
            ExpressionTemporalFinally expr = ExpressionTemporalFinally.as(prop);
            Set<Set<Expr>> set = new HashSet<>();
            Set<Set<Expr>> op1 = flatten(expr.getOperand(),labels);
            if (ExpressionTemporalGlobally.is(expr.getOperand())
                    || ExpressionTemporalFinally.is(expr.getOperand())) {
                return op1;
            }
            for (Set<Expr> inset : op1) {
                Set<Expr> tmp = new HashSet<>();
                DD item = contextDD.newConstant(true);
                for (Expr p : inset) {
                    if(p.op == Mod.UNDEF) {
                            item = item.andWith(p.expr.clone());
                        } else {
                            tmp.add(p);
                        }
                    }
                    if (!item.isTrue()) {
                        tmp.add(new Expr(Mod.F, item));
                    } else {
                        item.dispose();
                    }
                    set.add(tmp);
            }
            return set;
        } else if (ExpressionTemporalGlobally.is(prop)) {
            ExpressionTemporalGlobally expr = ExpressionTemporalGlobally.as(prop);
            Set<Set<Expr>> set = new HashSet<>();
            Set<Set<Expr>> opset = flatten(expr.getOperand(),labels);
            if (ExpressionTemporalFinally.is(expr.getOperand())
                    || ExpressionTemporalGlobally.is(expr.getOperand())) {
                return opset;
            }
            Set<Set<Expr>> tmp1 = permute(opset);

            for(Set<Expr> inset: tmp1) {
                Set<Expr> tmp2 = new HashSet<>();
                DD item = contextDD.newConstant(false);
                for(Expr p : inset) {
                    if (p.op == Mod.UNDEF) {
                        item = item.orWith(p.expr.clone());
                    } else { 
                        tmp2.add(p);
                    }
                }
                if(! item.isFalse()) {
                    tmp2.add(new Expr(Mod.G, item));
                }else item.dispose();
                set.add(tmp2);
            }
            return permute(set);
        }
        assert false;
        return null;
    }

    /*
     * DNF <=> CNF for Set<Set<Object>>
     */
    public static Set<Set<Expr>> permute(Set<Set<Expr>> sets) {
        if (sets.size() < 2)
            return sets;
        List<Set<Expr>> listOfSets = new ArrayList<>(
                sets.size());
        for (Set<Expr> set : sets) {
            listOfSets.add(set);
        }
        Set<Set<Expr>> perms = permute(0, listOfSets);

        perms.remove(new HashSet<>());

        // remove redundant/duplicate accepting conditions
        boolean exitIteration = false;
        do {
            Set<Set<Expr>> result = new HashSet<>();
            for (Set<Expr> c : perms) {//
                boolean subsumed = false;
                Set<Expr> replace = null;

                for (Set<Expr> d : result) {
                    if (c.containsAll(d)) {
                        subsumed = true;
                        break;
                    }// c contais d, do not add
                    if (d.containsAll(c)) {
                        replace = d;
                        break;
                    }// d contains c, then remove d, add c
                }// first check whether d is subsumed by some set in result
                if (!subsumed) {
                    if (replace != null) {
                        result.remove(replace);
                    }
                    result.add(c);
                }
            }// until no more changes
            exitIteration = result.size() == perms.size();
            perms = result;
        } while (!exitIteration);
        return perms;
    }

    /*
     * recursive to get product
     */
    public static Set<Set<Expr>> permute(int index,
            List<Set<Expr>> listOfSets) {
        Set<Set<Expr>> result = new HashSet<>();
        if (index == listOfSets.size()) {
            result.add(new HashSet<Expr>());
        } else {
            for (Object list : listOfSets.get(index)) {
                for (Set set : permute(index + 1, listOfSets)) {
                    Set tmp = new HashSet<>(set);
                    set.add(list);
                    result.add(set);
                    result.add(tmp);
                }
            }
        }
        return result;
    }
    //    /**
    //     * cartesion product for Set<Set<expression>>
    //     * */
    //    private static Set<Set<Expr>> cartesian(Set<Set<Expr>> props) {
    //
    //        if(props.size() < 2) return props;
    //       
    //        List<Set<Expr>> listExpr = new ArrayList<Set<Expr>>(props.size());
    //        for(Set<Expr> set: props) {
    //            listExpr.add(set);
    //        }
    //        return cartesian(0, listExpr);
    //    }
    //
    //    private static Set<Set<Expr>> cartesian(int index,List<Set<Expr>> props) {
    //        Set<Set<Expr>> ret = new HashSet<Set<Expr>>();
    //        if(index == props.size()) {
    //            ret.add(new HashSet<Expr>());
    //        }else {
    //            for(Expr expr: props.get(index)) {
    //                for(Set<Expr> set: cartesian(index+1, props)) {
    //                    set.add(expr);
    //                    ret.add(set);
    //                }
    //            }
    //        }
    //        return ret;
    //    }

    public DD solve(Expression path, boolean isMin) {
        this.negate = isMin;              
        this.path = path;
        //this.stateLabels = path.collectIdentifiers();
        this.nodeSpace = exploreNodeSpace(modelGraph);
        DD innerResult = checkProperty(path);

        if (negate) {
            innerResult = contextDD.newConstant(1).subtract(innerResult);
        }
        this.nodeSpace.dispose();
        return innerResult;
    }

    private boolean isBSCC(GraphDD graph, DD scc)
    {
        if (!nonDet) {
            return true;
        }
        DD next = post(graph, scc, graph.getTransitions());
        return next.andNot(scc).isFalse();
    }

    private DD post(GraphDD graph, DD nodes, DD trans) {
        Permutation nextToPres = graph.getSwapPresNext();
        DD presAndActions = graph.getPresCube().and(graph.getActionCube());
        trans = trans.abstractAndExist(nodes, presAndActions);
        trans = trans.permute(nextToPres);
        return trans;
    }//move one , no more states reached

    /** find all the accepted BSCCs*/
    private DD checkProperty(Expression property) {
        // TODO Auto-generated method stub
        Expression propNorm = UtilLTL.getNormForm(property, stateLabels);
        Set<Set<Expr>> sets = flatten(propNorm,stateLabels);
        DD acSCCs = contextDD.newConstant(false);
        int numSCCs = 0;
        ComponentsDD sccs = new ComponentsDD(modelGraph, nodeSpace, skipTransient);

        for (DD scc = sccs.next(); scc != null; scc = sccs.next()) {
            if (isBSCC(modelGraph, scc)) {
                for (Set<Expr> oset : sets) { //find a set satisfied will be enough
                    if (checkSCC(scc, oset)) {
                        acSCCs = acSCCs.orWith(scc);      //have wrong operations
                        numSCCs++;
                        break;
                    }
                }
            }else {
                scc.dispose();
            }
        }
        sccs.close();

        log.send(MessagesLTLFairness.LTL_FAIRNESS_COMPUTING_END_COMPONENTS_DONE, numSCCs);
        return computeReachProbs(modelGraph, acSCCs, nodeSpace);
    }
    /**
     * check whether this BSCC will be accepted 
     * , all Exprs in oset must be satisfied 
     * */
    private boolean checkSCC(DD scc, Set<Expr> set) {
        for (Expr expr : set) {
            DD prop = expr.expr; 
            switch(expr.op) {
            case G: //Global, all the states in scc must satisfied the DD
                DD tmp = scc.andNot(prop);
                if (tmp.isFalseWith()) {
                    break;
                }
                return false;
            case F: //finally, must have common state 
                DD tmp2 = scc.and(prop);
                if (tmp2.isFalseWith()) {
                    return false;
                }
                break;
            default:
                assert false;
            }
        }
        return true;
    }

    private DD exploreNodeSpace(GraphDD graph) {
        DD sta = graph.getInitialNodes();
        DD pred = contextDD.newConstant(false);
        //        long nano = System.nanoTime();
        DD trans = graph.getTransitions().abstractExist(graph.getActionCube());
        while (!sta.equals(pred)) {
            // only exploring new states important for Rabin semi-symbolic mtd
            DD tmp = pred;
            pred = sta;
            sta = sta.or(next(trans, sta.andNot(tmp), graph.getPresCube(), graph.getSwapPresNext()));
        }
        trans.dispose();
        graph.getTransitions();
        //        nano = System.nanoTime() - nano; 
        //        System.out.println("explore graph ,time elapsed: " + nano * 1.0 /1000000 + " ms");
        return sta;
    }

    private DD next(DD trans, DD from, DD pres, Permutation swap) {
        return trans.abstractAndExist(from, pres).permuteWith(swap);
    }

    private DD computeReachProbs(GraphDD graphDD, DD target, DD nodeSpace)
    {
        //        target = ComponentsDD.reachMaxOne(graphDD, target, nodeSpace);
        DD someNodes = ComponentsDD.reachMaxSome(graphDD, target, nodeSpace).andNot(target);
        DD zeroNodes = nodeSpace.andNot(someNodes).andNot(target);

        DD init = graphDD.getInitialNodes();

        if (init.andNot(target).isFalse() || init.andNot(zeroNodes).isFalse()) {
            return target.toMT();
        }

        List<DD> sinks = new ArrayList<>();
        sinks.add(zeroNodes);
        sinks.add(target);
        Semantics semantics = graphDD.getGraphPropertyObject(CommonProperties.SEMANTICS);
        GraphBuilderDD converter = new GraphBuilderDD(graphDD, sinks, SemanticsNonDet.isNonDet(semantics));
        GraphExplicit graph = converter.buildGraph();
        BitSet targets = converter.ddToBitSet(target);
        BitSet targetS = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());

        for (int nodeNr = 0; nodeNr < graph.getNumNodes(); nodeNr++) {
            targetS.set(nodeNr, targets.get(nodeNr));
        }
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
        objective.setGraph(graph);
        objective.setMin(false);
        objective.setTarget(targetS);
        configuration.setObjective(objective);
        configuration.solve();
        ValueArray values = objective.getResult();
        DD result = converter.valuesToDD(values);
        converter.close();
        result = result.multiply(graphDD.getNodeSpace().toMT());
        result = result.add(target.andNot(graphDD.getNodeSpace()).toMT());

        return result;
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
        Options options = Options.get();
        this.log = options.get(OptionsMessages.LOG);
        this.skipTransient = options.getBoolean(OptionsLTLFairness.LTL_FAIRNESS_SCC_SKIP_TRANSIENT);
        this.contextDD = modelGraph.getContextDD();
        Expression quantifiedProp = propertyQuantifier.getQuantified();
        Set<Expression> inners = UtilLTL.collectLTLInner(propertyQuantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);            
            ExpressionToDD expressionToDD = modelGraph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        this.stateLabels = inners;
        allStates.close();
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        StateMap result = solve(quantifiedProp, (StateSetDD) forStates, min);
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = propertyQuantifier.getCompareType().asExOpType();
            result = result.applyWith(op, compare);
        }
        return result;
    }

    private StateMap solve(Expression quantifiedProp, StateSetDD forStates,
            boolean min) {
        DD res = solve(quantifiedProp, min);
        return new StateMapDD(forStates.clone(), res);
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

}
