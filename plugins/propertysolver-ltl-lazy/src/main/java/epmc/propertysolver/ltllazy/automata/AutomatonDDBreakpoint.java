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

package epmc.propertysolver.ltllazy.automata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import epmc.automaton.AutomatonDD;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiTransition;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;

public final class AutomatonDDBreakpoint implements AutomatonDD {
    private final GraphExplicit automaton;
    private final int numLabels;
    private final List<DD> rPresVars = new ArrayList<>();
    private final List<DD> rNextVars = new ArrayList<>();
    private final List<DD> cPresVars = new ArrayList<>();
    private final List<DD> cNextVars = new ArrayList<>();
    private final List<DD> counterPresVars = new ArrayList<>();
    private final List<DD> counterNextVars = new ArrayList<>();
    private final List<DD> presVars = new ArrayList<>();
    private final List<DD> nextVars = new ArrayList<>();
    private final DD initialStates;
    private final DD transition;
    private final DD accepting;
    private final DD rejecting;
    private final VariableDD counter;
    private final ExpressionToDD expressionToDD;
    private final DD presCube;
    private final DD nextCube;
    private final DD labelCube;
    private boolean closed;

    public AutomatonDDBreakpoint(ExpressionToDD expressionToDD, Buechi buechi,
            DD states, DD init, List<VariableDD> subsetVariables)
    {
        assert expressionToDD != null;
        assert buechi != null;
        assert states != null;
        this.expressionToDD = expressionToDD;
        assert buechi.getNumLabels() > 0;
        this.automaton = buechi.getGraph();
        this.numLabels = buechi.getNumLabels();
        /* prepare variables */
        if (init == null) {
            for (int state = 0; state < automaton.getNumNodes(); state++) {
                VariableDD variable = ContextDD.get().newBoolean("%autstate" + state, 2);
                rPresVars.addAll(ContextDD.get().clone(variable.getDDVariables(0)));
                rNextVars.addAll(ContextDD.get().clone(variable.getDDVariables(1)));
            }
        } else {
            for (VariableDD variable : subsetVariables) {
                rPresVars.addAll(ContextDD.get().clone(variable.getDDVariables(0)));
                rNextVars.addAll(ContextDD.get().clone(variable.getDDVariables(1)));
            }
        }
        counter = ContextDD.get().newInteger("%autcounter", 2, 0, numLabels - 1);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            VariableDD variable = ContextDD.get().newBoolean("%autstateC" + state, 2);
            cPresVars.addAll(ContextDD.get().clone(variable.getDDVariables(0)));
            cNextVars.addAll(ContextDD.get().clone(variable.getDDVariables(1)));
        }
        counterPresVars.addAll(ContextDD.get().clone(counter.getDDVariables(0)));
        counterNextVars.addAll(ContextDD.get().clone(counter.getDDVariables(1)));
        presVars.addAll(rPresVars);
        presVars.addAll(counterPresVars);
        presVars.addAll(cPresVars);
        nextVars.addAll(rNextVars);
        nextVars.addAll(counterNextVars);
        nextVars.addAll(cNextVars);

