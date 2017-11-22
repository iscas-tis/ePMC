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

package epmc.algorithms.dd;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.algorithms.DdSccAlgorithm;
import epmc.algorithms.OptionsAlgorithm;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.dd.GraphDD;
import epmc.options.Options;

public class ComponentsDD implements Closeable {
    private static class Spine implements Closeable, Cloneable {
        private DD u;
        private DD s;
        private boolean closed;

        Spine(DD u, DD s) {
            assert u != null;
            assert s != null;
            this.u = u.clone();
            this.s = s.clone();
        }

        DD getU() {
            return u;
        }

        DD getS() {
            return s;
        }

        boolean isUEmpty() {
            return u.isFalse();
        }

        void setS(DD s) {
            this.s.dispose();
            this.s = s;
        }

        @Override
        protected Spine clone() {
            return new Spine(u, s);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            u.dispose();
            s.dispose();
        }
    }

    private static class SkelResult implements Closeable, Cloneable {
        private final DD fwSet;
        private final DD newSet;
        private final DD newNode;
        private final DD P;
        private boolean closed;

        SkelResult(DD fwd, DD newSet, DD newNode, DD P) {
            this.fwSet = fwd.clone();
            this.newSet = newSet.clone();
            this.newNode = newNode.clone();
            this.P = P.clone();
        }

        DD getFw() {
            return fwSet;
        }

        DD getNewSet() {
            return newSet;
        }

        DD getNewNode() {
            return newNode;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            fwSet.dispose();
            newSet.dispose();
            newNode.dispose();
            P.dispose();
        }

        @Override
        protected SkelResult clone() {
            return new SkelResult(fwSet, newSet, newNode, P);
        }
    }

    private enum Where {
        FIRST,
        SECOND
    };

    private class StackEntry implements Closeable {
        private final Where where;
        private final DD nodes;
        private final DD edges;
        private final Spine spine;
        private final SkelResult skelResult;
        private final DD scc;
        private boolean closed;

        public StackEntry(Where where, DD nodes, DD edges, Spine spine,
                SkelResult skelResult, DD scc) {
            assert edges.assertSupport(graph.getPresCube(), graph.getNextCube());
            this.where = where;
            if (nodes == null) {
                this.nodes = null;
            } else {
                this.nodes = nodes.clone();
            }
            this.edges = edges.clone();
            this.spine = spine.clone();
            if (skelResult == null) {
                this.skelResult = null;
            } else {
                this.skelResult = skelResult.clone();
            }
            if (scc == null) {
                this.scc = null;
            } else {
                this.scc = scc.clone();
            }
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (nodes != null) {
                nodes.dispose();
            }
            edges.dispose();
            spine.close();
            if (skelResult != null) {
                skelResult.close();
            }
            if (scc != null) {
                scc.dispose();
            }
        }
    }

    private DD mecNextNodes;
    private final boolean isNondet;
    private final boolean skipTransient;
    private final boolean startWithInit;
    private final boolean onlyBottom;
    private final Permutation nextToPres;
    private final DD transitions;
    private final DD transitionsNoActions;
    private final Deque<StackEntry> stack = new ArrayDeque<>();
    private final GraphDD graph;
    private final boolean chatterjee;
    private boolean closed;
    private final DD nodes;
    private final DD presAndActions;
    private final DD nextAndActions;

    public ComponentsDD(GraphDD graph, DD nodes, boolean onlyBSCC, boolean skipTransient) {
        assert graph != null;
        assert nodes != null;
        assert nodes.isBoolean();
        this.nodes = nodes.clone();
        Options options = Options.get();
        Semantics sem = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        this.isNondet = SemanticsNonDet.isNonDet(sem);
        this.skipTransient = skipTransient;
        this.startWithInit = true;
        this.onlyBottom = !isNondet && onlyBSCC;
        this.nextToPres = ContextDD.get().newPermutationCube(graph.getPresCube(), graph.getNextCube());
        this.transitions = graph.getTransitions().clone();
        this.transitionsNoActions = transitions.abstractExist(graph.getActionCube());
        this.graph = graph;
        this.mecNextNodes = ContextDD.get().newConstant(false);
        this.presAndActions = graph.getPresCube().and(graph.getActionCube());
        this.nextAndActions = graph.getNextCube().and(graph.getActionCube());
        DdSccAlgorithm sccAlgorithm = options.getEnum(OptionsAlgorithm.DD_SCC_ALGORITHM);
        this.chatterjee = sccAlgorithm == DdSccAlgorithm.CHATTERJEE;
        if (isNondet) {
            startMECs(nodes);
        } else {
            startSCCs(nodes);
        }
    }

    public ComponentsDD(GraphDD graph, DD nodes, boolean skipTransient) {
        this(graph, nodes, true, skipTransient);
    }

    public DD next() {
        assert !closed;
        if (isNondet) {
            return nextMEC();
        } else {
            return nextSCC();
        }
    }

    private void startSCCs(DD nodes) {
        DD edges = restrictTrans(nextToPres, transitionsNoActions, nodes);
        assert nodes.assertSupport(graph.getPresCube());
        assert edges.assertSupport(graph.getPresCube(), graph.getNextCube());
        DD falseConst = ContextDD.get().newConstant(false);
        Spine spine = new Spine(falseConst, falseConst);
        falseConst.dispose();
        while (!stack.isEmpty()) {
            stack.pop().close();
        }
        stack.push(new StackEntry(Where.FIRST, nodes, edges, spine, null, null));
        edges.dispose();
        spine.close();
    }

    private DD nextSCC() {
        DD found = null;
        while (found == null && !stack.isEmpty()) {
            StackEntry entry = stack.pop();
            DD nodes = entry.nodes;
            DD edges = entry.edges;
            Spine spine = entry.spine;
            switch (entry.where) {
            case FIRST: {
                if (nodes.isFalse()) {
                    entry.close();
                    continue;
                }
                if (spine.isUEmpty()) {
                    spine.setS(chooseS(nodes));
                }
                assert !spine.s.isFalse();
                SkelResult skelResult = skelFwd(graph, nodes, edges, spine);
                DD scc = skelResult.P.clone();
                while (!pre(scc, edges).andWith(skelResult.getFw().clone()).andNotWith(scc.clone()).isFalseWith()) {
                    scc = scc.orWith(pre(scc, edges).andWith(skelResult.getFw().clone()));
                }
                DD transNodes = ContextDD.get().newConstant(false);
                if (skipTransient) {
                    DD stableStates = skelResult.getFw().clone();
                    DD stableLast = ContextDD.get().newConstant(false);
                    while (!stableStates.equals(stableLast)) {
                        stableLast.dispose();
                        stableLast = stableStates;
                        stableStates = post(stableStates, edges);
                    }
                    stableLast.dispose();
                    transNodes.dispose();
                    transNodes = skelResult.getFw().clone().andNotWith(stableStates);
                    scc = scc.andNotWith(transNodes.clone());
                }
                if (!scc.isFalse()) {
                    if (onlyBottom) {
                        DD post = post(scc, transitionsNoActions).andNotWith(scc.clone());
                        if (post.isFalse()) {
                            found = scc.clone();
                        }
                        post.dispose();
                    } else {
                        found = scc.clone();
                    }
                }
                scc = scc.orWith(transNodes);
                DD nodesPrimed = nodes.andNot(skelResult.getFw());
                DD edgesPrimed = restrictTrans(nextToPres, edges, nodesPrimed);
                DD uPrimed = spine.getU().andNot(scc);
                DD sccAndSpine = scc.and(spine.getU());
                DD sPrimed = pre(sccAndSpine, edges).andWith(nodes.andNot(scc));
                sccAndSpine.dispose();
                assert !sPrimed.isFalse() || uPrimed.isFalse();
                Spine spinePrimed = new Spine(uPrimed, sPrimed);
                uPrimed.dispose();
                sPrimed.dispose();
                stack.push(new StackEntry(Where.SECOND, null, edges, spine, skelResult, scc));
                scc.dispose();
                skelResult.close();
                stack.push(new StackEntry(Where.FIRST, nodesPrimed, edgesPrimed, spinePrimed, null, null));
                spinePrimed.close();
                nodesPrimed.dispose();
                edgesPrimed.dispose();
                break;
            }
            case SECOND: {
                SkelResult skelResult = entry.skelResult;
                DD scc = entry.scc;
                DD nodesPrimed = skelResult.getFw().andNot(scc);
                DD edgesPrimed = restrictTrans(nextToPres, edges, nodesPrimed);
                DD uPrimed = skelResult.getNewSet().andNot(scc);
                DD sPrimed = skelResult.getNewNode().andNot(scc);
                assert !sPrimed.isFalse() || uPrimed.isFalse();
                Spine spinePrimed = new Spine(uPrimed, sPrimed);
                uPrimed.dispose();
                sPrimed.dispose();
                stack.push(new StackEntry(Where.FIRST, nodesPrimed, edgesPrimed, spinePrimed, null, null));
                spinePrimed.close();
                nodesPrimed.dispose();
                edgesPrimed.dispose();
                break;
            }
            }
            entry.close();
        }
        return found;
    }