        /* compute initial state */
        if (init == null) {
            init = ContextDD.get().newConstant(true);
            BitSet bInit = buechi.getGraph().getInitialNodes();
            for (int state = 0; state < automaton.getNumNodes(); state++) {
                if (bInit.get(state)) {
                    init = init.andWith(rPresVars.get(state).clone());            		
                } else {
                    init = init.andWith(rPresVars.get(state).not());
                }
            }
        }
        DD counterInit = counter.getValueEncoding(0);
        counterInit = counterInit.clone().eqWith(ContextDD.get().newConstant(0));
        init = init.clone().andWith(counterInit);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            init = init.andWith(cPresVars.get(state).not());
        }
        initialStates = init;

        /* compute transition */
        DD incCounter = computeIncModCounter();
        DD sameCounter = eq(counterNextVars, counterPresVars);
        ArrayList<DD> rSucc = subsetImage(rPresVars);
        ArrayList<DD> cSucc = subsetImage(cPresVars);
        ArrayList<DD> cSuccOrFis = new ArrayList<>();
        ArrayList<DD> acceptanceSet = acceptanceSetByCounter();
        for (int entryNr = 0; entryNr < acceptanceSet.size(); entryNr++) {
            cSuccOrFis.add(cSucc.get(entryNr).or(acceptanceSet.get(entryNr)));
        }
        ContextDD.get().dispose(acceptanceSet);
        int trueState = buechi.getTrueState();
        if (trueState != -1) {
            DD rNextTrue = rSucc.get(trueState);
            for (int state = 0; state < rSucc.size(); state++) {
                if (state != trueState) {
                    rSucc.set(state, rSucc.get(state).andNot(rNextTrue));
                }
            }
            DD cNextTrue = cSucc.get(trueState);
            for (int state = 0; state < cSucc.size(); state++) {
                if (state != trueState) {
                    cSucc.set(state, cSucc.get(state).andNot(cNextTrue));
                }
            }
        }

        DD cSuccOrFisEqRSucc = eq(cSuccOrFis, rSucc);
        ArrayList<DD> emptySet = new ArrayList<>();
        for (int entry = 0; entry < cSuccOrFis.size(); entry++) {
            emptySet.add(ContextDD.get().newConstant(false));
        }
        DD rSuccEqEmtpy = eq(emptySet, rSucc);
        DD nextR = eq(rNextVars, rSucc);
        ContextDD.get().dispose(rSucc);
        DD nextC = eq(cNextVars, cSuccOrFis);
        ContextDD.get().dispose(cSuccOrFis);
        DD cSuccEqEmpty = eq(cNextVars, emptySet);
        DD transAccept = ContextDD.get().newConstant(true);
        transAccept = transAccept.andWith(cSuccOrFisEqRSucc.clone());
        transAccept = transAccept.andWith(nextR.clone());
        transAccept = transAccept.andWith(cSuccEqEmpty);
        transAccept = transAccept.andWith(incCounter);
        DD transNonAccept = ContextDD.get().newConstant(true);
        transNonAccept = transNonAccept.andWith(cSuccOrFisEqRSucc.notWith());
        transNonAccept = transNonAccept.andWith(nextR);
        transNonAccept = transNonAccept.andWith(nextC);
        transNonAccept = transNonAccept.andWith(sameCounter);
        transNonAccept = transNonAccept.orWith(transAccept.and(rSuccEqEmtpy));
        transAccept = transAccept.andWith(rSuccEqEmtpy.not());
        DD tr = transAccept.or(transNonAccept);
        tr = states.clone().andWith(tr).orWith(states.not().andWith(eq(presVars, nextVars)));
        transition = tr;

        /* accepting and rejecting conditions */
        /* note that we make transitions with empty R set rejecting rather
         * than removing them completely as in the paper, because complete
         * automata are more convenient to work with.
         * */
        this.accepting = transAccept.abstractExist(nextVars);
        transAccept.dispose();
        DD rejectRootNoChildren = ContextDD.get().newConstant(true);
        rejectRootNoChildren = rejectRootNoChildren.andWith(transNonAccept);
        rejectRootNoChildren = rejectRootNoChildren.andWith(eq(cSucc, emptySet));
        ContextDD.get().dispose(emptySet);
        ContextDD.get().dispose(cSucc);
        // TODO check
        DD o = rSuccEqEmtpy.orWith(rejectRootNoChildren);
        this.rejecting = o.abstractExist(nextVars);
        o.dispose();
        this.presCube = ContextDD.get().listToCube(presVars);
        this.nextCube = ContextDD.get().listToCube(nextVars);
        this.labelCube = ContextDD.get().newConstant(true);
    }

    public AutomatonDDBreakpoint(ExpressionToDD contextDD, Buechi buechi, DD states)
    {
        this(contextDD, buechi, states, null, null);
    }

    private DD eq(List<DD> set1, List<DD> set2) {
        assert set1 != null;
        assert set2 != null;
        for (DD dd : set1) {
            assert dd != null;
        }
        for (DD dd : set2) {
            assert dd != null;
        }
        assert set1.size() == set2.size();
        DD result = ContextDD.get().newConstant(true);
        Iterator<DD> set1Iter = set1.iterator();
        Iterator<DD> set2Iter = set2.iterator();
        while (set1Iter.hasNext()) {
            DD state1 = set1Iter.next();
            DD state2 = set2Iter.next();
            result = result.andWith(state1.iff(state2));
        }

        return result;
    }

    private DD computeIncModCounter() {
        DD result = counter.getValueEncoding(0).clone();
        result = result.addWith(ContextDD.get().newConstant(1));
        result = result.modWith(ContextDD.get().newConstant(numLabels));
        result = result.eqWith(counter.getValueEncoding(1).clone());
        return result;
    }

    private List<List<DD>> computeAcceptanceSets() {
        List<List<DD>> nextOns = new ArrayList<>();
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            List<DD> labelNextOns = new ArrayList<>();
            for (int state = 0; state < automaton.getNumNodes(); state++) {
                labelNextOns.add(ContextDD.get().newConstant(false));
            }
            nextOns.add(labelNextOns);
        }
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            DD presVar = rPresVars.get(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(state); succNr++) {
                BuechiTransition trans = labels.getObject(state, succNr);
                int succ = automaton.getSuccessorNode(state, succNr);
                DD guard = expressionToDD.translate(trans.getExpression());
                BitSet label = trans.getLabeling();
                for (int labelNr = 0; labelNr < numLabels; labelNr++) {
                    if (label.get(labelNr)) {
                        List<DD> labelNextOns = nextOns.get(labelNr);
                        DD nextOn = labelNextOns.get(succ);
                        nextOn = nextOn.orWith(guard.and(presVar));
                        labelNextOns.set(succ, nextOn);
                    }
                }
                guard.dispose();
            }
        }

        return nextOns;
    }

    private ArrayList<DD> acceptanceSetByCounter() {
        List<List<DD>> acceptanceSets = computeAcceptanceSets();
        ArrayList<DD> result = new ArrayList<>();
        DD counter = this.counter.getValueEncoding(0);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            DD setDD = ContextDD.get().newConstant(false);
            for (int labelNr = 0; labelNr < numLabels; labelNr++) {
                DD labelNrDD = counter.clone().eqWith(ContextDD.get().newConstant(labelNr));
                List<DD> labelSet = acceptanceSets.get(labelNr);
                setDD = setDD.orWith(labelNrDD.andWith(labelSet.get(state).clone()));
            }
            result.add(setDD);
        }
        for (List<DD> free : acceptanceSets) {
            ContextDD.get().dispose(free);
        }

        return result;
    }

    private ArrayList<DD> subsetImage(List<DD> presStates)
    {
        ArrayList<DD> nextOns = new ArrayList<>();
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            nextOns.add(ContextDD.get().newConstant(false));
        }
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            DD presVar = presStates.get(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(state); succNr++) {
                BuechiTransition trans = labels.getObject(state, succNr);
                int succ = automaton.getSuccessorNode(state, succNr);
                DD guard = expressionToDD.translate(trans.getExpression());
                DD nextOn = nextOns.get(succ);
                nextOn = nextOn.orWith(guard.andWith(presVar.clone()));
                nextOns.set(succ, nextOn);
            }
        }

        return nextOns;
    }

    @Override
    public DD getTransitions() {
        return transition;
    }

    @Override
    public DD getInitial() {
        return initialStates;
    }

    @Override
    public List<DD> getPresVars() {
        return presVars;
    }

    @Override
    public List<DD> getNextVars() {
        return nextVars;
    }

    @Override
    public List<DD> getLabelVars() {
        return new ArrayList<>();
    }

    public DD getAccepting() {
        return accepting;
    }

    public DD getRejecting() {
        return rejecting;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        initialStates.dispose();
        transition.dispose();
        accepting.dispose();
        rejecting.dispose();
        presCube.dispose();
        nextCube.dispose();
        labelCube.dispose();
        ContextDD.get().dispose(rPresVars);
        ContextDD.get().dispose(rNextVars);
        ContextDD.get().dispose(cPresVars);
        ContextDD.get().dispose(cNextVars);
        ContextDD.get().dispose(counterPresVars);
        ContextDD.get().dispose(counterNextVars);
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
    public DD getLabelCube() {
        return labelCube;
    }
}