    private DD chooseS(DD nodes) {
        DD searchIn;
        if (startWithInit) {
            DD nodesAndInit = nodes.and(graph.getInitialNodes());
            if (!nodesAndInit.isFalse()) {
                searchIn = nodesAndInit;
            } else {
                searchIn = nodes.clone();
                nodesAndInit.dispose();
            }
        } else {
            searchIn = nodes.clone();
        }
        DD result = searchIn.findSat(graph.getPresCube());
        searchIn.dispose();
        assert !result.isFalse();
        assert result.countSat(graph.getPresCube()).equals(BigInteger.ONE);
        return result;
    }

    private DD restrictTrans(Permutation nextToPres, DD edges, DD nodes)
    {
        edges = edges.and(nodes);
        DD permNodes = nodes.permute(nextToPres);
        edges = edges.andWith(permNodes);
        return edges;
    }

    private DD pre(DD nodes, DD trans) {
        assert nodes != null;
        assert trans != null;
        assert trans.assertSupport(graph.getPresCube(), graph.getNextCube());
        Permutation nextToPres = graph.getSwapPresNext();
        DD nodesPerm = nodes.permute(nextToPres);
        DD result = trans.abstractAndExist(nodesPerm, graph.getNextCube());
        return result;
    }

    private DD post(DD nodes, DD trans) {
        assert nodes != null;
        assert trans != null;
        assert trans.assertSupport(graph.getPresCube(), graph.getNextCube());
        Permutation nextToPres = graph.getSwapPresNext();
        trans = trans.abstractAndExist(nodes.clone(), graph.getPresCube());
        trans = trans.permuteWith(nextToPres);
        return trans;
    }

    private SkelResult skelFwd(GraphDD graph, DD nodes, DD edges, Spine spine) {
        assert nodes != null;
        assert edges != null;
        assert spine != null;
        assert !nodes.isFalse();
        assert !spine.getS().isFalse();
        Deque<DD> stack = new ArrayDeque<>();
        DD l = spine.getS().clone();
        DD fwd = ContextDD.get().newConstant(false);
        while (!l.isFalse()) {
            stack.push(l);
            fwd = fwd.orWith(l.clone());
            l = post(l, edges).andNotWith(fwd.clone());
        }
        l.dispose();
        DD P;
        if (chatterjee) {
            P = fwd.and(spine.getU());
        } else {
            P = spine.getS().clone();
        }
        l = stack.pop();
        DD newNode = l.findSat(graph.getPresCube());
        DD newSet = newNode.clone();
        l.dispose();
        while (!stack.isEmpty()) {
            l = stack.pop();
            if (chatterjee) {
                DD lAndP = l.and(P);
                if (!lAndP.isFalse()) {
                    lAndP.dispose();
                    l.dispose();
                    break;
                } else {
                    lAndP.dispose();
                    newSet = newSet.orWith(pre(newSet, edges).andWith(l).findSatWith(graph.getPresCube().clone()));
                }
            } else {
                newSet = newSet.orWith(pre(newSet, edges).andWith(l).findSatWith(graph.getPresCube().clone()));
            }
        }
        SkelResult result = new SkelResult(fwd, newSet, newNode, P);
        newSet.dispose();
        fwd.dispose();
        newNode.dispose();
        P.dispose();
        return result;
    }

    private void startMECs(DD nodes) {
        startSCCs(nodes);
    }

    private DD nextMEC() {
        while (true) {
            DD orig = nextSCC();
            if (orig == null) {
                if (mecNextNodes.isFalse()) {
                    return null;
                } else {
                    startSCCs(mecNextNodes);
                    mecNextNodes.dispose();
                    mecNextNodes = ContextDD.get().newConstant(false);
                    orig = nextSCC();
                    if (orig == null) {
                        return null;
                    }
                }
            }
            assert orig.assertSupport(graph.getPresCube());
            DD player = graph.getNodeProperty(CommonProperties.PLAYER);
            DD nondet = player.clone().eqWith(ContextDD.get().newConstant(Player.ONE));
            DD prob = player.clone().eqWith(ContextDD.get().newConstant(Player.STOCHASTIC));
            DD nondetProb = player.clone().eqWith(ContextDD.get().newConstant(Player.ONE_STOCHASTIC));

            //            DD presCube = graph.getPresCube();
            DD nextCube = graph.getNextCube();
            DD actionsCube = graph.getActionCube();
            DD nextAndActions = nextCube.and(actionsCube);

            DD validActions = transitions.and(nodes).abstractExistWith(nextCube.clone());

            DD scc = orig.clone();
            DD sccPred = ContextDD.get().newConstant(false);
            while (!scc.equals(sccPred)) {
                sccPred.dispose();
                sccPred = scc;
                DD sccNext = scc.permute(nextToPres);

                DD sccProb = scc.and(prob);
                DD sccNondet = scc.and(nondet);
                DD sccNondetProb = scc.and(nondetProb);

                DD sccProbStay = sccNext.not().abstractAndExistWith(transitionsNoActions.clone(), nextCube.clone());
                sccProbStay = sccProbStay.notWith().andWith(sccProb);

                DD sccNondetStay = sccNondet.and(sccNext);
                sccNondetStay = sccNondetStay.abstractAndExistWith(transitionsNoActions.clone(), nextCube.clone());                

                DD sccNondetProbStay = sccNext.not().abstractAndExistWith(transitions.clone(), nextCube.clone()).andWith(scc.clone());
                sccNondetProbStay = sccNondetProbStay.abstractImpliesForall(validActions, actionsCube);
                sccNondetProbStay = sccNondetProbStay.notWith().andWith(sccNondetProb);
                sccNext.dispose();

                scc = sccProbStay.orWith(sccNondetStay, sccNondetProbStay);
            }
            nondet.dispose();
            prob.dispose();
            nondetProb.dispose();
            sccPred.dispose();
            nextAndActions.dispose();
            validActions.dispose();
            if (orig.equals(scc)) {
                orig.dispose();
                return scc;
            } else {
                orig.dispose();
                mecNextNodes = mecNextNodes.orWith(scc);
            }
        }
    }

    private final static class QuantTypes {
        private final DD forall;
        private final DD exist;
        private final DD existForall;
        private final DD forallExist;

        public QuantTypes(DD forall, DD exist, DD existForall, DD forallExist) {
            this.forall = forall;
            this.exist = exist;
            this.existForall = existForall;
            this.forallExist = forallExist;
        }
    }

    private static DD attract(GraphDD graph, DD targetParam, DD other, boolean stochForall, boolean oneForall, boolean twoForall)
    {
        assert graph != null;
        assert targetParam != null;
        assert other != null;

        DD player = graph.getNodeProperty(CommonProperties.PLAYER);

        QuantTypes quantTypes = computeQuantTypes(player, stochForall,
                oneForall, twoForall);
        DD forall = quantTypes.forall;
        DD exist = quantTypes.exist;
        DD forallExist = quantTypes.forallExist;
        DD existForall = quantTypes.existForall;
        DD result = attract(graph, targetParam, other, forall, exist, forallExist, existForall);
        forall.dispose();
        exist.dispose();
        forallExist.dispose();
        existForall.dispose();
        return result;
    }

    public static DD attract(GraphDD graph, DD targetParam, DD other,
            DD forall, DD exist, DD forallExist, DD existForall)
    {
        ContextDD contextDD = ContextDD.get();
        DD target = targetParam.clone();
        DD prevOther = contextDD.newConstant(false);
        DD presCube = graph.getPresCube();
        DD nextCube = graph.getNextCube();
        DD actionCube = graph.getActionCube();
        DD nextAndActions = nextCube.and(actionCube);
        DD set = other.clone();
        DD transitions = graph.getTransitions();
        DD transitionsNoActions = transitions.abstractExist(actionCube);

        DD validActions = transitions.and(other).abstractExistWith(presCube.clone(), nextCube.clone());
        List<DD> transitionParts = new ArrayList<>();
        Set<DD> allParts = new LinkedHashSet<>();
        while (!validActions.isFalse()) {
            DD sat = validActions.findSat(actionCube);
            DD transPart = transitions.and(sat);
            DD singleStateActionValid = transPart.abstractExist(actionCube, nextCube);
            transPart = singleStateActionValid.clone().impliesWith(transPart);
            transPart = transPart.abstractExistWith(actionCube.clone());
            transitionParts.add(transPart);
            validActions = validActions.andNotWith(sat);
        }
        transitionParts.addAll(allParts);
        DD stateActionValid = transitions.abstractExist(nextCube);

        boolean stop = false;
        while (!stop) {
            prevOther.dispose();
            prevOther = set;

            DD targetNodesNext = target.permute(graph.getSwapPresNext());

            // forall
            DD otherForallOne;
            if (forall.isFalse()) {
                otherForallOne = contextDD.newConstant(false);
            } else {
                DD otherForall = set.and(forall);
                otherForallOne = targetNodesNext.not().abstractAndExistWith(transitionsNoActions.clone(), nextCube.clone());
                otherForallOne = otherForallOne.notWith().andWith(otherForall);
            }

            // exist
            DD otherExistOne;
            if (exist.isFalse()) {
                otherExistOne = contextDD.newConstant(false);
            } else {
                DD otherExist = set.and(exist);
                otherExistOne = targetNodesNext.abstractAndExist(transitionsNoActions, nextCube);
                otherExistOne = otherExistOne.andWith(otherExist);
            }

            // forall-exist
            DD otherForallExistOne;
            if (forallExist.isFalse()) {
                otherForallExistOne = contextDD.newConstant(false);
            } else {
                /*
                otherForallExistOne = set.and(forallExist);
                for (DD transPart : transitionParts) {
                    DD andEx = targetNodesNext.abstractAndExist(transPart, nextCube);
                    otherForallExistOne = otherForallExistOne.andWith(andEx);
                }
                 */
                DD otherForallExist = set.and(forallExist);
                otherForallExistOne = targetNodesNext.abstractAndExist(transitions, nextCube);
                otherForallExistOne = otherForallExistOne.notWith();
                otherForallExistOne = otherForallExistOne.andWith(stateActionValid.clone());
                otherForallExistOne = otherForallExistOne.abstractExistWith(actionCube.clone()).notWith();
                otherForallExistOne = otherForallExist.andWith(otherForallExistOne);
            }

            // exist-forall
            DD otherExistForallOne;
            if (existForall.isFalse()) {
                otherExistForallOne = contextDD.newConstant(false);
            } else {
                DD otherExistForall = set.and(existForall);
                otherExistForallOne = targetNodesNext.not().abstractAndExistWith(transitions.clone(), nextCube.clone());
                otherExistForallOne = otherExistForallOne.abstractExistWith(actionCube.clone()).notWith();
                otherExistForallOne = otherExistForall.andWith(otherExistForallOne);
            }

            targetNodesNext.dispose();

            DD pre = otherForallOne.orWith(otherExistOne, otherForallExistOne, otherExistForallOne);
            DD targetNew = target.clone().orWith(pre.clone());
            if (target.equals(targetNew)) {
                stop = true;
            }
            target.dispose();
            target = targetNew;
            set = set.clone().andNotWith(pre);
        }
        prevOther.dispose();
        nextAndActions.dispose();
        set.dispose();
        transitionsNoActions.dispose();
        validActions.dispose();
        contextDD.dispose(transitionParts);
        stateActionValid.dispose();
        return target;
    }

    private static QuantTypes computeQuantTypes(DD player, boolean stochForall, boolean oneForall, boolean twoForall)
    {
        ContextDD contextDD = ContextDD.get();
        DD forall = contextDD.newConstant(false);
        DD exist = contextDD.newConstant(false);
        DD existForall = contextDD.newConstant(false);
        DD forallExist = contextDD.newConstant(false);
        if (stochForall) {
            forall = forall.orWith(player.clone().eqWith(contextDD.newConstant(Player.STOCHASTIC)));
        } else {
            exist = exist.orWith(player.clone().eqWith(contextDD.newConstant(Player.STOCHASTIC)));
        }
        if (oneForall) {
            forall = forall.orWith(player.clone().eqWith(contextDD.newConstant(Player.ONE)));
        } else {
            exist = exist.orWith(player.clone().eqWith(contextDD.newConstant(Player.ONE)));
        }
        if (twoForall) {
            forall = forall.orWith(player.clone().eqWith(contextDD.newConstant(Player.TWO)));
        } else {
            exist = exist.orWith(player.clone().eqWith(contextDD.newConstant(Player.TWO)));
        }
        if (oneForall && stochForall) {
            forall = forall.orWith(player.clone().eqWith(contextDD.newConstant(Player.ONE_STOCHASTIC)));
        } else if (!oneForall && !stochForall) {
            exist = exist.orWith(player.clone().eqWith(contextDD.newConstant(Player.ONE_STOCHASTIC)));
        } else if (oneForall && !stochForall) {
            forallExist = forallExist.orWith(player.clone().eqWith(contextDD.newConstant(Player.ONE_STOCHASTIC)));
        } else if (!oneForall && stochForall) {
            existForall = existForall.orWith(player.clone().eqWith(contextDD.newConstant(Player.ONE_STOCHASTIC)));
        } else {
            assert false;
        }

        return new QuantTypes(forall, exist, existForall, forallExist);
    }

    public static DD reachMaxOne(GraphDD graph, DD target, DD nodeSpace)
    {
        return reachPre(graph, target, nodeSpace, false, true);
    }

    public static DD reachMaxSome(GraphDD graph, DD target, DD nodeSpace)
    {
        return reachPre(graph, target, nodeSpace, false, false);
    }

    public static DD reachPre(GraphDD graph, DD target, DD nodes,
            boolean min, boolean one) {
        DD blockFalse = ContextDD.get().newConstant(false);
        DD result = reachPre(graph, target, nodes, blockFalse, min, one);
        blockFalse.dispose();
        return result;
    }

    public static DD reachPre(GraphDD graph, DD target, DD nodes,
            DD block, boolean min, boolean one) {
        assert graph != null;
        assert target != null;
        assert nodes != null;
        if (one) {
            DD reachSome = reachPre(graph, target, nodes, block, min, false);
            DD reachNone = nodes.clone().andNotWith(reachSome);
            DD reachNoneOld = reachNone;
            DD nodesAndNotTarget = nodes.andNot(target);
            DD constFalse = ContextDD.get().newConstant(false);
            reachNone = reachPre(graph, reachNone, nodesAndNotTarget, constFalse, !min, false);
            constFalse.dispose();
            nodesAndNotTarget.dispose();
            reachNoneOld.dispose();
            DD result = nodes.clone().andNotWith(reachNone);
            return result;
        } else {
            boolean stochForall = false;
            boolean oneForall;
            boolean twoForall = false; // currently unused
            oneForall = min;
            DD nodesAndNotTarget = nodes.andNot(target);

            DD targetBlocked = target.andNot(block);
            DD nodesBlocked = nodesAndNotTarget.andNot(block);
            DD result = attract(graph, targetBlocked, nodesBlocked, stochForall, oneForall, twoForall);
            targetBlocked.dispose();
            nodesAndNotTarget.dispose();
            nodesBlocked.dispose();
            return result;
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        mecNextNodes.dispose();
        transitions.dispose();
        transitionsNoActions.dispose();
        nodes.dispose();
        this.presAndActions.dispose();
        this.nextAndActions.dispose();
        while (!stack.isEmpty()) {
            stack.pop().close();
        }
    }    
}
